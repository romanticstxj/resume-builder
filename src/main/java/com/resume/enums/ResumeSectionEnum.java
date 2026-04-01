package com.resume.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 简历模块注册表
 * 新增模块只需在此枚举中添加一个条目，渲染逻辑在 ResumeRenderServiceImpl 中注册对应渲染器。
 * 枚举声明顺序即为默认渲染顺序。
 */
public enum ResumeSectionEnum {

    HEADER("header", "个人信息", "Contact"),
    SUMMARY("summary", "个人简介", "Professional Summary"),
    SKILLS("skills", "专业技能", "Skills"),
    EXPERIENCE("experience", "工作经历", "Work Experience"),
    PROJECTS("projects", "项目经历", "Projects"),
    WORKS("works", "个人作品", "Portfolio"),
    EDUCATION("education", "教育经历", "Education"),
    HONORS("honors", "荣誉证书", "Awards & Honors");

    private final String key;
    private final String labelZh;
    private final String labelEn;

    ResumeSectionEnum(String key, String labelZh, String labelEn) {
        this.key = key;
        this.labelZh = labelZh;
        this.labelEn = labelEn;
    }

    public String getKey() { return key; }
    public String getLabelZh() { return labelZh; }
    public String getLabelEn() { return labelEn; }

    public String getLabel(String lang) {
        return "en".equals(lang) ? labelEn : labelZh;
    }

    /** 默认渲染顺序，由枚举声明顺序自动推导 */
    public static final List<String> DEFAULT_ORDER =
            Arrays.stream(values()).map(ResumeSectionEnum::getKey).toList();

    public static Optional<ResumeSectionEnum> fromKey(String key) {
        return Arrays.stream(values())
                .filter(s -> s.key.equals(key))
                .findFirst();
    }
}
