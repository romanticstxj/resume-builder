package com.resume.controller;

import com.resume.config.SecurityUtils;
import com.resume.dto.ApiResponse;
import com.resume.dto.request.TemplateCreateRequest;
import com.resume.dto.request.TemplateUpdateRequest;
import com.resume.entity.Template;
import com.resume.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "模板接口", description = "模板CRUD相关接口")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @Operation(summary = "获取模板列表")
    public ApiResponse<List<Template>> getAllTemplates() {
        return ApiResponse.success(templateService.getAllTemplates());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取模板")
    public ApiResponse<List<Template>> getTemplatesByCategory(@PathVariable String category) {
        return ApiResponse.success(templateService.getTemplatesByCategory(category));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情")
    public ApiResponse<Template> getTemplateById(@PathVariable Long id) {
        Template template = templateService.getById(id);
        templateService.incrementUsage(id);
        return ApiResponse.success(template);
    }

    @PostMapping
    @Operation(summary = "创建模板")
    public ApiResponse<Template> createTemplate(@RequestBody TemplateCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Template template = templateService.create(request, userId);
        return ApiResponse.success(template);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板")
    public ApiResponse<Template> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Template template = templateService.update(id, request, userId);
        return ApiResponse.success(template);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        templateService.delete(id, userId);
        return ApiResponse.success(null);
    }
}
