package com.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResumeCreateRequest {

    @NotBlank(message = "简历标题不能为空")
    private String title;

    private Long templateId;

    @NotBlank(message = "简历内容不能为空")
    private String content;  // JSON格式
}
