---
inclusion: fileMatch
fileMatchPattern: "**/*.sql,**/migration/**,**/entity/**,**/repository/**,**/mapper/**"
---

# 数据库规范

## Flyway 迁移
- 当前最新版本：V6，下一个必须是 V7
- 文件命名：`V{n}__{动词_名词}.sql`，例如 `V7__add_user_avatar.sql`
- 禁止修改已存在的 migration 文件
- 每个 migration 文件顶部加注释说明变更目的

## 实体规范
- 实体类放 `entity/`，字段名用驼峰，数据库列名用下划线（MyBatis 自动映射已开启）
- 时间字段统一用 `LocalDateTime`

## Mapper / Repository
- 简单 CRUD 用 Spring Data `Repository`
- 复杂查询用 MyBatis `Mapper`，XML 放 `src/main/resources/mapper/*.xml`
