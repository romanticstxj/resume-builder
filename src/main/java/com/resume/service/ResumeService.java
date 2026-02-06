package com.resume.service;

import com.resume.dto.Page;
import com.resume.dto.ResumeCreateRequest;
import com.resume.dto.ResumeUpdateRequest;
import com.resume.entity.Resume;

public interface ResumeService {
    Resume create(ResumeCreateRequest request, Long userId);
    Resume getById(Long id, Long userId);
    Page<Resume> getResumeList(Long userId, String status, int page, int size);
    Resume update(Long id, ResumeUpdateRequest request, Long userId);
    void delete(Long id, Long userId);
    Resume copy(Long id, Long userId);
}
