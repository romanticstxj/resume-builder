package com.resume.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 解析任务实体
 */
@Data
public class ParseTask {
    private Long id;
    private Long userId;
    private String fileName;
    private Long fileSize;
    private String status;
    private Integer progress;
    private String errorMessage;
    private String parseResult;
    private String plaintext;
    private Integer retryCount;
    private Integer maxRetries;
    private String lastError;
    private String processingBy;
    private java.time.LocalDateTime processingAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
