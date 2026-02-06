package com.resume.service;

import com.resume.dto.Page;
import com.resume.dto.TemplateCreateRequest;
import com.resume.dto.TemplateUpdateRequest;
import com.resume.entity.Template;

import java.util.List;

public interface TemplateService {
    List<Template> getAllTemplates();
    List<Template> getTemplatesByCategory(String category);
    Template getById(Long id);
    Template create(TemplateCreateRequest request, Long userId);
    Template update(Long id, TemplateUpdateRequest request, Long userId);
    void delete(Long id, Long userId);
    void incrementUsage(Long id);
}
