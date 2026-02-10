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

            // 解析sectionOrder获取模块顺序
            java.util.List<String> sectionOrder = (java.util.List<String>) parseJson(resume.getSectionOrder() != null ? resume.getSectionOrder() : (sectionConfig != null ? sections.get("order").toString() : null));
            if (sectionOrder == null || sectionOrder.isEmpty()) {
                sectionOrder = java.util.Arrays.asList("header", "summary", "experience", "education", "projects", "skills", "honors", "portfolio");
            }

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

            // 根据sectionOrder动态渲染模块
            for (String sectionName : sectionOrder) {
                switch (sectionName) {
                    case "header":
                        if (shouldShowSection(sections, "header")) {
                            html.append(renderHeader(content, theme, sections));
                        }
                        break;
                    case "summary":
                        if (shouldShowSection(sections, "summary") && content.containsKey("summary") && content.get("summary") != null) {
                            html.append(renderSummary(content));
                        }
                        break;
                    case "experience":
                        if (shouldShowSection(sections, "experience") && content.containsKey("experience") && content.get("experience") != null) {
                            html.append(renderExperience(content, sections));
                        }
                        break;
                    case "education":
                        if (shouldShowSection(sections, "education") && content.containsKey("education") && content.get("education") != null) {
                            html.append(renderEducation(content, sections));
                        }
                        break;
                    case "projects":
                        if (shouldShowSection(sections, "projects") && content.containsKey("projects") && content.get("projects") != null) {
                            html.append(renderProjects(content));
                        }
                        break;
                    case "skills":
                        if (shouldShowSection(sections, "skills") && content.containsKey("skills") && content.get("skills") != null) {
                            html.append(renderSkills(content));
                        }
                        break;
                    case "honors":
                        if (shouldShowSection(sections, "honors") && content.containsKey("honors") && content.get("honors") != null) {
                            html.append(renderHonors(content));
                        }
                        break;
                    case "portfolio":
                        if (shouldShowSection(sections, "portfolio") && content.containsKey("portfolio") && content.get("portfolio") != null) {
                            html.append(renderPortfolio(content));
                        }
                        break;
                }
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

            // 头部 - 姓名
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setSpacingAfter(200);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText((String) content.getOrDefault("name", "姓名"));
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            // 联系信息
            String email = (String) content.get("email");
            String phone = (String) content.get("phone");
            String location = (String) content.get("location");
            if (email != null || phone != null || location != null) {
                XWPFParagraph contactPara = document.createParagraph();
                XWPFRun contactRun = contactPara.createRun();
                if (email != null) contactRun.setText(email);
                if (email != null && phone != null) contactRun.setText(" | ");
                if (phone != null) contactRun.setText(phone);
                if (location != null && (email != null || phone != null)) contactRun.setText(" | ");
                if (location != null) contactRun.setText(location);
                contactRun.setFontSize(12);
            }

            // 个人简介
            if (content.containsKey("summary") && content.get("summary") != null) {
                XWPFParagraph summaryPara = document.createParagraph();
                summaryPara.setSpacingBefore(200);
                summaryPara.setSpacingAfter(100);
                XWPFRun summaryRun = summaryPara.createRun();
                summaryRun.setText("个人简介");
                summaryRun.setBold(true);
                summaryRun.setFontSize(16);

                XWPFParagraph summaryContentPara = document.createParagraph();
                XWPFRun summaryContentRun = summaryContentPara.createRun();
                summaryContentRun.setText((String) content.get("summary"));
            }

            // 工作经历
            if (content.containsKey("experience") && content.get("experience") != null) {
                XWPFParagraph expTitlePara = document.createParagraph();
                expTitlePara.setSpacingBefore(200);
                expTitlePara.setSpacingAfter(100);
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
            if (content.containsKey("education") && content.get("education") != null) {
                XWPFParagraph eduTitlePara = document.createParagraph();
                eduTitlePara.setSpacingBefore(200);
                eduTitlePara.setSpacingAfter(100);
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

            // 项目经历
            if (content.containsKey("projects") && content.get("projects") != null) {
                XWPFParagraph projTitlePara = document.createParagraph();
                projTitlePara.setSpacingBefore(200);
                projTitlePara.setSpacingAfter(100);
                XWPFRun projTitleRun = projTitlePara.createRun();
                projTitleRun.setText("项目经历");
                projTitleRun.setBold(true);
                projTitleRun.setFontSize(16);

                java.util.List<Map<String, Object>> projects = (java.util.List<Map<String, Object>>) content.get("projects");
                if (projects != null) {
                    for (Map<String, Object> proj : projects) {
                        XWPFParagraph projPara = document.createParagraph();
                        XWPFRun projRun = projPara.createRun();
                        projRun.setText((String) proj.getOrDefault("name", ""));
                        projRun.setBold(true);

                        XWPFRun projPeriodRun = projPara.createRun();
                        projPeriodRun.setText("  (" + proj.getOrDefault("period", "") + ")");
                        projPeriodRun.setItalic(true);

                        XWPFParagraph projDescPara = document.createParagraph();
                        XWPFRun projDescRun = projDescPara.createRun();
                        projDescRun.setText((String) proj.getOrDefault("description", ""));
                    }
                }
            }

            // 专业技能
            if (content.containsKey("skills") && content.get("skills") != null) {
                XWPFParagraph skillsTitlePara = document.createParagraph();
                skillsTitlePara.setSpacingBefore(200);
                skillsTitlePara.setSpacingAfter(100);
                XWPFRun skillsTitleRun = skillsTitlePara.createRun();
                skillsTitleRun.setText("专业技能");
                skillsTitleRun.setBold(true);
                skillsTitleRun.setFontSize(16);

                java.util.List<Map<String, Object>> skills = (java.util.List<Map<String, Object>>) content.get("skills");
                if (skills != null) {
                    for (Map<String, Object> skill : skills) {
                        XWPFParagraph skillPara = document.createParagraph();
                        XWPFRun skillRun = skillPara.createRun();
                        skillRun.setText("• " + skill.getOrDefault("name", "") + ": " + skill.getOrDefault("level", ""));
                    }
                }
            }

            // 荣誉奖项
            if (content.containsKey("honors") && content.get("honors") != null) {
                XWPFParagraph honorsTitlePara = document.createParagraph();
                honorsTitlePara.setSpacingBefore(200);
                honorsTitlePara.setSpacingAfter(100);
                XWPFRun honorsTitleRun = honorsTitlePara.createRun();
                honorsTitleRun.setText("荣誉奖项");
                honorsTitleRun.setBold(true);
                honorsTitleRun.setFontSize(16);

                java.util.List<Map<String, Object>> honors = (java.util.List<Map<String, Object>>) content.get("honors");
                if (honors != null) {
                    for (Map<String, Object> honor : honors) {
                        XWPFParagraph honorPara = document.createParagraph();
                        XWPFRun honorRun = honorPara.createRun();
                        honorRun.setText("• " + honor.getOrDefault("name", "") + " (" + honor.getOrDefault("date", "") + ")");
                    }
                }
            }

            // 个人作品
            if (content.containsKey("portfolio") && content.get("portfolio") != null) {
                XWPFParagraph portfolioTitlePara = document.createParagraph();
                portfolioTitlePara.setSpacingBefore(200);
                portfolioTitlePara.setSpacingAfter(100);
                XWPFRun portfolioTitleRun = portfolioTitlePara.createRun();
                portfolioTitleRun.setText("个人作品");
                portfolioTitleRun.setBold(true);
                portfolioTitleRun.setFontSize(16);

                java.util.List<Map<String, Object>> portfolio = (java.util.List<Map<String, Object>>) content.get("portfolio");
                if (portfolio != null) {
                    for (Map<String, Object> item : portfolio) {
                        XWPFParagraph itemPara = document.createParagraph();
                        XWPFRun itemRun = itemPara.createRun();
                        itemRun.setText("• " + item.getOrDefault("name", ""));
                        itemRun.setBold(true);

                        XWPFParagraph itemDescPara = document.createParagraph();
                        XWPFRun itemDescRun = itemDescPara.createRun();
                        itemDescRun.setText((String) item.getOrDefault("description", ""));

                        XWPFParagraph itemLinkPara = document.createParagraph();
                        XWPFRun itemLinkRun = itemLinkPara.createRun();
                        itemLinkRun.setText((String) item.getOrDefault("link", ""));
                        itemLinkRun.setItalic(true);
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

    @SuppressWarnings("unchecked")
    private String renderProjects(Map<String, Object> content) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>\n");
        html.append("<h2 class='section-title'>项目经历</h2>\n");

        java.util.List<Map<String, Object>> projects = (java.util.List<Map<String, Object>>) content.get("projects");
        if (projects != null) {
            for (Map<String, Object> proj : projects) {
                html.append("<div class='item'>\n");
                html.append("<div class='item-header'>\n");
                html.append("<div class='item-title'>").append(proj.getOrDefault("name", "")).append("</div>\n");
                html.append("<div class='item-period'>").append(proj.getOrDefault("period", "")).append("</div>\n");
                html.append("</div>\n");
                html.append("<div class='item-description'>").append(proj.getOrDefault("description", "")).append("</div>\n");
                html.append("</div>\n");
            }
        }

        html.append("</div>\n");
        return html.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderSkills(Map<String, Object> content) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>\n");
        html.append("<h2 class='section-title'>专业技能</h2>\n");

        java.util.List<Map<String, Object>> skills = (java.util.List<Map<String, Object>>) content.get("skills");
        if (skills != null) {
            for (Map<String, Object> skill : skills) {
                html.append("<div class='item'>\n");
                html.append("<div class='item-title'>").append(skill.getOrDefault("name", ""))
                    .append(" - ").append(skill.getOrDefault("level", "")).append("</div>\n");
                html.append("</div>\n");
            }
        }

        html.append("</div>\n");
        return html.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderHonors(Map<String, Object> content) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>\n");
        html.append("<h2 class='section-title'>荣誉奖项</h2>\n");

        java.util.List<Map<String, Object>> honors = (java.util.List<Map<String, Object>>) content.get("honors");
        if (honors != null) {
            for (Map<String, Object> honor : honors) {
                html.append("<div class='item'>\n");
                html.append("<div class='item-header'>\n");
                html.append("<div class='item-title'>").append(honor.getOrDefault("name", "")).append("</div>\n");
                html.append("<div class='item-period'>").append(honor.getOrDefault("date", "")).append("</div>\n");
                html.append("</div>\n");
                html.append("</div>\n");
            }
        }

        html.append("</div>\n");
        return html.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderPortfolio(Map<String, Object> content) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='section'>\n");
        html.append("<h2 class='section-title'>个人作品</h2>\n");

        java.util.List<Map<String, Object>> portfolio = (java.util.List<Map<String, Object>>) content.get("portfolio");
        if (portfolio != null) {
            for (Map<String, Object> item : portfolio) {
                html.append("<div class='item'>\n");
                html.append("<div class='item-title'>").append(item.getOrDefault("name", "")).append("</div>\n");
                html.append("<div class='item-description'>").append(item.getOrDefault("description", "")).append("</div>\n");
                if (item.containsKey("link")) {
                    html.append("<div class='item-link'><a href='").append(item.get("link")).append("'>").append(item.get("link")).append("</a></div>\n");
                }
                html.append("</div>\n");
            }
        }

        html.append("</div>\n");
        return html.toString();
    }
}
