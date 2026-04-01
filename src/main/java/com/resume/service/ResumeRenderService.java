package com.resume.service;

import com.resume.entity.Resume;

/**
 * 简历渲染服务接口
 */
public interface ResumeRenderService {

    /**
     * 将简历渲染为 HTML
     * @param sectionOrder 模板定义的区块顺序（JSON 数组字符串），为 null 时使用枚举默认顺序
     */
    String renderToHtml(Resume resume, String templateLayout, String themeConfig, String sectionConfig, String sectionOrder);

    /**
     * 将简历导出为 PDF
     * @param sectionOrder 模板定义的区块顺序（JSON 数组字符串），为 null 时使用枚举默认顺序
     */
    byte[] exportToPdf(Resume resume, String templateLayout, String themeConfig, String sectionConfig, String sectionOrder);

    /**
     * 将简历导出为 Word
     * @param sectionOrder 模板定义的区块顺序（JSON 数组字符串），为 null 时使用枚举默认顺序
     */
    byte[] exportToWord(Resume resume, String templateLayout, String themeConfig, String sectionConfig, String sectionOrder);
}
