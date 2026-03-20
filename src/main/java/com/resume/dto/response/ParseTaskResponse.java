package com.resume.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 解析任务响应
 */
@Data
public class ParseTaskResponse {
    private Long id;
    private Long userId;
    private String fileName;
    private Long fileSize;
    private String status;
    private Integer progress;
    private String errorMessage;
    private com.resume.dto.response.ParseResumeResponse parseResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    public ParseTaskResponse(Long id, Long userId, String fileName, Long fileSize, String status,
                           Integer progress, String errorMessage,
                           com.resume.dto.response.ParseResumeResponse parseResult,
                           LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime completedAt) {
        this.id = id;
        this.userId = userId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = status;
        this.progress = progress;
        this.errorMessage = errorMessage;
        this.parseResult = parseResult;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }
}
