package com.resume.dto.request;

import lombok.Data;

/**
 * 创建解析任务请求
 */
@Data
public class ParseTaskCreateRequest {
    private String fileName;
    private Long fileSize;
}
