package com.resume.service.impl;

import com.resume.dto.request.TemplateCreateRequest;
import com.resume.dto.request.TemplateUpdateRequest;
import com.resume.entity.Template;
import com.resume.repository.TemplateRepository;
import com.resume.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    @Override
    public List<Template> getAllTemplates() {
        return templateRepository.findByIsPublicOrderByIsOfficialDescUsageCountDesc(true);
    }

    @Override
    public List<Template> getTemplatesByCategory(String category) {
        return templateRepository.findByCategoryAndIsPublicOrderByUsageCountDesc(category, true);
    }

    @Override
    public Template getById(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        return template;
    }

    @Override
    @Transactional
    public Template create(TemplateCreateRequest request, Long userId) {
        Template template = new Template();
        template.setName(request.getName());
        template.setCategory(request.getCategory());
        template.setDescription(request.getDescription());
        template.setPreviewUrl(request.getPreviewUrl());
        template.setContent(request.getContent());
        template.setLayout(request.getLayout());
        template.setThemeConfig(request.getThemeConfig());
        template.setSectionConfig(request.getSectionConfig());
        template.setSectionOrder(request.getSectionOrder());
        template.setIsOfficial(false);
        template.setIsPublic(request.getIsPublic());
        template.setCreatedBy(userId);
        template.setUsageCount(0);
        template.setCreatedAt(java.time.LocalDateTime.now());
        template.setUpdatedAt(java.time.LocalDateTime.now());

        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public Template update(Long id, TemplateUpdateRequest request, Long userId) {
        Template template = getById(id);

        if (template.getIsOfficial()) {
            throw new RuntimeException("不能修改官方模板");
        }

        if (!template.getCreatedBy().equals(userId)) {
            throw new RuntimeException("只能修改自己创建的模板");
        }

        template.setName(request.getName());
        template.setCategory(request.getCategory());
        template.setDescription(request.getDescription());
        template.setPreviewUrl(request.getPreviewUrl());
        template.setContent(request.getContent());
        template.setLayout(request.getLayout());
        template.setThemeConfig(request.getThemeConfig());
        template.setSectionConfig(request.getSectionConfig());
        template.setSectionOrder(request.getSectionOrder());
        if (request.getIsPublic() != null) {
            template.setIsPublic(request.getIsPublic());
        }
        template.setUpdatedAt(java.time.LocalDateTime.now());

        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Template template = getById(id);

        if (template.getIsOfficial()) {
            throw new RuntimeException("不能删除官方模板");
        }

        if (!template.getCreatedBy().equals(userId)) {
            throw new RuntimeException("只能删除自己创建的模板");
        }

        templateRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void incrementUsage(Long id) {
        Template template = templateRepository.findById(id);
        if (template != null) {
            templateRepository.incrementUsage(id);
        }
    }
}

