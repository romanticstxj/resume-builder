package com.resume.service;

import com.resume.entity.Resume;

/**
 * 简历渲染服务接口
 */
public interface ResumeRenderService {

    /**
     * 将简历渲染为 HTML
     */
    String renderToHtml(Resume resume, String templateLayout, String themeConfig, String sectionConfig);

    /**
     * 将简历导出为 PDF
     */
    byte[] exportToPdf(Resume resume, String templateLayout, String themeConfig, String sectionConfig);

    /**
     * 将简历导出为 Word
     */
    byte[] exportToWord(Resume resume, String templateLayout, String themeConfig, String sectionConfig);
}
