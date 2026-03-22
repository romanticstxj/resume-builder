package com.resume.repository;

import com.resume.dto.Page;
import com.resume.entity.ParseTask;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 解析任务Repository
 */
@Repository
public class ParseTaskRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ParseTaskRowMapper rowMapper = new ParseTaskRowMapper();

    public ParseTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ParseTask save(ParseTask task) {
        if (task.getId() == null) {
            // 插入新任务（使用PostgreSQL的RETURNING子句）
            String sql = "INSERT INTO parse_tasks (user_id, file_name, file_size, status, progress, error_message, parse_result, plaintext, retry_count, max_retries, last_error, processing_by, processing_at, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?::json, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            Long id = jdbcTemplate.queryForObject(sql, Long.class,
                task.getUserId(),
                task.getFileName(),
                task.getFileSize(),
                task.getStatus(),
                task.getProgress(),
                task.getErrorMessage(),
                task.getParseResult(),
                task.getPlaintext(),
                task.getRetryCount(),
                task.getMaxRetries(),
                task.getLastError(),
                task.getProcessingBy(),
                task.getProcessingAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
            );
            task.setId(id);
        } else {
            // 更新现有任务
            String sql = "UPDATE parse_tasks SET status = ?, progress = ?, error_message = ?, " +
                         "parse_result = ?::json, plaintext = ?, retry_count = ?, max_retries = ?, last_error = ?, processing_by = ?, processing_at = ?, updated_at = ?, completed_at = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                task.getStatus(),
                task.getProgress(),
                task.getErrorMessage(),
                task.getParseResult(),
                task.getPlaintext(),
                task.getRetryCount(),
                task.getMaxRetries(),
                task.getLastError(),
                task.getProcessingBy(),
                task.getProcessingAt(),
                task.getUpdatedAt(),
                task.getCompletedAt(),
                task.getId()
            );
        }
        return task;
    }

    public ParseTask findById(Long id) {
        String sql = "SELECT * FROM parse_tasks WHERE id = ?";
        List<ParseTask> tasks = jdbcTemplate.query(sql, rowMapper, id);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    public Page<ParseTask> findByUserId(Long userId, int page, int size) {
        // normalize page size
        int pageSize = Math.max(1, size);

        // 获取总数
        String countSql = "SELECT COUNT(*) FROM parse_tasks WHERE user_id = ?";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
        if (total == null) total = 0;

        // compute total pages (page is 0-based)
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 1;
        if (totalPages <= 0) totalPages = 1;

        // clamp requested page to [0, totalPages-1]
        int pageNumber = Math.max(0, page);
        if (pageNumber >= totalPages) {
            pageNumber = Math.max(0, totalPages - 1);
        }

        int offset = pageNumber * pageSize;
        String sql = "SELECT * FROM parse_tasks WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ParseTask> tasks = jdbcTemplate.query(sql, rowMapper, userId, pageSize, offset);

        // Page(List<T> content, int pageNumber, int pageSize, long totalElements)
        return new Page<>(tasks, pageNumber, pageSize, total);
    }

    public ParseTask findLatestByUserId(Long userId) {
        String sql = "SELECT * FROM parse_tasks WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        List<ParseTask> tasks = jdbcTemplate.query(sql, rowMapper, userId);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    /**
     * Try to atomically claim a task for processing: set status to 'processing' if current status is 'pending'.
     * Returns true if claim succeeded.
     */
    /**
     * Atomically claim a task for processing and record the workerId and timestamp.
     * Returns true if claim succeeded.
     */
    public boolean claimTaskForProcessing(Long id, String workerId) {
        // Allow claiming only if status is 'pending'. Keep claim atomic to avoid races.
        String sql = "UPDATE parse_tasks SET status = 'processing', processing_by = ?, processing_at = now(), updated_at = now() " +
                     "WHERE id = ? AND status = 'pending'";
        int updated = jdbcTemplate.update(sql, workerId, id);
        return updated > 0;
    }

    /**
     * Mark task for retry by updating retry_count, last_error and next_try_at and set status back to pending
     */
    public void markForRetry(Long id, int retryCount, String lastError, java.time.LocalDateTime nextTryAt) {
        String sql = "UPDATE parse_tasks SET retry_count = ?, last_error = ?, next_try_at = ?, status = 'pending', updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, retryCount, lastError, java.sql.Timestamp.valueOf(nextTryAt), java.time.LocalDateTime.now(), id);
    }

    /**
     * Find tasks whose next_try_at is due (<= now) or null and status = pending
     */
    public List<ParseTask> findDueTasks(int limit) {
        String sql = "SELECT * FROM parse_tasks WHERE status = 'pending' AND (next_try_at IS NULL OR next_try_at <= now()) ORDER BY created_at FOR UPDATE SKIP LOCKED LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public void setNextTryAt(Long id, java.time.LocalDateTime nextTryAt) {
        String sql = "UPDATE parse_tasks SET next_try_at = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, java.sql.Timestamp.valueOf(nextTryAt), java.time.LocalDateTime.now(), id);
    }

    private static class ParseTaskRowMapper implements RowMapper<ParseTask> {
        @Override
        public ParseTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            ParseTask task = new ParseTask();
            task.setId(rs.getLong("id"));
            task.setUserId(rs.getLong("user_id"));
            task.setFileName(rs.getString("file_name"));
            task.setFileSize(rs.getLong("file_size"));
            task.setStatus(rs.getString("status"));
            task.setProgress(rs.getInt("progress"));
            task.setErrorMessage(rs.getString("error_message"));
            task.setParseResult(rs.getString("parse_result"));
            task.setPlaintext(rs.getString("plaintext"));
            task.setRetryCount(rs.getInt("retry_count"));
            task.setMaxRetries(rs.getInt("max_retries"));
            task.setLastError(rs.getString("last_error"));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            if (rs.getTimestamp("processing_at") != null) {
                task.setProcessingAt(rs.getTimestamp("processing_at").toLocalDateTime());
            }
            task.setProcessingBy(rs.getString("processing_by"));
            if (rs.getTimestamp("completed_at") != null) {
                task.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
            }
            return task;
        }
    }
}
