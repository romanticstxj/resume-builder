package com.resume.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 简历模板实体
 */
@Getter
@Setter
public class Template {

    private Long id;

    private String name;

    private String category;

    private String description;

    private String previewUrl;

    private String content;  // JSON格式存储模板初始内容

    private String layout;  // 布局类型：classic, modern, minimalist, creative

    private String themeConfig;  // JSON格式存储主题配置（颜色、字体等）

    private String sectionConfig;  // JSON格式存储区块配置

    private String sectionOrder;  // JSON格式存储区块顺序

    private Boolean isOfficial = false;

    private Boolean isPublic = true;

    private Long createdBy;

    private Integer usageCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
