package com.resume.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 简历实体
 */
@Getter
@Setter
public class Resume {

    private Long id;

    private Long userId;

    private String title;

    private Long templateId;

    private String content;  // JSON格式存储简历内容

    private String sectionOrder;  // JSON格式存储区块顺序

    private String status = "draft";  // draft, published

    private Boolean isPrimary = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
