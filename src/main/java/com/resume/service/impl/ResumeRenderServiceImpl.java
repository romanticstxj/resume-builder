package com.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.entity.Resume;
import com.resume.service.ResumeRenderService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 简历渲染服务实现
 */
@Service
@RequiredArgsConstructor
public class ResumeRenderServiceImpl implements ResumeRenderService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String renderToHtml(Resume resume, String templateLayout, String themeConfig, String sectionConfig) {
        try {
            // 解析简历内容
            Map<String, Object> content = parseJson(resume.getContent());
            Map<String, Object> theme = parseJson(themeConfig);
            Map<String, Object> sections = parseJson(sectionConfig);

            // 构建 HTML
            StringBuilder html = new StringBuilder();

            // 样式
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang='zh-CN'>\n");
            html.append("<head>\n");
            html.append("<meta charset='UTF-8'>\n");
            html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
            html.append("<title>").append(resume.getTitle()).append("</title>\n");
            html.append("<style>\n");
            html.append(getThemeStyles(theme));
            html.append("</style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("<div class='resume-container'>\n");

            // 头部
            if (shouldShowSection(sections, "header")) {
                html.append(renderHeader(content, theme, sections));
            }

            // 个人简介
            if (shouldShowSection(sections, "summary") && content.containsKey("summary")) {
                html.append(renderSummary(content));
            }

            // 工作经历
            if (shouldShowSection(sections, "experience") && content.containsKey("experience")) {
                html.append(renderExperience(content, sections));
            }

            // 教育经历
            if (shouldShowSection(sections, "education") && content.containsKey("education")) {
                html.append(renderEducation(content, sections));
            }

            html.append("</div>\n");
            html.append("</body>\n");
            html.append("</html>");

            return html.toString();
        } catch (Exception e) {
            throw new RuntimeException("渲染简历失败", e);
        }
    }

    @Override
    public byte[] exportToPdf(Resume resume, String templateLayout, String themeConfig, String sectionConfig) {
        // 生成 HTML
        String html = renderToHtml(resume, templateLayout, themeConfig, sectionConfig);
        // 返回 HTML，浏览器可以使用"打印 -> 另存为 PDF"功能
        // 或者使用 html2pdf 等库进行转换
        return html.getBytes();
    }

