package com.resume.event;

import com.google.common.util.concurrent.RateLimiter;
import com.resume.dto.response.ParseResumeResponse;
import com.resume.entity.ParseTask;
import com.resume.service.ParseTaskService;
import com.resume.service.ResumeParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 定时扫描到期任务并在当前线程同步执行解析（先认领一次）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryScheduler {

    private final ParseTaskService taskService;
    private final ResumeParserService parserService;
    private final RateLimiter rateLimiter;

    // identifier for this scheduler worker
    private final String workerId = UUID.randomUUID().toString();

    @Scheduled(fixedDelayString = "30000")
    public void scanAndProcessDueTasks() {
        try {
            List<ParseTask> due = taskService.findDueTasks(50);
            for (ParseTask t : due) {
                Long id = t.getId();
                log.info("RetryScheduler attempting to claim task {}", id);
                boolean claimed = taskService.claimTaskForProcessing(id, workerId);
                if (!claimed) {
                    log.info("RetryScheduler could not claim task {}", id);
                    continue;
                }

                // process synchronously
                try {
                    ParseTask task = taskService.getTaskById(id);
                    if (task == null) continue;
                    if ("canceled".equals(task.getStatus())) {
                        log.info("Task {} was canceled, skipping", id);
                        continue;
                    }
                    String plaintext = task.getPlaintext();
                    if (plaintext == null || plaintext.isBlank()) {
                        taskService.updateTaskStatus(id, "failed", 0, "empty plaintext");
                        continue;
                    }

                    boolean ok = rateLimiter.tryAcquire(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (!ok) {
                        // schedule retry
                        scheduleRetryPersistent(task);
                        continue;
                    }

                    ParseResumeResponse result = parserService.parse(plaintext);
                    taskService.saveParseResult(id, result);
                    taskService.updateTaskStatus(id, "success", 100, null);
                    log.info("RetryScheduler processed task {} successfully", id);

                } catch (Exception ex) {
                    log.error("RetryScheduler processing failed for task {}", id, ex);
                    scheduleRetryPersistent(taskService.getTaskById(id));
                }
            }
        } catch (Exception e) {
            log.error("RetryScheduler failed", e);
        }
    }

    private void scheduleRetryPersistent(ParseTask task) {
        if (task == null) return;
        try {
            int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
            int maxRetries = task.getMaxRetries() == null ? 3 : task.getMaxRetries();
            if (retries + 1 >= maxRetries) {
                taskService.updateTaskStatus(task.getId(), "failed", 0, task.getLastError());
                return;
            }
            int nextRetry = Math.min((1 << retries) * 2, 120);
            java.time.LocalDateTime nextTry = LocalDateTime.now().plusSeconds(nextRetry);
            taskService.markForRetry(task.getId(), retries + 1, task.getLastError(), nextTry);
            log.info("Scheduled retry for task {} in {}s", task.getId(), nextRetry);
        } catch (Exception e) {
            log.error("scheduleRetryPersistent failed", e);
        }
    }
}

