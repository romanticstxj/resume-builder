package com.resume.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.Page;
import com.resume.dto.request.ResumeCreateRequest;
import com.resume.dto.request.ResumeUpdateRequest;
import com.resume.dto.response.ParseResumeResponse;
import com.resume.entity.Resume;
import com.resume.repository.ResumeRepository;
import com.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Resume create(ResumeCreateRequest request, Long userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(request.getTitle());
        resume.setTemplateId(request.getTemplateId());
        resume.setContent(request.getContent());
        resume.setStatus("draft");
        resume.setIsPrimary(false);
        resume.setCreatedAt(java.time.LocalDateTime.now());
        resume.setUpdatedAt(java.time.LocalDateTime.now());

        return resumeRepository.save(resume);
    }

    @Override
    @Transactional
    public Resume createFromParsed(ParseResumeResponse parseResponse, Long userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(parseResponse.getTitle());
        resume.setTemplateId(null); // 用户后续可自行选择模板
        resume.setStatus("draft");
        resume.setIsPrimary(false);
        resume.setCreatedAt(java.time.LocalDateTime.now());
        resume.setUpdatedAt(java.time.LocalDateTime.now());

        try {
            // 将解析的内容序列化为JSON字符串存储
            String contentJson = objectMapper.writeValueAsString(parseResponse.getContent());
            resume.setContent(contentJson);
        } catch (JsonProcessingException e) {
            log.error("序列化简历内容失败", e);
            throw new RuntimeException("保存简历失败", e);
        }

        return resumeRepository.save(resume);
    }

    @Override
    public Resume getById(Long id, Long userId) {
        Resume resume = resumeRepository.findById(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该简历");
        }
        return resume;
    }

    @Override
    public Page<Resume> getResumeList(Long userId, String status, int page, int size) {
        if (status == null || status.isEmpty()) {
            return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId, page, size);
        }
        return resumeRepository.findByUserIdAndStatus(userId, status, page, size);
    }

    @Override
    @Transactional
    public Resume update(Long id, ResumeUpdateRequest request, Long userId) {
        Resume resume = getById(id, userId);

        if (request.getTitle() != null) {
            resume.setTitle(request.getTitle());
        }
        if (request.getTemplateId() != null) {
            resume.setTemplateId(request.getTemplateId());
        }
        if (request.getContent() != null) {
            resume.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            resume.setStatus(request.getStatus());
        }

        // 如果设置为默认简历，需要取消其他简历的默认状态
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            resumeRepository.clearPrimaryForUser(userId, resume.getId());
            resume.setIsPrimary(true);
        }

        resume.setUpdatedAt(java.time.LocalDateTime.now());
        return resumeRepository.save(resume);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Resume resume = getById(id, userId);
        resumeRepository.delete(resume);
    }

    @Override
    @Transactional
    public Resume copy(Long id, Long userId) {
        Resume originalResume = getById(id, userId);

        Resume newResume = new Resume();
        newResume.setUserId(userId);
        newResume.setTitle(originalResume.getTitle() + " (副本)");
        newResume.setTemplateId(originalResume.getTemplateId());
        newResume.setContent(originalResume.getContent());
        newResume.setStatus("draft");
        newResume.setIsPrimary(false);
        newResume.setCreatedAt(java.time.LocalDateTime.now());
        newResume.setUpdatedAt(java.time.LocalDateTime.now());

        return resumeRepository.save(newResume);
    }
}
