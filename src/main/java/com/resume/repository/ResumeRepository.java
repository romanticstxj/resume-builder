package com.resume.repository;

import com.resume.dto.Page;
import com.resume.entity.Resume;
import com.resume.mapper.ResumeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResumeRepository {

    private final ResumeMapper resumeMapper;

    public ResumeRepository(ResumeMapper resumeMapper) {
        this.resumeMapper = resumeMapper;
    }

    public Page<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId, int page, int size) {
        int offset = (page-1) * size;
        List<Resume> content = resumeMapper.findByUserIdOrderByUpdatedAtDesc(userId, size, offset);
        long total = resumeMapper.countByUserId(userId);
        return Page.of(content, page, size, total);
    }

    public Page<Resume> findByUserIdAndStatus(Long userId, String status, int page, int size) {
        int offset =  (page-1) * size;
        List<Resume> content = resumeMapper.findByUserIdAndStatus(userId, status, size, offset);
        long total = resumeMapper.countByUserIdAndStatus(userId, status);
        return Page.of(content, page, size, total);
    }

    public Resume findById(Long id) {
        return resumeMapper.findById(id);
    }

    public Resume save(Resume resume) {
        if (resume.getId() == null) {
            resumeMapper.insert(resume);
        } else {
            resumeMapper.update(resume);
        }
        return resume;
    }

    public void delete(Resume resume) {
        resumeMapper.delete(resume.getId());
    }

    public List<Resume> findAllByUserIdAndIsPrimary(Long userId, Boolean isPrimary) {
        return resumeMapper.findAllByUserIdAndIsPrimary(userId, isPrimary);
    }

    public void clearPrimaryForUser(Long userId, Long excludeId) {
        resumeMapper.clearPrimaryForUser(userId, excludeId);
    }
}
