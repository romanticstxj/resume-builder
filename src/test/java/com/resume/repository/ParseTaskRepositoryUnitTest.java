package com.resume.repository;

import com.resume.entity.ParseTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ParseTaskRepositoryUnitTest {

    @Test
    public void save_shouldUseJsonCastForParseResult_onInsertAndUpdate() {
        // Use a simple in-memory stub of JdbcTemplate to capture SQL and parameters
        class StubJdbcTemplate extends JdbcTemplate {
            String lastSql;
            Object[] lastArgs;
            @Override
            public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
                this.lastSql = sql;
                this.lastArgs = args;
                return (T) Long.valueOf(42L);
            }
            @Override
            public int update(String sql, Object... args) {
                this.lastSql = sql;
                this.lastArgs = args;
                return 1;
            }
        }

        StubJdbcTemplate jdbc = new StubJdbcTemplate();
        ParseTaskRepository repo = new ParseTaskRepository(jdbc);
        ParseTask task = new ParseTask();
        task.setUserId(1L);
        task.setFileName("file.docx");
        task.setFileSize(123L);
        task.setStatus("pending");
        task.setProgress(0);
        task.setErrorMessage(null);
        task.setParseResult("{\"name\":\"张三\"}");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // StubJdbcTemplate already returns id for queryForObject
        ParseTask saved = repo.save(task);
        assertThat(saved.getId()).isEqualTo(42L);

        // verify insert SQL captured by stub
        String insertSql = jdbc.lastSql;
        assertThat(insertSql).contains("parse_result").contains("::json");

        // now simulate update path
        saved.setStatus("completed");
        saved.setParseResult("{\"name\":\"李四\"}");

        repo.save(saved);
        String updateSql = jdbc.lastSql;
        assertThat(updateSql).contains("parse_result").contains("::json");
    }
}

