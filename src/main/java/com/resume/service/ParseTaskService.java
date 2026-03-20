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
     * 异步执行解析任务
     */
    void executeParseTask(Long taskId, String fileContent, Consumer<ParseResumeResponse> callback);

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
     * 将ParseTask转换为ParseTaskResponse
     */
    ParseTaskResponse toResponse(ParseTask task);
}
