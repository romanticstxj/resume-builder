package com.resume.service.impl;

import com.resume.dto.response.ParseResumeResponse;
import com.resume.service.ResumeParserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Mock implementation of ResumeParserService for test profile.
 * Returns a demo ParseResumeResponse without calling external AI.
 */
@Service
@Profile("test")
public class ResumeParserServiceMock implements ResumeParserService {

    @Override
    public ParseResumeResponse parse(String fileContent) {
        return parse(fileContent, "zh");
    }

    @Override
    public ParseResumeResponse parse(String fileContent, String language) {
        ParseResumeResponse.ResumeContent content = new ParseResumeResponse.ResumeContent();
        content.setLanguage(language != null ? language : "zh");
        content.setName("en".equals(language) ? "John Doe" : "张三");
        content.setEmail("zhangsan@example.com");
        content.setPhone("13800000000");
        content.setSummary("en".equals(language) ? "A demo resume summary for testing." : "这是一个用于测试的简历摘要。");

        ParseResumeResponse.ExperienceItem exp = new ParseResumeResponse.ExperienceItem();
        exp.setCompany("en".equals(language) ? "Example Corp" : "示例公司");
        exp.setPosition("en".equals(language) ? "Software Engineer" : "软件工程师");
        exp.setPeriod("2020-2023");
        exp.setDescription("en".equals(language) ? "Backend development and system design." : "负责后端开发与系统设计。");

        ArrayList<ParseResumeResponse.ExperienceItem> exps = new ArrayList<>();
        exps.add(exp);
        content.setExperience(exps);

        content.setEducation(new ArrayList<>());
        content.setProjects(new ArrayList<>());
        content.setSkills(new ArrayList<>());
        content.setHonors(new ArrayList<>());
        content.setWorks(new ArrayList<>());

        return new ParseResumeResponse("en".equals(language) ? "Demo Resume" : "Demo 简历", content);
    }
}

