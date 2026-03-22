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
        ParseResumeResponse.ResumeContent content = new ParseResumeResponse.ResumeContent();
        content.setName("张三");
        content.setEmail("zhangsan@example.com");
        content.setPhone("13800000000");
        content.setSummary("这是一个用于测试的简历摘要。");

        ParseResumeResponse.ExperienceItem exp = new ParseResumeResponse.ExperienceItem();
        exp.setCompany("示例公司");
        exp.setPosition("软件工程师");
        exp.setPeriod("2020-2023");
        exp.setDescription("负责后端开发与系统设计。");

        ArrayList<ParseResumeResponse.ExperienceItem> exps = new ArrayList<>();
        exps.add(exp);
        content.setExperience(exps);

        content.setEducation(new ArrayList<>());
        content.setProjects(new ArrayList<>());
        content.setSkills(new ArrayList<>());
        content.setPersonalSummary("个人总结示例。");
        content.setHonors(new ArrayList<>());
        content.setWorks(new ArrayList<>());

        return new ParseResumeResponse("Demo 简历", content);
    }
}

