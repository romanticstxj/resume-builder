package com.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateCreateRequest {

    @NotBlank(message = "模板名称不能为空")
    private String name;

    private String category;

    private String description;

    private String previewUrl;

    private String content;

    private String layout;

    private String themeConfig;

    private String sectionConfig;

    private Boolean isPublic = true;
}
