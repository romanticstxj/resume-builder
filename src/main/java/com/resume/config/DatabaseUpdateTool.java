package com.resume.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据库字段更新工具（一次性使用）
 * 运行后会自动添加缺失的字段
 */
@Component
public class DatabaseUpdateTool implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("检查并更新数据库字段...");
        System.out.println("========================================");

        // 检查 templates 表的字段
        checkAndAddColumn("templates", "layout", "VARCHAR(50) DEFAULT 'classic'");
        checkAndAddColumn("templates", "theme_config", "TEXT");
        checkAndAddColumn("templates", "section_config", "TEXT");
        checkAndAddColumn("templates", "section_order", "TEXT");
        checkAndModifyColumnNotNull("templates", "content", "TEXT");

        // 检查 resumes 表的字段
        checkAndAddColumn("resumes", "section_order", "TEXT");

        System.out.println("========================================");
        System.out.println("数据库字段检查完成！");
        System.out.println("========================================");
    }

    private void checkAndAddColumn(String tableName, String columnName, String columnDefinition) {
        try {
            // 检查字段是否存在
            String checkSql = "SELECT column_name FROM information_schema.columns " +
                           "WHERE table_name = ? AND column_name = ?";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                checkSql,
                tableName.toLowerCase(),
                columnName.toLowerCase()
            );

            if (result.isEmpty()) {
                System.out.println(String.format("添加字段 %s.%s", tableName, columnName));
                String alterSql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
                jdbcTemplate.execute(alterSql);
                System.out.println(String.format("✓ 字段 %s.%s 添加成功", tableName, columnName));
            } else {
                System.out.println(String.format("✓ 字段 %s.%s 已存在", tableName, columnName));
            }
        } catch (Exception e) {
            System.err.println(String.format("✗ 添加字段 %s.%s 失败: %s", tableName, columnName, e.getMessage()));
        }
    }

    private void checkAndModifyColumnNotNull(String tableName, String columnName, String columnDefinition) {
        try {
            // 检查字段是否存在且允许NULL
            String checkSql = "SELECT is_nullable FROM information_schema.columns " +
                             "WHERE table_name = ? AND column_name = ?";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                checkSql,
                tableName.toLowerCase(),
                columnName.toLowerCase()
            );

            if (!result.isEmpty()) {
                String isNullable = (String) result.get(0).get("is_nullable");
                if ("NO".equals(isNullable)) {
                    System.out.println(String.format("修改字段 %s.%s 允许为NULL", tableName, columnName));
                    String alterSql = String.format("ALTER TABLE %s ALTER COLUMN %s DROP NOT NULL", tableName, columnName);
                    jdbcTemplate.execute(alterSql);
                    System.out.println(String.format("✓ 字段 %s.%s 已修改为允许NULL", tableName, columnName));
                } else {
                    System.out.println(String.format("✓ 字段 %s.%s 已允许NULL", tableName, columnName));
                }
            }
        } catch (Exception e) {
            System.err.println(String.format("✗ 修改字段 %s.%s 失败: %s", tableName, columnName, e.getMessage()));
        }
    }
}
