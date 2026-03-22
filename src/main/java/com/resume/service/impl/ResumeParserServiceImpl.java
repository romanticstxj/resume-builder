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

    private static final String SYSTEM_PROMPT = """
        你是一位专业的简历解析专家。请仔细阅读用户提供的简历内容，
        并按照指定的JSON格式返回解析结果。

        要求：
        1. 准确提取姓名、邮箱、电话等基本信息
        2. 识别并整理工作经历，包含公司、职位、时间段、工作描述
        3. 识别并整理教育经历，包含学校、专业、学位、时间段
        4. 识别项目经历，提取项目名称、时间、描述、使用的技术栈
        5. 识别专业技能，包含技能名称和熟练度（0-100）
        6. 识别个人总结、荣誉奖项、个人作品
        7. 如果某些信息在简历中不存在，对应字段设为null或空数组
        8. 必须返回有效的JSON格式，不要包含其他解释性文字

        技术栈需要拆分为独立的技术名称列表，如 ["Vue", "React", "Node.js"]
        """;

    public ResumeParserServiceImpl(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public ParseResumeResponse parse(String fileContent) {
        log.info("开始解析简历，内容长度: {}", fileContent.length());

        String userPrompt = buildParsePrompt(fileContent);

        try {
            String response = chatModel.call(userPrompt);

            log.info("AI解析结果: {}", response);

            // 清理响应中的JSON部分（去除可能存在的markdown标记）
            String cleanedJson = extractJsonFromResponse(response);

            // 解析JSON为对象
            ParseResumeResponse.ResumeContent content = objectMapper.readValue(
                    cleanedJson,
                    new TypeReference<ParseResumeResponse.ResumeContent>() {}
            );

            return new ParseResumeResponse("从导入的简历", content);

        } catch (Exception e) {
            log.error("简历解析失败", e);
            throw new RuntimeException("简历解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建解析提示词
     */
    private String buildParsePrompt(String fileContent) {
        return String.format("""
            请解析以下简历内容，返回JSON格式：

            {
              "name": "姓名",
              "email": "邮箱",
              "phone": "电话",
              "summary": "个人简介",
              "experience": [
                {
                  "company": "公司名称",
                  "position": "职位",
                  "period": "时间段（如：2020-2023）",
                  "description": "工作描述"
                }
              ],
              "education": [
                {
                  "school": "学校名称",
                  "major": "专业",
                  "degree": "学位（如：本科、硕士）",
                  "period": "时间段（如：2016-2020）"
                }
              ],
              "projects": [
                {
                  "name": "项目名称",
                  "period": "项目时间",
                  "description": "项目描述",
                  "technologies": ["技术1", "技术2", "技术3"]
                }
              ],
              "skills": [
                {
                  "name": "技能名称",
                  "level": 熟练度（0-100的整数）
                }
              ],
              "personalSummary": "个人总结",
              "honors": [
                {
                  "name": "奖项名称",
                  "date": "获奖时间",
                  "description": "奖项描述"
                }
              ],
              "works": [
                {
                  "name": "作品名称",
                  "description": "作品描述",
                  "url": "作品链接"
                }
              ]
            }

            简历内容：
            %s
            """, fileContent);
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
