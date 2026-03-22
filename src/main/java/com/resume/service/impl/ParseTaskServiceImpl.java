package com.resume.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.Page;
import com.resume.dto.response.ParseResumeResponse;
import com.resume.dto.response.ParseTaskResponse;
import com.resume.entity.ParseTask;
import com.resume.repository.ParseTaskRepository;
import com.resume.service.ParseTaskService;
import com.resume.service.ResumeParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

/**
 * 解析任务Service实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParseTaskServiceImpl implements ParseTaskService {

    private final ParseTaskRepository taskRepository;
    private final ResumeParserService parserService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ParseTask createTask(MultipartFile file, Long userId) {
        ParseTask task = new ParseTask();
        task.setUserId(userId);
        task.setFileName(file.getOriginalFilename());
        task.setFileSize(file.getSize());
        task.setStatus("pending");
        task.setProgress(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Override
    public ParseTask createTask(MultipartFile file, Long userId, String plaintext) {
        ParseTask task = new ParseTask();
        task.setUserId(userId);
        task.setFileName(file.getOriginalFilename());
        task.setFileSize(file.getSize());
        task.setPlaintext(plaintext);
        task.setStatus("pending");
        task.setProgress(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void publishTaskCreatedEvent(Long taskId) {
        // set a small cooldown so RetryScheduler won't immediately pick it again
        taskRepository.setNextTryAt(taskId, java.time.LocalDateTime.now().plusSeconds(30));
        eventPublisher.publishEvent(new com.resume.event.TaskCreatedEvent(taskId));
    }

    @Override
    @Async
    public void executeParseTask(Long taskId, String fileContent, Consumer<ParseResumeResponse> callback) {
        try {
            // 更新任务状态为处理中
            updateTaskStatus(taskId, "processing", 10, null);

            log.info("开始执行解析任务: {}", taskId);

            // 更新进度
            updateTaskStatus(taskId, "processing", 30, null);

            // 执行解析
            ParseResumeResponse result = parserService.parse(fileContent);

            // 更新进度
            updateTaskStatus(taskId, "processing", 80, null);

            // 保存解析结果
            saveParseResult(taskId, result);

            // 更新任务状态为已完成
            updateTaskStatus(taskId, "completed", 100, null);

            log.info("解析任务完成: {}", taskId);

            // 调用回调（如果有）
            if (callback != null) {
                callback.accept(result);
            }

        } catch (Exception e) {
            log.error("解析任务失败: {}", taskId, e);
            updateTaskStatus(taskId, "failed", 0, e.getMessage());
        }
    }

    @Override
    @Async
    public void executeParseTaskFromFile(Long taskId, org.springframework.web.multipart.MultipartFile file) {
        try {
            // 将文件字节读取并委托给基于字节的异步方法
            byte[] bytes = file.getBytes();
            executeParseTaskFromBytes(taskId, bytes, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("从文件执行解析任务失败: {}", taskId, e);
            updateTaskStatus(taskId, "failed", 0, e.getMessage());
        }
    }

    @Override
    @Async
    public void executeParseTaskFromBytes(Long taskId, byte[] fileBytes, String originalFilename) {
        try {
            String fileContent = com.resume.util.FileParserUtil.extractTextFromBytes(fileBytes, originalFilename);
            executeParseTask(taskId, fileContent, null);
        } catch (Exception e) {
            log.error("从字节执行解析任务失败: {}", taskId, e);
            updateTaskStatus(taskId, "failed", 0, e.getMessage());
        }
    }

    @Override
    public ParseTask getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public boolean claimTaskForProcessing(Long taskId, String workerId) {
        return taskRepository.claimTaskForProcessing(taskId, workerId);
    }

    @Override
    public void markForRetry(Long taskId, int retryCount, String lastError, java.time.LocalDateTime nextTryAt) {
        taskRepository.markForRetry(taskId, retryCount, lastError, nextTryAt);
    }

    @Override
    public java.util.List<ParseTask> findDueTasks(int limit) {
        return taskRepository.findDueTasks(limit);
    }

    @Override
    public void cancelTask(Long taskId) {
        ParseTask task = getTaskById(taskId);
        if (task != null && !"success".equals(task.getStatus())) {
            task.setStatus("canceled");
            task.setUpdatedAt(java.time.LocalDateTime.now());
            taskRepository.save(task);
        }
    }

    @Override
    public Page<ParseTask> getTasksByUserId(Long userId, int page, int size) {
        return taskRepository.findByUserId(userId, page, size);
    }

    @Override
    public void updateTaskStatus(Long taskId, String status, Integer progress, String errorMessage) {
        ParseTask task = getTaskById(taskId);
        if (task != null) {
            task.setStatus(status);
            if (progress != null) {
                task.setProgress(progress);
            }
            task.setErrorMessage(errorMessage);
            task.setUpdatedAt(LocalDateTime.now());
            if ("completed".equals(status) || "failed".equals(status)) {
                task.setCompletedAt(LocalDateTime.now());
            }
            taskRepository.save(task);
        }
    }

    @Override
    public void saveParseResult(Long taskId, ParseResumeResponse result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            ParseTask task = getTaskById(taskId);
            if (task != null) {
                task.setParseResult(resultJson);
                task.setUpdatedAt(LocalDateTime.now());
                taskRepository.save(task);
            }
        } catch (JsonProcessingException e) {
            log.error("序列化解析结果失败", e);
        }
    }

    @Override
    public ParseTaskResponse toResponse(ParseTask task) {
        if (task == null) {
            return null;
        }

        ParseResumeResponse parseResult = null;
        if (task.getParseResult() != null) {
            try {
                parseResult = objectMapper.readValue(task.getParseResult(), ParseResumeResponse.class);
            } catch (JsonProcessingException e) {
                log.error("反序列化解析结果失败", e);
            }
        }

        return new ParseTaskResponse(
            task.getId(),
            task.getUserId(),
            task.getFileName(),
            task.getFileSize(),
            task.getStatus(),
            task.getProgress(),
            task.getErrorMessage(),
            parseResult,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCompletedAt()
        );
    }
}
