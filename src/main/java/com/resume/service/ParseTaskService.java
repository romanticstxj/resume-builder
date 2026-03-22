package com.resume.service;

import com.resume.dto.Page;
import com.resume.dto.response.ParseResumeResponse;
import com.resume.dto.response.ParseTaskResponse;
import com.resume.entity.ParseTask;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Consumer;

/**
 * 解析任务Service
 */
public interface ParseTaskService {
    /**
     * 创建新的解析任务
     */
    ParseTask createTask(MultipartFile file, Long userId);

    /**
     * 创建新的解析任务并保存 plaintext（选择性使用）
     */
    ParseTask createTask(MultipartFile file, Long userId, String plaintext);

    /**
     * 异步执行解析任务
     */
    void executeParseTask(Long taskId, String fileContent, Consumer<ParseResumeResponse> callback);

    /**
     * 从上传的文件异步执行解析（Controller 调用此方法后应立即返回）
     */
    void executeParseTaskFromFile(Long taskId, org.springframework.web.multipart.MultipartFile file);

    /**
     * 从文件字节异步执行解析（Controller 在读取字节后立即返回）
     */
    void executeParseTaskFromBytes(Long taskId, byte[] fileBytes, String originalFilename);

    /**
     * 获取任务详情
     */
    ParseTask getTaskById(Long id);

    /**
     * 获取用户的任务列表
     */
    Page<ParseTask> getTasksByUserId(Long userId, int page, int size);

    /**
     * 更新任务状态
     */
    void updateTaskStatus(Long taskId, String status, Integer progress, String errorMessage);

    /**
     * 保存解析结果
     */
    void saveParseResult(Long taskId, ParseResumeResponse result);

    /**
     * Try to atomically claim a task for processing.
     */
    boolean claimTaskForProcessing(Long taskId, String workerId);

    /**
     * Mark a task for retry with next try time.
     */
    void markForRetry(Long taskId, int retryCount, String lastError, java.time.LocalDateTime nextTryAt);

    /**
     * Find due tasks for retry/processing
     */
    java.util.List<ParseTask> findDueTasks(int limit);

    /**
     * Cancel a task
     */
    void cancelTask(Long taskId);

    /**
     * Publish TaskCreatedEvent within a transaction (ensures AFTER_COMMIT listeners run)
     */
    void publishTaskCreatedEvent(Long taskId);

    /**
     * 将ParseTask转换为ParseTaskResponse
     */
    ParseTaskResponse toResponse(ParseTask task);
}
