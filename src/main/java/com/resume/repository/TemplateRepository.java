package com.resume.repository;

import com.resume.entity.Template;
import com.resume.mapper.TemplateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TemplateRepository {

    private final TemplateMapper templateMapper;

    public TemplateRepository(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public List<Template> findByIsPublicOrderByIsOfficialDescUsageCountDesc(Boolean isPublic) {
        return templateMapper.findByIsPublicOrderByIsOfficialDescUsageCountDesc(isPublic);
    }

    public List<Template> findByCategoryAndIsPublicOrderByUsageCountDesc(String category, Boolean isPublic) {
        return templateMapper.findByCategoryAndIsPublicOrderByUsageCountDesc(category, isPublic);
    }

    public Template findById(Long id) {
        return templateMapper.findById(id);
    }

    public Template save(Template template) {
        if (template.getId() == null) {
            templateMapper.insert(template);
        } else {
            templateMapper.update(template);
        }
        return template;
    }

    public void deleteById(Long id) {
        templateMapper.delete(id);
    }

    public void incrementUsage(Long id) {
        templateMapper.incrementUsage(id);
    }
}
