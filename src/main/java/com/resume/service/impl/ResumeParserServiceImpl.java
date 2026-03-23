package com.resume.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.response.ParseResumeResponse;
import com.resume.service.ResumeParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

/**
 * 简历解析服务实现
 * 使用Qwen2.5-72B进行简历解析
 */
@Slf4j
@Service
@Profile("!test")
public class ResumeParserServiceImpl implements ResumeParserService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public ResumeParserServiceImpl(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public ParseResumeResponse parse(String fileContent) {
        return parse(fileContent, "zh");
    }

    @Override
    public ParseResumeResponse parse(String fileContent, String language) {
        log.info("开始解析简历，内容长度: {}, 语言: {}", fileContent.length(), language);

        String userPrompt = buildParsePrompt(fileContent, language);

        try {
            String response = chatModel.call(userPrompt);

            log.info("AI解析结果: {}", response);

            String cleanedJson = extractJsonFromResponse(response);

            ParseResumeResponse.ResumeContent content = objectMapper.readValue(
                    cleanedJson,
                    new TypeReference<ParseResumeResponse.ResumeContent>() {}
            );
            content.setLanguage("en".equals(language) ? "en" : "zh");

            return new ParseResumeResponse("从导入的简历", content);

        } catch (Exception e) {
            log.error("简历解析失败", e);
            throw new RuntimeException("简历解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建解析提示词
     */
    private String buildParsePrompt(String fileContent, String language) {
        boolean isEn = "en".equals(language);
        String langInstruction = isEn
            ? "The resume is in English. Extract all fields in English as they appear."
            : "简历为中文，请用中文提取所有字段内容。";

        return String.format("""
            请解析以下简历内容，返回JSON格式。%s

            {
              "name": "姓名/Full Name",
              "email": "邮箱/Email",
              "phone": "电话/Phone",
              "summary": "个人简介/Professional Summary",
              "experience": [
                {
                  "company": "公司名称/Company",
                  "position": "职位/Position",
                  "period": "时间段/Period（如：2020-2023）",
                  "description": "工作描述/Description"
                }
              ],
              "education": [
                {
                  "school": "学校名称/School",
                  "major": "专业/Major",
                  "degree": "学位/Degree（如：本科/Bachelor）",
                  "period": "时间段/Period（如：2016-2020）"
                }
              ],
              "projects": [
                {
                  "name": "项目名称/Project Name",
                  "period": "项目时间/Period",
                  "description": "项目描述/Description",
                  "technologies": ["技术1/Tech1", "技术2/Tech2"]
                }
              ],
              "skills": [
                {
                  "name": "技能名称/Skill Name",
                  "level": 熟练度/Proficiency（0-100的整数/integer）
                }
              ],
              "honors": [
                {
                  "name": "奖项名称/Award Name",
                  "date": "获奖时间/Date",
                  "description": "奖项描述/Description"
                }
              ],
              "works": [
                {
                  "name": "作品名称/Work Name",
                  "description": "作品描述/Description",
                  "url": "作品链接/URL"
                }
              ]
            }

            简历内容/Resume Content：
            %s
            """, langInstruction, fileContent);
    }

    /**
     * 从AI响应中提取JSON部分
     * 处理可能包含 ```json 标记的情况
     */
    private String extractJsonFromResponse(String response) {
        // 去除前后空白
        response = response.trim();

        // 检查是否包含 ```json 标记
        if (response.startsWith("```json")) {
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1) {
                return response.substring(startIndex, endIndex + 1);
            }
        }

        // 检查是否包含 ``` 标记
        if (response.startsWith("```")) {
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1) {
                return response.substring(startIndex, endIndex + 1);
            }
        }

        return response;
    }
}
