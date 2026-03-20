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
            String sql = "INSERT INTO parse_tasks (user_id, file_name, file_size, status, progress, error_message, parse_result, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?::json, ?, ?) RETURNING id";
            Long id = jdbcTemplate.queryForObject(sql, Long.class,
                task.getUserId(),
                task.getFileName(),
                task.getFileSize(),
                task.getStatus(),
                task.getProgress(),
                task.getErrorMessage(),
                task.getParseResult(),
                task.getCreatedAt(),
                task.getUpdatedAt()
            );
            task.setId(id);
        } else {
            // 更新现有任务
            String sql = "UPDATE parse_tasks SET status = ?, progress = ?, error_message = ?, " +
                         "parse_result = ?::json, updated_at = ?, completed_at = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                task.getStatus(),
                task.getProgress(),
                task.getErrorMessage(),
                task.getParseResult(),
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
        int offset = page * size;
        String sql = "SELECT * FROM parse_tasks WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ParseTask> tasks = jdbcTemplate.query(sql, rowMapper, userId, size, offset);

        // 获取总数
        String countSql = "SELECT COUNT(*) FROM parse_tasks WHERE user_id = ?";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, userId);

        return new Page<>(tasks, total, page, size);
    }

    public ParseTask findLatestByUserId(Long userId) {
        String sql = "SELECT * FROM parse_tasks WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        List<ParseTask> tasks = jdbcTemplate.query(sql, rowMapper, userId);
        return tasks.isEmpty() ? null : tasks.get(0);
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
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            if (rs.getTimestamp("completed_at") != null) {
                task.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
            }
            return task;
        }
    }
}
