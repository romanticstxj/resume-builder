package com.resume.dto;

import lombok.Data;

@Data
public class ResumeUpdateRequest {
    private String title;
    private Long templateId;
    private String content;
    private String status;  // draft, published
    private Boolean isPrimary;
}
