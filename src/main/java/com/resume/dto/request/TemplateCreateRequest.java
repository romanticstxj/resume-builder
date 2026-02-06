package com.resume.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TemplateCreateRequest {
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @NotBlank(message = "模板分类不能为空")
    private String category;

    private String description;

    private String previewUrl;

    @NotNull(message = "模板内容不能为空")
    private Object content;

    private Boolean isPublic = true;

    private String layout;

    private String themeConfig;

    private String sectionConfig;

    private String sectionOrder;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getThemeConfig() {
        return themeConfig;
    }

    public void setThemeConfig(String themeConfig) {
        this.themeConfig = themeConfig;
    }

    public String getSectionConfig() {
        return sectionConfig;
    }

    public void setSectionConfig(String sectionConfig) {
        this.sectionConfig = sectionConfig;
    }

    public String getSectionOrder() {
        return sectionOrder;
    }

    public void setSectionOrder(String sectionOrder) {
        this.sectionOrder = sectionOrder;
    }
}


