package com.resume.event;

import com.google.common.util.concurrent.RateLimiter;
import com.resume.dto.response.ParseResumeResponse;
import com.resume.entity.ParseTask;
import com.resume.service.ParseTaskService;
import com.resume.service.ResumeParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
// ...existing code... (removed unused AsyncResult and Scheduled imports)
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * 监听任务创建事件并执行 AI 解析
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AIParseListener {

    private final ParseTaskService taskService;
    private final ResumeParserService parserService;

    // Rate limiter injected from AsyncConfig bean (shared with RetryScheduler)
    private final RateLimiter rateLimiter;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // worker id for this listener instance
    private final String workerId = UUID.randomUUID().toString();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("aiParsingExecutor")
    public void onTaskCreated(TaskCreatedEvent event) {
        Long taskId = event.getTaskId();
        log.info("AIParseListener invoked for task {} (transactional listener)", taskId);
        try {
            // Simplified: rely on atomic claimTaskForProcessing to check/persist pending->processing.
            // A previous short-circuit pre-check was redundant and could cause race conditions.

            // try to atomically claim the task for processing (record workerId)
            boolean claimed = taskService.claimTaskForProcessing(taskId, workerId);
            if (!claimed) {
                log.info("AIParseListener: task {} not claimed (maybe processed by other worker)", taskId);
                return;
            }

            // read plaintext after claim
            ParseTask claimedTask = taskService.getTaskById(taskId);
            String plaintext = claimedTask.getPlaintext();
            if (plaintext == null || plaintext.isBlank()) {
                // nothing to parse
                taskService.updateTaskStatus(taskId, "failed", 0, "empty plaintext");
                return;
            }

            // Acquire rate limiter token (block up to 5s)
            boolean ok = rateLimiter.tryAcquire(5, TimeUnit.SECONDS);
            if (!ok) {
                scheduleRetry(taskId, "rate limit timeout");
                return;
            }

            // perform parsing
            String lang = claimedTask.getLanguage() != null ? claimedTask.getLanguage() : "zh";
            ParseResumeResponse result = parserService.parse(plaintext, lang);
            taskService.saveParseResult(taskId, result);
            taskService.updateTaskStatus(taskId, "success", 100, null);

        } catch (Exception e) {
            log.error("AI 解析失败, taskId={}", taskId, e);
            handleFailure(taskId, e.getMessage());
        }
    }

    // Fallback: handle published events even when not using transactional event publishing
    // Note: non-transactional fallback listener removed to avoid pre-commit execution.
    // TransactionalEventListener (AFTER_COMMIT) above is sufficient and prevents
    // attempting to claim DB rows before the creating transaction commits.

    private void handleFailure(Long taskId, String errorMessage) {
        try {
            ParseTask task = taskService.getTaskById(taskId);
            if (task == null) return;
            int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
            int maxRetries = task.getMaxRetries() == null ? 3 : task.getMaxRetries();
            if (retries + 1 >= maxRetries) {
                task.setRetryCount(retries + 1);
                task.setLastError(errorMessage);
                taskService.updateTaskStatus(taskId, "failed", 0, errorMessage);
            } else {
                task.setRetryCount(retries + 1);
                task.setLastError(errorMessage);
                taskService.updateTaskStatus(taskId, "pending", 0, errorMessage);
                long delay = Math.min(2L << retries, 120L);
                scheduler.schedule(() -> {
                    taskService.updateTaskStatus(taskId, "pending", 0, null);
                    // republish event via direct call
                    onTaskCreated(new TaskCreatedEvent(taskId));
                }, delay, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            log.error("处理失败时出现异常, taskId={}", taskId, ex);
        }
    }

    private void scheduleRetry(Long taskId, String reason) {
        log.info("Scheduling retry for task {} due to {}", taskId, reason);
        scheduler.schedule(() -> onTaskCreated(new TaskCreatedEvent(taskId)), 5, TimeUnit.SECONDS);
    }
}