    @Override
    public byte[] exportToWord(Resume resume, String templateLayout, String themeConfig, String sectionConfig) {
        try (XWPFDocument document = new XWPFDocument()) {
            Map<String, Object> content = parseJson(resume.getContent());

            // 标题
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setSpacingAfter(200);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(resume.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            // 联系信息
            String email = (String) content.get("email");
            String phone = (String) content.get("phone");
            if (email != null || phone != null) {
                XWPFParagraph contactPara = document.createParagraph();
                XWPFRun contactRun = contactPara.createRun();
                if (email != null) contactRun.setText(email);
                if (email != null && phone != null) contactRun.setText(" | ");
                if (phone != null) contactRun.setText(phone);
                contactRun.setFontSize(12);
            }

            // 个人简介
            if (content.containsKey("summary")) {
                XWPFParagraph summaryPara = document.createParagraph();
                XWPFRun summaryRun = summaryPara.createRun();
                summaryRun.setText("个人简介");
                summaryRun.setBold(true);
                summaryRun.setFontSize(16);

                XWPFParagraph summaryContentPara = document.createParagraph();
                XWPFRun summaryContentRun = summaryContentPara.createRun();
                summaryContentRun.setText((String) content.get("summary"));
            }

            // 工作经历
            if (content.containsKey("experience")) {
                XWPFParagraph expTitlePara = document.createParagraph();
                XWPFRun expTitleRun = expTitlePara.createRun();
                expTitleRun.setText("工作经历");
                expTitleRun.setBold(true);
                expTitleRun.setFontSize(16);

                java.util.List<Map<String, Object>> experiences = (java.util.List<Map<String, Object>>) content.get("experience");
                if (experiences != null) {
                    for (Map<String, Object> exp : experiences) {
                        XWPFParagraph expPara = document.createParagraph();
                        XWPFRun expRun = expPara.createRun();
                        expRun.setText(exp.getOrDefault("company", "") + " - " + exp.getOrDefault("position", ""));
                        expRun.setBold(true);

                        XWPFRun periodRun = expPara.createRun();
                        periodRun.setText("  (" + exp.getOrDefault("period", "") + ")");
                        periodRun.setItalic(true);

                        XWPFParagraph descPara = document.createParagraph();
                        XWPFRun descRun = descPara.createRun();
                        descRun.setText((String) exp.getOrDefault("description", ""));
                    }
                }
            }

            // 教育经历
            if (content.containsKey("education")) {
                XWPFParagraph eduTitlePara = document.createParagraph();
                XWPFRun eduTitleRun = eduTitlePara.createRun();
                eduTitleRun.setText("教育经历");
                eduTitleRun.setBold(true);
                eduTitleRun.setFontSize(16);

                java.util.List<Map<String, Object>> educations = (java.util.List<Map<String, Object>>) content.get("education");
                if (educations != null) {
                    for (Map<String, Object> edu : educations) {
                        XWPFParagraph eduPara = document.createParagraph();
                        XWPFRun eduRun = eduPara.createRun();
                        eduRun.setText(edu.getOrDefault("school", "") + " - " + edu.getOrDefault("major", ""));
                        eduRun.setBold(true);

                        XWPFRun periodRun = eduPara.createRun();
                        periodRun.setText("  (" + edu.getOrDefault("period", "") + ")");
                        periodRun.setItalic(true);

                        XWPFParagraph degPara = document.createParagraph();
                        XWPFRun degRun = degPara.createRun();
                        degRun.setText((String) edu.getOrDefault("degree", ""));
                    }
                }
            }

            // 输出
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Word失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSectionConfig(Map<String, Object> sections, String sectionName) {
        if (sections == null || !sections.containsKey(sectionName)) {
            return new HashMap<>();
        }
        return (Map<String, Object>) sections.get(sectionName);
    }

    private boolean shouldShowSection(Map<String, Object> sections, String sectionName) {
        Map<String, Object> config = getSectionConfig(sections, sectionName);
        return config.get("show") != Boolean.FALSE;
    }

    private String getThemeStyles(Map<String, Object> theme) {
        StringBuilder styles = new StringBuilder();
        String primaryColor = (String) theme.getOrDefault("primaryColor", "#2c3e50");
        String secondaryColor = (String) theme.getOrDefault("secondaryColor", "#3498db");
        String textColor = (String) theme.getOrDefault("textColor", "#333333");
        String fontFamily = (String) theme.getOrDefault("fontFamily", "Arial, sans-serif");
        String fontSize = (String) theme.getOrDefault("fontSize", "14px");

        styles.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n");
        styles.append("body { font-family: '").append(fontFamily).append("'; font-size: ").append(fontSize).append("; color: ").append(textColor).append("; background: #f5f5f5; padding: 20px; }\n");
        styles.append(".resume-container { max-width: 800px; margin: 0 auto; background: white; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n");
        styles.append(".resume-header { margin-bottom: 30px; }\n");
        styles.append(".name { font-size: 32px; font-weight: 700; color: ").append(primaryColor).append("; margin-bottom: 10px; }\n");
        styles.append(".contact-info { font-size: 14px; color: #666; }\n");
        styles.append(".section { margin-bottom: 25px; }\n");
        styles.append(".section-title { font-size: 18px; font-weight: 600; color: ").append(primaryColor).append("; padding-bottom: 8px; margin-bottom: 15px; border-bottom: 2px solid ").append(primaryColor).append("; }\n");
        styles.append(".item { margin-bottom: 15px; }\n");
        styles.append(".item-header { display: flex; justify-content: space-between; margin-bottom: 5px; }\n");
        styles.append(".item-title { font-size: 16px; font-weight: 600; }\n");
        styles.append(".item-period { font-size: 13px; color: #666; }\n");
        styles.append(".item-description { font-size: 14px; line-height: 1.6; }\n");

        return styles.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderHeader(Map<String, Object> content, Map<String, Object> theme, Map<String, Object> sections) {
        StringBuilder html = new StringBuilder();
        Map<String, Object> headerConfig = getSectionConfig(sections, "header");
        String style = (String) headerConfig.getOrDefault("style", "left");

        String textAlign = "center".equals(style) ? "text-align: center;" : "";

        html.append("<div class='resume-header' style='").append(textAlign).append("'>\n");
        html.append("<h1 class='name'>").append(content.getOrDefault("name", "")).append("</h1>\n");

        String email = (String) content.get("email");
        String phone = (String) content.get("phone");
        if (email != null || phone != null) {
            html.append("<div class='contact-info'>\n");
            if (email != null) html.append(email);
            if (email != null && phone != null) html.append(" | ");
            if (phone != null) html.append(phone);
            html.append("</div>\n");
        }

        html.append("</div>\n");
        return html.toString();
    }

    private String renderSummary(Map<String, Object> content) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>\n");
        html.append("<h2 class='section-title'>个人简介</h2>\n");
        html.append("<p>").append(content.getOrDefault("summary", "")).append("</p>\n");
        html.append("</div>\n");
        return html.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderExperience(Map<String, Object> content, Map<String, Object> sections) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>\n");
        html.append("<h2 class='section-title'>工作经历</h2>\n");

        java.util.List<Map<String, Object>> experiences = (java.util.List<Map<String, Object>>) content.get("experience");
        if (experiences != null) {
            for (Map<String, Object> exp : experiences) {
                html.append("<div class='item'>\n");
                html.append("<div class='item-header'>\n");
                html.append("<div class='item-title'>").append(exp.getOrDefault("company", "")).append(" - ").append(exp.getOrDefault("position", "")).append("</div>\n");
                html.append("<div class='item-period'>").append(exp.getOrDefault("period", "")).append("</div>\n");
                html.append("</div>\n");
                html.append("<div class='item-description'>").append(exp.getOrDefault("description", "")).append("</div>\n");
                html.append("</div>\n");
            }
        }

        html.append("</div>\n");
        return html.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderEducation(Map<String, Object> content, Map<String, Object> sections) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>\n");
        html.append("<h2 class='section-title'>教育经历</h2>\n");

        java.util.List<Map<String, Object>> educations = (java.util.List<Map<String, Object>>) content.get("education");
        if (educations != null) {
            for (Map<String, Object> edu : educations) {
                html.append("<div class='item'>\n");
                html.append("<div class='item-header'>\n");
                html.append("<div class='item-title'>").append(edu.getOrDefault("school", "")).append(" - ").append(edu.getOrDefault("major", "")).append("</div>\n");
                html.append("<div class='item-period'>").append(edu.getOrDefault("period", "")).append("</div>\n");
                html.append("</div>\n");
                html.append("<div class='item-description'>").append(edu.getOrDefault("degree", "")).append("</div>\n");
                html.append("</div>\n");
            }
        }

        html.append("</div>\n");
        return html.toString();
    }
}
