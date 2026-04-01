package com.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.entity.Resume;
import com.resume.enums.ResumeSectionEnum;
import com.resume.service.ResumeRenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历渲染服务实现
 *
 * 模块渲染器通过 ResumeSectionEnum 驱动，新增模块只需：
 *   1. 在 ResumeSectionEnum 添加枚举值
 *   2. 在 buildRenderers() 中注册对应渲染方法
 */
@Service
@RequiredArgsConstructor
public class ResumeRenderServiceImpl implements ResumeRenderService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── 渲染上下文，每次渲染时构建 ──────────────────────────────────────────
    private record RenderContext(
            Map<String, Object> content,
            Map<String, Object> theme,
            Map<String, Object> sections,
            String lang
    ) {}

    // ── 渲染器类型 ──────────────────────────────────────────────────────────
    @FunctionalInterface
    private interface SectionRenderer {
        /** 返回该模块的 HTML 片段，若不应渲染则返回 null */
        String render(RenderContext ctx);
    }

    // ── 枚举 key → 渲染器 Map ───────────────────────────────────────────────
    private final Map<String, SectionRenderer> renderers = buildRenderers();

    private Map<String, SectionRenderer> buildRenderers() {
        Map<String, SectionRenderer> map = new HashMap<>();
        map.put(ResumeSectionEnum.HEADER.getKey(),     this::renderHeader);
        map.put(ResumeSectionEnum.SUMMARY.getKey(),    this::renderSummary);
        map.put(ResumeSectionEnum.SKILLS.getKey(),     this::renderSkills);
        map.put(ResumeSectionEnum.EXPERIENCE.getKey(), this::renderExperience);
        map.put(ResumeSectionEnum.PROJECTS.getKey(),   this::renderProjects);
        map.put(ResumeSectionEnum.WORKS.getKey(),      this::renderWorks);
        map.put(ResumeSectionEnum.EDUCATION.getKey(),  this::renderEducation);
        map.put(ResumeSectionEnum.HONORS.getKey(),     this::renderHonors);
        return map;
    }

    // ── 公开接口 ────────────────────────────────────────────────────────────

    @Override
    public String renderToHtml(Resume resume, String templateLayout, String themeConfig, String sectionConfig, String sectionOrder) {
        try {
            Map<String, Object> content  = parseJson(resume.getContent());
            Map<String, Object> theme    = parseJson(themeConfig);
            Map<String, Object> sections = parseJson(sectionConfig);
            String lang = String.valueOf(content.getOrDefault("language", "zh"));

            List<String> sectionOrderList = parseSectionOrder(sectionOrder);
            if (sectionOrderList == null || sectionOrderList.isEmpty()) {
                sectionOrderList = ResumeSectionEnum.DEFAULT_ORDER;
            }

            RenderContext ctx = new RenderContext(content, theme, sections, lang);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html lang='zh-CN'>\n<head>\n");
            html.append("<meta charset='UTF-8'>\n");
            html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
            html.append("<title>").append(resume.getTitle()).append("</title>\n");
            html.append("<style>\n").append(getThemeStyles(theme)).append("</style>\n");
            html.append("</head>\n<body>\n<div class='resume-container'>\n");

            for (String key : sectionOrderList) {
                SectionRenderer renderer = renderers.get(key);
                if (renderer != null) {
                    String fragment = renderer.render(ctx);
                    if (fragment != null) html.append(fragment);
                }
            }

            html.append("</div>\n</body>\n</html>");
            return html.toString();

        } catch (Exception e) {
            throw new RuntimeException("渲染简历失败", e);
        }
    }

    @Override
    public byte[] exportToPdf(Resume resume, String templateLayout, String themeConfig, String sectionConfig, String sectionOrder) {
        return renderToHtml(resume, templateLayout, themeConfig, sectionConfig, sectionOrder).getBytes();
    }

    @Override
    public byte[] exportToWord(Resume resume, String templateLayout, String themeConfig, String sectionConfig, String sectionOrder) {
        try {
            Map<String, Object> content  = parseJson(resume.getContent());
            Map<String, Object> theme    = parseJson(themeConfig);
            Map<String, Object> sections = parseJson(sectionConfig);
            String lang = String.valueOf(content.getOrDefault("language", "zh"));

            List<String> sectionOrderList = parseSectionOrder(sectionOrder);
            if (sectionOrderList == null || sectionOrderList.isEmpty()) {
                sectionOrderList = ResumeSectionEnum.DEFAULT_ORDER;
            }

            RenderContext ctx = new RenderContext(content, theme, sections, lang);

            String primary = (String) theme.getOrDefault("primaryColor", "#2c3e50");
            String textColor = (String) theme.getOrDefault("textColor", "#333333");
            String font = (String) theme.getOrDefault("fontFamily", "Arial, sans-serif");
            Object fsRaw = theme.getOrDefault("fontSize", "14px");
            String fontSize = (fsRaw instanceof Number) ? fsRaw + "px" : fsRaw.toString();

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' ");
            html.append("xmlns:w='urn:schemas-microsoft-com:office:word' ");
            html.append("xmlns='http://www.w3.org/TR/REC-html40'>\n");
            html.append("<head><meta charset='UTF-8'>\n");
            html.append("<style>\n");
            // Word page margins — tighter than default
            html.append("@page { margin: 1.5cm 1.8cm; }\n");
            html.append("body { font-family: '").append(font).append("'; font-size: ").append(fontSize)
                .append("; color: ").append(textColor).append("; margin: 0; padding: 0; }\n");
            // Word-compatible watermark via background SVG on body
            String svgWatermark = buildSvgWatermark(primary);
            html.append("body { background-image: url('").append(svgWatermark).append("'); background-repeat: repeat; background-size: 200px 200px; }\n");
            html.append(".resume-container { background: white; padding: 0; }\n");
            html.append(".resume-header { margin-bottom: 24pt; }\n");
            // Photo: fixed pixel size via inline style, Word respects width/height attributes
            html.append(".header-main { width: 100%; }\n");
            html.append(".header-text { display: inline-block; vertical-align: top; width: 75%; }\n");
            html.append(".header-photo-cell { display: inline-block; vertical-align: top; width: 20%; text-align: right; }\n");
            html.append(".name { font-size: 24pt; font-weight: bold; color: ").append(primary).append("; margin-bottom: 6pt; }\n");
            html.append(".contact-info { font-size: 10pt; color: #666; }\n");
            html.append(".section { margin-bottom: 18pt; }\n");
            html.append(".section-title { font-size: 14pt; font-weight: bold; color: ").append(primary)
                .append("; border-bottom: 1.5pt solid ").append(primary).append("; padding-bottom: 4pt; margin-bottom: 10pt; }\n");
            html.append(".item { margin-bottom: 10pt; }\n");
            html.append(".item-header { width: 100%; }\n");
            html.append(".item-title-cell { display: inline-block; width: 70%; font-size: 11pt; font-weight: bold; }\n");
            html.append(".item-period-cell { display: inline-block; width: 28%; text-align: right; font-size: 10pt; color: #666; }\n");
            html.append(".item-title { font-size: 11pt; font-weight: bold; }\n");
            html.append(".item-role { font-size: 10pt; font-weight: normal; color: #555; }\n");
            html.append(".item-role-line { font-size: 10pt; font-style: italic; color: #666; margin: 2pt 0 4pt; display: block; }\n");
            html.append(".item-period { font-size: 10pt; color: #666; }\n");
            html.append(".item-description { font-size: 10pt; line-height: 1.5; margin-top: 4pt; color: ").append(textColor).append("; }\n");
            html.append(".item-tech { margin-top: 4pt; }\n");
            html.append(".item-link { margin-top: 3pt; font-size: 10pt; }\n");
            html.append(".skills-tags-wrap { }\n");
            html.append(".skill-tag { display: inline-block; padding: 2pt 6pt; background: #f0f4ff; color: ").append(primary).append("; border: 1pt solid #c8d8ff; border-radius: 3pt; font-size: 9pt; margin: 2pt; }\n");
            html.append("</style></head>\n<body>\n<div class='resume-container'>\n");

            for (String key : sectionOrderList) {
                SectionRenderer renderer = renderers.get(key);
                if (renderer != null) {
                    String fragment = renderForWord(key, ctx);
                    if (fragment != null) html.append(fragment);
                }
            }

            html.append("</div>\n</body>\n</html>");
            return html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("导出 Word 失败", e);
        }
    }

    /**
     * Word-specific rendering: uses table-based layout for header (photo sizing),
     * and inline-block cells instead of flex (Word doesn't support flexbox).
     */
    private String renderForWord(String key, RenderContext ctx) {
        return switch (key) {
            case "header" -> renderHeaderWord(ctx);
            default -> {
                SectionRenderer r = renderers.get(key);
                yield r != null ? r.render(ctx) : null;
            }
        };
    }

    private String renderHeaderWord(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "header")) return null;
        String photo = (String) ctx.content().get("photo");
        String email = (String) ctx.content().get("email");
        String phone = (String) ctx.content().get("phone");
        String city = (String) ctx.content().get("city");
        String jobTarget = (String) ctx.content().get("jobTarget");

        java.util.List<String> line1 = new java.util.ArrayList<>();
        if (email != null && !email.isBlank()) line1.add(email);
        if (phone != null && !phone.isBlank()) line1.add(phone);
        java.util.List<String> line2 = new java.util.ArrayList<>();
        if (city != null && !city.isBlank()) line2.add(city);
        if (jobTarget != null && !jobTarget.isBlank()) line2.add(jobTarget);

        StringBuilder contactHtml = new StringBuilder();
        if (!line1.isEmpty()) contactHtml.append(String.join(" | ", line1));
        if (!line1.isEmpty() && !line2.isEmpty()) contactHtml.append("<br/>");
        if (!line2.isEmpty()) contactHtml.append(String.join(" | ", line2));

        StringBuilder sb = new StringBuilder("<div class='resume-header'>\n");
        if (photo != null && !photo.isBlank()) {
            sb.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>\n");
            sb.append("<td valign='top'>\n");
            sb.append("<div class='name'>").append(ctx.content().getOrDefault("name", "")).append("</div>\n");
            if (contactHtml.length() > 0) {
                sb.append("<div class='contact-info'>").append(contactHtml).append("</div>\n");
            }
            sb.append("</td>\n");
            sb.append("<td width='80' valign='top' align='right'>\n");
            sb.append("<img src='").append(photo)
              .append("' width='70' height='88' style='border:1px solid #e0e0e0;' />\n");
            sb.append("</td></tr></table>\n");
        } else {
            sb.append("<div class='name'>").append(ctx.content().getOrDefault("name", "")).append("</div>\n");
            if (contactHtml.length() > 0) {
                sb.append("<div class='contact-info'>").append(contactHtml).append("</div>\n");
            }
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    /**
     * Builds a tiny SVG as a data URL for use as a repeating background pattern.
     * Word supports background-image with data URLs for simple SVGs.
     * The pattern is a subtle diamond/dot grid in the primary color at very low opacity.
     */
    private String buildSvgWatermark(String primaryColor) {
        // Extract hex without # for SVG fill
        String hex = primaryColor.startsWith("#") ? primaryColor.substring(1) : primaryColor;
        // Small repeating diamond pattern — 40x40 tile
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40'>" +
                     "<circle cx='20' cy='20' r='1.5' fill='%23" + hex + "' opacity='0.08'/>" +
                     "<circle cx='0' cy='0' r='1.5' fill='%23" + hex + "' opacity='0.08'/>" +
                     "<circle cx='40' cy='0' r='1.5' fill='%23" + hex + "' opacity='0.08'/>" +
                     "<circle cx='0' cy='40' r='1.5' fill='%23" + hex + "' opacity='0.08'/>" +
                     "<circle cx='40' cy='40' r='1.5' fill='%23" + hex + "' opacity='0.08'/>" +
                     "</svg>";
        // Encode as data URL (spaces and special chars already handled above with %23)
        return "data:image/svg+xml," + svg;
    }

    // ── 各模块渲染器 ────────────────────────────────────────────────────────

    private String renderHeader(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "header")) return null;
        Map<String, Object> cfg = sectionCfg(ctx.sections(), "header");
        String align = "center".equals(cfg.get("style")) ? "text-align:center;" : "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='resume-header' style='").append(align).append("'>\n");
        sb.append("<div class='header-main'>\n");
        sb.append("<div class='header-text'>\n");
        sb.append("<h1 class='name'>").append(ctx.content().getOrDefault("name", "")).append("</h1>\n");
        String email = (String) ctx.content().get("email");
        String phone = (String) ctx.content().get("phone");
        String city = (String) ctx.content().get("city");
        String jobTarget = (String) ctx.content().get("jobTarget");

        // Line 1: email | phone
        java.util.List<String> line1 = new java.util.ArrayList<>();
        if (email != null && !email.isBlank()) line1.add(email);
        if (phone != null && !phone.isBlank()) line1.add(phone);
        // Line 2: city | jobTarget
        java.util.List<String> line2 = new java.util.ArrayList<>();
        if (city != null && !city.isBlank()) line2.add(city);
        if (jobTarget != null && !jobTarget.isBlank()) line2.add(jobTarget);

        if (!line1.isEmpty() || !line2.isEmpty()) {
            sb.append("<div class='contact-info'>");
            if (!line1.isEmpty()) sb.append(String.join(" | ", line1));
            if (!line1.isEmpty() && !line2.isEmpty()) sb.append("<br/>");
            if (!line2.isEmpty()) sb.append(String.join(" | ", line2));
            sb.append("</div>\n");
        }
        sb.append("</div>\n"); // header-text
        String photo = (String) ctx.content().get("photo");
        if (photo != null && !photo.isBlank()) {
            sb.append("<img src='").append(photo).append("' class='header-photo' />\n");
        }
        sb.append("</div>\n"); // header-main
        sb.append("</div>\n");
        return sb.toString();
    }

    private String renderSummary(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "summary")) return null;
        // Support both "summary" and legacy "personalSummary" field names
        Object val = ctx.content().get("summary");
        if (val == null || val.toString().isBlank()) {
            val = ctx.content().get("personalSummary");
        }
        if (val == null || val.toString().isBlank()) return null;
        String title = ResumeSectionEnum.SUMMARY.getLabel(ctx.lang());
        return "<div class='section'>\n<h2 class='section-title'>" + title + "</h2>\n" +
               "<p>" + val + "</p>\n</div>\n";
    }

    @SuppressWarnings("unchecked")
    private String renderSkills(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "skills")) return null;
        String title = ResumeSectionEnum.SKILLS.getLabel(ctx.lang());

        // skillsText is now HTML from the rich text editor
        Object skillsTextRaw = ctx.content().get("skillsText");
        // Legacy fallback: skills array
        List<Map<String, Object>> skillsList = (List<Map<String, Object>>) ctx.content().get("skills");

        String html = null;
        if (skillsTextRaw != null && !skillsTextRaw.toString().isBlank()) {
            html = skillsTextRaw.toString();
        } else if (skillsList != null && !skillsList.isEmpty()) {
            // Legacy: render as comma-separated plain text
            html = "<p>" + skillsList.stream()
                    .map(s -> String.valueOf(s.getOrDefault("name", "")))
                    .filter(s -> !s.isBlank())
                    .collect(java.util.stream.Collectors.joining(", ")) + "</p>";
        }

        if (html == null || html.isBlank()) return null;

        return "<div class='section'>\n<h2 class='section-title'>" + title + "</h2>\n" +
               "<div class='item-description'>" + html + "</div>\n</div>\n";
    }

    @SuppressWarnings("unchecked")
    private String renderExperience(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "experience")) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) ctx.content().get("experience");
        if (list == null || list.isEmpty()) return null;
        String title = ResumeSectionEnum.EXPERIENCE.getLabel(ctx.lang());
        StringBuilder sb = new StringBuilder("<div class='section'>\n<h2 class='section-title'>").append(title).append("</h2>\n");
        for (Map<String, Object> exp : list) {
            sb.append("<div class='item'>\n");
            sb.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>");
            sb.append("<td><strong class='item-title'>").append(exp.getOrDefault("company", "")).append("</strong></td>");
            sb.append("<td align='right'><span class='item-period'>").append(exp.getOrDefault("period", "")).append("</span></td>");
            sb.append("</tr></table>\n");
            Object position = exp.get("position");
            if (position != null && !position.toString().isBlank()) {
                sb.append("<div class='item-role-line'>").append(position).append("</div>\n");
            }
            Object expRole = exp.get("role");
            if (expRole != null && !expRole.toString().isBlank()) {
                sb.append("<div class='item-role-line'>").append(expRole).append("</div>\n");
            }
            Object desc = exp.get("description");
            if (desc != null && !desc.toString().isBlank()) {
                sb.append("<div class='item-description'>").append(desc).append("</div>\n");
            }
            sb.append("</div>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderProjects(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "projects")) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) ctx.content().get("projects");
        if (list == null || list.isEmpty()) return null;
        String title = ResumeSectionEnum.PROJECTS.getLabel(ctx.lang());
        StringBuilder sb = new StringBuilder("<div class='section'>\n<h2 class='section-title'>").append(title).append("</h2>\n");
        for (Map<String, Object> proj : list) {
            sb.append("<div class='item'>\n");
            sb.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>");
            // name + optional role
            String projName = String.valueOf(proj.getOrDefault("name", ""));
            Object roleObj = proj.get("title");
            sb.append("<td><strong class='item-title'>").append(projName).append("</strong></td>");
            sb.append("<td align='right'><span class='item-period'>").append(proj.getOrDefault("period", "")).append("</span></td>");
            sb.append("</tr></table>\n");
            Object company = proj.get("company");
            boolean hasCompany = company != null && !company.toString().isBlank();
            boolean hasRole = roleObj != null && !roleObj.toString().isBlank();
            if (hasCompany || hasRole) {
                sb.append("<div class='item-role-line'>");
                if (hasCompany) sb.append(company);
                if (hasCompany && hasRole) sb.append(" · ");
                if (hasRole) sb.append(roleObj);
                sb.append("</div>\n");
            }
            Object desc = proj.get("description");
            if (desc != null && !desc.toString().isBlank()) {
                sb.append("<div class='item-description'>").append(desc).append("</div>\n");
            }
            // technologies
            Object techObj = proj.get("technologies");
            if (techObj instanceof List) {
                List<?> techs = (List<?>) techObj;
                if (!techs.isEmpty()) {
                    sb.append("<div class='item-tech'>");
                    for (Object t : techs) sb.append("<span class='skill-tag'>").append(t).append("</span> ");
                    sb.append("</div>\n");
                }
            }
            sb.append("</div>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderWorks(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "works")) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) ctx.content().get("works");
        if (list == null || list.isEmpty()) return null;
        String title = ResumeSectionEnum.WORKS.getLabel(ctx.lang());
        StringBuilder sb = new StringBuilder("<div class='section'>\n<h2 class='section-title'>").append(title).append("</h2>\n");
        for (Map<String, Object> item : list) {
            sb.append("<div class='item'>\n");
            sb.append("<strong class='item-title'>").append(item.getOrDefault("name", "")).append("</strong>\n");
            Object workRole = item.get("role");
            if (workRole != null && !workRole.toString().isBlank()) {
                sb.append("<div class='item-role-line'>").append(workRole).append("</div>\n");
            }
            Object desc = item.get("description");
            if (desc != null && !desc.toString().isBlank()) {
                sb.append("<div class='item-description'>").append(desc).append("</div>\n");
            }
            Object url = item.get("url");
            if (url != null && !url.toString().isBlank()) {
                sb.append("<div class='item-link'><a href='").append(url).append("'>").append(url).append("</a></div>\n");
            }
            sb.append("</div>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderEducation(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "education")) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) ctx.content().get("education");
        if (list == null || list.isEmpty()) return null;
        String title = ResumeSectionEnum.EDUCATION.getLabel(ctx.lang());
        StringBuilder sb = new StringBuilder("<div class='section'>\n<h2 class='section-title'>").append(title).append("</h2>\n");
        for (Map<String, Object> edu : list) {
            sb.append("<div class='item'>\n");
            sb.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>");
            sb.append("<td><strong class='item-title'>").append(edu.getOrDefault("school", "")).append(" — ").append(edu.getOrDefault("major", "")).append("</strong></td>");
            sb.append("<td align='right'><span class='item-period'>").append(edu.getOrDefault("period", "")).append("</span></td>");
            sb.append("</tr></table>\n");
            Object degree = edu.get("degree");
            if (degree != null && !degree.toString().isBlank()) {
                sb.append("<div class='item-description'>").append(degree).append("</div>\n");
            }
            sb.append("</div>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderHonors(RenderContext ctx) {
        if (!shouldShow(ctx.sections(), "honors")) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) ctx.content().get("honors");
        if (list == null || list.isEmpty()) return null;
        String title = ResumeSectionEnum.HONORS.getLabel(ctx.lang());
        StringBuilder sb = new StringBuilder("<div class='section'>\n<h2 class='section-title'>").append(title).append("</h2>\n");
        for (Map<String, Object> honor : list) {
            sb.append("<div class='item'>\n");
            sb.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>");
            sb.append("<td><strong class='item-title'>").append(honor.getOrDefault("name", "")).append("</strong></td>");
            sb.append("<td align='right'><span class='item-period'>").append(honor.getOrDefault("date", "")).append("</span></td>");
            sb.append("</tr></table>\n");
            Object desc = honor.get("description");
            if (desc != null && !desc.toString().isBlank()) {
                sb.append("<div class='item-description'>").append(desc).append("</div>\n");
            }
            sb.append("</div>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    // ── 工具方法 ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try { return objectMapper.readValue(json, Map.class); }
        catch (Exception e) { return new HashMap<>(); }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseSectionOrder(String json) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readValue(json, List.class); }
        catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sectionCfg(Map<String, Object> sections, String key) {
        Object v = sections.get(key);
        return (v instanceof Map) ? (Map<String, Object>) v : new HashMap<>();
    }

    private boolean shouldShow(Map<String, Object> sections, String key) {
        return sectionCfg(sections, key).get("show") != Boolean.FALSE;
    }

    private String getThemeStyles(Map<String, Object> theme) {
        String primary  = (String) theme.getOrDefault("primaryColor", "#2c3e50");
        String text     = (String) theme.getOrDefault("textColor", "#333333");
        String font     = (String) theme.getOrDefault("fontFamily", "Arial, sans-serif");
        Object fsRaw    = theme.getOrDefault("fontSize", "14px");
        String fontSize = (fsRaw instanceof Number) ? fsRaw + "px" : fsRaw.toString();

        return "* { margin: 0; padding: 0; box-sizing: border-box; }\n" +
               "body { font-family: '" + font + "'; font-size: " + fontSize + "; color: " + text + "; background: #f5f5f5; padding: 20px; }\n" +
               ".resume-container { max-width: 960px; margin: 0 auto; background: white; padding: 48px 56px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); position: relative; overflow: hidden; }\n" +
               // watermark
               ".resume-container::before { content: 'RESUME'; position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%) rotate(-30deg); font-size: 120px; font-weight: 900; color: " + primary + "; opacity: 0.03; pointer-events: none; white-space: nowrap; letter-spacing: 12px; z-index: 0; }\n" +
               ".resume-header { margin-bottom: 30px; position: relative; z-index: 1; }\n" +
               ".header-main { display: flex; justify-content: space-between; align-items: flex-start; }\n" +
               ".header-text { flex: 1; }\n" +
               ".header-photo { width: 90px; height: 112px; object-fit: cover; border-radius: 4px; margin-left: 24px; flex-shrink: 0; border: 1px solid #e0e0e0; }\n" +
               ".name { font-size: 32px; font-weight: 700; color: " + primary + "; margin-bottom: 10px; }\n" +
               ".contact-info { font-size: 14px; color: #666; }\n" +
               ".section { margin-bottom: 25px; position: relative; z-index: 1; }\n" +
               ".section-title { font-size: 18px; font-weight: 600; color: " + primary + "; padding-bottom: 8px; margin-bottom: 15px; border-bottom: 2px solid " + primary + "; }\n" +
               ".item { margin-bottom: 15px; }\n" +
               ".item-header { display: flex; justify-content: space-between; margin-bottom: 5px; }\n" +
               ".item-title { font-size: 16px; font-weight: bold; }\n" +
               ".item-role { font-size: 13px; font-weight: normal; color: #555; }\n" +
               ".item-role-line { font-size: 13px; font-style: italic; color: #666; margin: 2px 0 4px; }\n" +
               ".item-period { font-size: 13px; color: #666; }\n" +
               ".item-description { font-size: 14px; line-height: 1.6; margin-top: 4px; color: " + text + "; }\n" +
               ".item-description ul, .item-description ol { padding-left: 14px; margin: 4px 0; }\n" +
               ".item-description li { margin: 2px 0; }\n" +
               ".item-description p { margin: 0 0 4px; }\n" +
               ".item-tech { margin-top: 6px; }\n" +
               ".item-link { margin-top: 4px; font-size: 13px; }\n" +
               ".item-link a { color: " + primary + "; }\n" +
               ".skills-tags-wrap { display: flex; flex-wrap: wrap; gap: 8px; }\n" +
               ".skill-tag { display: inline-block; padding: 3px 10px; background: #f0f4ff; color: " + primary + "; border: 1px solid #c8d8ff; border-radius: 4px; font-size: 13px; }\n";
    }
}
