package com.resume.controller;

import com.resume.dto.ApiResponse;
import com.resume.dto.Page;
import com.resume.dto.request.ResumeCreateRequest;
import com.resume.dto.request.ResumeUpdateRequest;
import com.resume.entity.Resume;
import com.resume.entity.Template;
import com.resume.service.ResumeRenderService;
import com.resume.service.ResumeService;
import com.resume.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "简历接口", description = "简历CRUD相关接口")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeRenderService resumeRenderService;
    private final TemplateService templateService;

    @PostMapping
    @Operation(summary = "创建简历")
    public ApiResponse<Resume> createResume(@RequestBody ResumeCreateRequest request) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        Resume resume = resumeService.create(request, userId);
        return ApiResponse.success(resume);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取简历详情")
    public ApiResponse<Resume> getResumeById(@PathVariable Long id) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        Resume resume = resumeService.getById(id, userId);
        return ApiResponse.success(resume);
    }

    @GetMapping
    @Operation(summary = "获取简历列表")
    public ApiResponse<Page<Resume>> getResumeList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        Page<Resume> resumes = resumeService.getResumeList(userId, status, page, size);
        return ApiResponse.success(resumes);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新简历")
    public ApiResponse<Resume> updateResume(
            @PathVariable Long id,
            @RequestBody ResumeUpdateRequest request) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        Resume resume = resumeService.update(id, request, userId);
        return ApiResponse.success(resume);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除简历")
    public ApiResponse<Void> deleteResume(@PathVariable Long id) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        resumeService.delete(id, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "复制简历")
    public ApiResponse<Resume> copyResume(@PathVariable Long id) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        Resume resume = resumeService.copy(id, userId);
        return ApiResponse.success(resume);
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "获取简历HTML预览")
    public ResponseEntity<String> getResumePreview(@PathVariable Long id) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        Resume resume = resumeService.getById(id, userId);

        Template template = null;
        if (resume.getTemplateId() != null) {
            template = templateService.getById(resume.getTemplateId());
        }

        String layout = template != null ? template.getLayout() : "classic";
        String themeConfig = template != null ? template.getThemeConfig() : null;
        String sectionConfig = template != null ? template.getSectionConfig() : null;

        String html = resumeRenderService.renderToHtml(resume, layout, themeConfig, sectionConfig);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/{id}/export/{format}")
    @Operation(summary = "导出简历为PDF或Word")
    public ResponseEntity<byte[]> exportResume(
            @PathVariable Long id,
            @PathVariable String format) {
        Long userId = 1L; // TODO: 从JWT获取用户ID
        Resume resume = resumeService.getById(id, userId);

        Template template = null;
        if (resume.getTemplateId() != null) {
            template = templateService.getById(resume.getTemplateId());
        }

        String layout = template != null ? template.getLayout() : "classic";
        String themeConfig = template != null ? template.getThemeConfig() : null;
        String sectionConfig = template != null ? template.getSectionConfig() : null;

        byte[] data;
        MediaType mediaType;
        String filename;

        if ("pdf".equalsIgnoreCase(format)) {
            // 返回HTML，用户可以使用浏览器的打印功能保存为PDF
            data = resumeRenderService.exportToPdf(resume, layout, themeConfig, sectionConfig);
            mediaType = MediaType.TEXT_HTML;
            filename = resume.getTitle() + ".html";
        } else if ("word".equalsIgnoreCase(format)) {
            data = resumeRenderService.exportToWord(resume, layout, themeConfig, sectionConfig);
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
            filename = resume.getTitle() + ".docx";
        } else {
            throw new IllegalArgumentException("不支持的导出格式: " + format);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }
}

