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
    public ParseTask getTaskById(Long id) {
        return taskRepository.findById(id);
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
