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
        log.info("解析提示词: {}", userPrompt);

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
            ? "The resume is in English."
            : "简历为中文。";

        return String.format("""
            你是一个简历解析工具。请将以下简历原文内容原封不动地搬运到 JSON 结构中，禁止修改、润色、翻译或补充任何内容，所有字段值必须与原文完全一致，但因为适用oci技术导致skills，experience，projects，works的内容里有些换行没了，可以按照你的理解帮我补全换行。%s

            只返回如下 JSON，不要输出任何解释：

            {
              "name": "原文姓名",
              "email": "原文邮箱",
              "phone": "原文电话",
              "summary": "原文个人简介，原文没有则为空字符串",
              "skills": [
                { "name": "原文技能名称" }
              ],
              "experience": [
                {
                  "company": "原文公司名称",
                  "position": "原文职位",
                  "period": "原文时间段",
                  "description": "原文工作描述"
                }
              ],
              "projects": [
                {
                  "name": "原文项目名称",
                  "company": "原文项目所属公司或组织，原文没有则为空字符串",
                  "period": "原文项目时间",
                  "description": "原文项目描述",
                  "technologies": ["原文技术1", "原文技术2"]
                }
              ],
              "works": [
                {
                  "name": "原文作品名称",
                  "description": "原文作品描述",
                  "url": "原文链接"
                }
              ],
              "education": [
                {
                  "school": "原文学校名称",
                  "major": "原文专业",
                  "degree": "原文学位",
                  "period": "原文时间段"
                }
              ],
              "honors": [
                {
                  "name": "原文奖项名称",
                  "date": "原文获奖时间",
                  "description": "原文奖项描述"
                }
              ]
            }

            简历原文：
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
