package com.resume.dto.response;

import lombok.Data;

/**
 * 简历解析响应
 */
@Data
public class ParseResumeResponse {
    private String title;
    private ResumeContent content;

    public ParseResumeResponse(String title, ResumeContent content) {
        this.title = title;
        this.content = content;
    }

    @Data
    public static class ResumeContent {
        private String name;
        private String email;
        private String phone;
        private String summary;
        private java.util.List<ExperienceItem> experience;
        private java.util.List<EducationItem> education;
        private java.util.List<ProjectItem> projects;
        private java.util.List<SkillItem> skills;
        private String personalSummary;
        private java.util.List<HonorItem> honors;
        private java.util.List<WorkItem> works;
    }

    @Data
    public static class ExperienceItem {
        private String company;
        private String position;
        private String period;
        private String description;
    }

    @Data
    public static class EducationItem {
        private String school;
        private String major;
        private String degree;
        private String period;
    }

    @Data
    public static class ProjectItem {
        private String name;
        private String period;
        private String description;
        private java.util.List<String> technologies;
    }

    @Data
    public static class SkillItem {
        private String name;
        private Integer level;
    }

    @Data
    public static class HonorItem {
        private String name;
        private String date;
        private String description;
    }

    @Data
    public static class WorkItem {
        private String name;
        private String description;
        private String url;
    }
}
