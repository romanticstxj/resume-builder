package com.resume.service;

import com.resume.dto.response.ParseResumeResponse;

/**
 * 简历解析服务接口
 */
public interface ResumeParserService {

    /**
     * 解析简历文件内容
     * @param fileContent 文件文本内容
     * @return 解析后的简历数据
     */
    ParseResumeResponse parse(String fileContent);

    /**
     * 解析简历文件内容（指定语言）
     * @param fileContent 文件文本内容
     * @param language 简历语言 "zh" | "en"
     * @return 解析后的简历数据
     */
    ParseResumeResponse parse(String fileContent, String language);
}
