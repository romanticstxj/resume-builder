package com.resume.controller;

import com.resume.dto.ApiResponse;
import com.resume.dto.Page;
import com.resume.dto.response.ParseResumeResponse;
import com.resume.dto.response.ParseTaskResponse;
import com.resume.entity.ParseTask;
import com.resume.service.ParseTaskService;
import com.resume.service.ResumeParserService;
import com.resume.util.FileParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import com.resume.event.TaskCreatedEvent;
import java.io.IOException;


/**
 * AI功能控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ParseTaskService taskService;
    private final ResumeParserService parserService;
    private final ApplicationEventPublisher eventPublisher;

    public AiController(ParseTaskService taskService, ResumeParserService parserService, ApplicationEventPublisher eventPublisher) {
        this.taskService = taskService;
        this.parserService = parserService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 提交简历解析任务（异步）
     * 支持的格式：PDF, Word (.doc, .docx), Text
     */
    @PostMapping("/parse-resume")
    @Transactional
    public ApiResponse<ParseTaskResponse> submitParseTask(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            validateFile(file);

            log.info("提交简历解析任务: {}", file.getOriginalFilename());

            Long userId = 1L; // TODO: 从JWT获取用户ID

            // 提取文本（同步写入 plaintext 到 DB），按你选择的 C 方案
            String plaintext = FileParserUtil.extractText(file);

            // 创建任务并保存 plaintext
            ParseTask task = taskService.createTask(file, userId, plaintext);
            log.info("任务创建成功: {}", task.getId());

            // 发布事件（listener 使用 @TransactionalEventListener AFTER_COMMIT）
            eventPublisher.publishEvent(new TaskCreatedEvent(task.getId()));

            return ApiResponse.success(taskService.toResponse(task));

        } catch (IllegalArgumentException e) {
            log.warn("文件验证失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());


        } catch (Exception e) {
            log.error("提交任务失败", e);
            return ApiResponse.error(500, "提交任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取解析任务状态
     */
    @GetMapping("/tasks/{id}")
    public ApiResponse<ParseTaskResponse> getTaskStatus(@PathVariable Long id) {
        try {
            ParseTask task = taskService.getTaskById(id);
            if (task == null) {
                return ApiResponse.error(404, "任务不存在");
            }
            return ApiResponse.success(taskService.toResponse(task));
        } catch (Exception e) {
            log.error("获取任务状态失败", e);
            return ApiResponse.error(500, "获取任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的任务列表
     */
    @GetMapping("/tasks")
    public ApiResponse<Page<ParseTaskResponse>> getTaskList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            Long userId = 1L; // TODO: 从JWT获取用户ID
            Page<ParseTask> tasks = taskService.getTasksByUserId(userId, page, size);
            // Page constructor: Page(List<T> content, int pageNumber, int pageSize, long totalElements)
            return ApiResponse.success(new Page<>(
                tasks.getContent().stream().map(taskService::toResponse).toList(),
                tasks.getPageNumber(),
                tasks.getPageSize(),
                tasks.getTotalElements()
            ));
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return ApiResponse.error(500, "获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 取消任务
     */
    @PostMapping("/tasks/{id}/cancel")
    public ApiResponse<Void> cancelTask(@PathVariable Long id) {
        try {
            taskService.cancelTask(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("取消任务失败", e);
            return ApiResponse.error(500, "取消任务失败: " + e.getMessage());
        }
    }

    /**
     * 同步解析简历文件（保留原有接口供其他场景使用）
     * 支持的格式：PDF, Word (.doc, .docx), Text
     */
    @PostMapping("/parse-resume-sync")
    public ApiResponse<ParseResumeResponse> parseResumeSync(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            validateFile(file);

            log.info("开始同步解析简历文件: {}", file.getOriginalFilename());

            // 提取文件内容
            String fileContent = FileParserUtil.extractText(file);
            log.info("提取的文本长度: {}", fileContent.length());
            log.info("提取的文本内容预览（前500字符）: {}", fileContent.length() > 500 ? fileContent.substring(0, 500) : fileContent);

            // AI解析
            ParseResumeResponse result = parserService.parse(fileContent);

            log.info("简历解析成功");
            return ApiResponse.success(result);

        } catch (IllegalArgumentException e) {
            log.warn("文件验证失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());

        } catch (IOException e) {
            log.error("文件读取失败", e);
            return ApiResponse.error(500, "文件读取失败: " + e.getMessage());

        } catch (Exception e) {
            log.error("简历解析失败", e);
            return ApiResponse.error(500, "简历解析失败: " + e.getMessage());
        }
    }

    /**
     * 验证上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查文件大小（限制10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        // 检查文件类型
        String extension = getFileExtension(fileName).toLowerCase();
        boolean isSupported = extension.matches("(pdf|doc|docx|txt)");
        if (!isSupported) {
            throw new IllegalArgumentException("不支持的文件格式，请上传PDF、Word或Text文件");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
