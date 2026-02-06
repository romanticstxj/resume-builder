# 简历制作工具 - 分步实现指南

## 第一步：多模板管理 ✅

### 实现功能
- ✅ 模板查询（全部/按分类）
- ✅ 模板创建（支持JSON格式内容存储）
- ✅ 模板更新和删除
- ✅ 模板使用次数统计
- ✅ 官方/用户模板区分

### API 接口

#### 获取所有模板
```
GET /api/templates
```

#### 按分类获取模板
```
GET /api/templates/category/{category}
```

#### 获取单个模板
```
GET /api/templates/{id}
```

#### 创建模板
```
POST /api/templates
Content-Type: application/json

{
  "name": "我的模板",
  "category": "专业",
  "description": "模板描述",
  "previewUrl": "http://example.com/preview.png",
  "content": "{\"layout\": \"simple\", \"sections\": [...] }",
  "isPublic": true
}
```

#### 更新模板
```
PUT /api/templates/{id}
Content-Type: application/json

{
  "name": "更新后的模板名",
  ...
}
```

#### 删除模板
```
DELETE /api/templates/{id}
```

---

## 第二步：简历创建 ✅

### 实现功能
- ✅ 简历创建（基于模板）
- ✅ 简历查询（单个/列表）
- ✅ 简历更新
- ✅ 简历删除
- ✅ 简历复制
- ✅ 默认简历设置

### API 接口

#### 创建简历
```
POST /api/resumes
Content-Type: application/json

{
  "title": "我的简历",
  "templateId": 1,
  "content": "{\"name\": \"张三\", \"email\": \"test@example.com\", ...}"
}
```

#### 获取简历列表
```
GET /api/resumes?status=draft&page=0&size=10
```

#### 获取单个简历
```
GET /api/resumes/{id}
```

#### 更新简历
```
PUT /api/resumes/{id}
Content-Type: application/json

{
  "title": "更新后的标题",
  "content": "{...}",
  "status": "published",
  "isPrimary": true
}
```

#### 删除简历
```
DELETE /api/resumes/{id}
```

#### 复制备份简历
```
POST /api/resumes/{id}/copy
```

---

## 环境准备

### 1. 安装 PostgreSQL

#### Windows
1. 下载安装: https://www.postgresql.org/download/windows/
2. 安装时设置密码（默认: postgres）
3. 确保 PostgreSQL 服务已启动

#### 创建数据库
```sql
-- 使用 psql 或 pgAdmin
CREATE DATABASE resume_builder;
```

### 2. 启动项目

#### 方法1：使用 Maven
```bash
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%
cd c:\Users\lenovo\CodeBuddy\resume-builder
D:\apache-maven-3.9.12\bin\mvn spring-boot:run
```

#### 方法2：使用 IDEA
1. 打开项目
2. 运行 ResumeApplication.main()

### 3. 测试 API

#### 使用 curl
```bash
# 获取所有模板
curl http://localhost:8080/api/templates

# 创建简历
curl -X POST http://localhost:8080/api/resumes ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"测试简历\",\"templateId\":1,\"content\":\"{}\"}"
```

#### 使用 Postman
导入以下请求：
- GET http://localhost:8080/api/templates
- POST http://localhost:8080/api/resumes
  Body:
  ```json
  {
    "title": "我的第一份简历",
    "templateId": 1,
    "content": "{\"name\": \"张三\", \"email\": \"zhangsan@example.com\"}"
  }
  ```

---

## 技术说明

### 为什么使用 Spring Boot 3？
1. **Jakarta EE 标准**：从 `javax.*` 迁移到 `jakarta.*`
2. **性能优化**：更好的虚拟线程支持、AOT 编译
3. **安全增强**：Spring Security 6 集成
4. **现代化特性**：支持 Java 17+ 特性
5. **长期支持**：官方长期维护版本

### 为什么使用 JDK 17？
1. **LTS 长期支持**：支持到 2029 年
2. **记录类型（Record）**：简化数据类
3. **模式匹配**：更简洁的代码
4. **文本块**：优雅的多行字符串
5. **性能提升**：GC 优化、启动更快

### 为什么选择 PostgreSQL？
1. **JSON 支持**：原生 JSONB 类型，查询性能好
2. **扩展性强**：丰富的扩展插件
3. **可靠性高**：ACID 完全支持
4. **开源免费**：MIT 许可证
5. **生态完善**：工具和文档丰富

### 为什么使用 Spring Data JPA？
1. **简化开发**：自动生成 CRUD
2. **类型安全**：强类型查询
3. **可读性好**：方法名即查询
4. **易于测试**：Mock 简单

---

## 数据库表结构

### users（用户表）
```sql
id          BIGINT       主键
username    VARCHAR(100) 用户名
email       VARCHAR(100) 邮箱（唯一）
status      INTEGER      状态
created_at  TIMESTAMP    创建时间
```

### templates（模板表）
```sql
id           BIGINT       主键
name         VARCHAR(100) 模板名称
category     VARCHAR(50)  分类
description  VARCHAR(500) 描述
preview_url  VARCHAR(500) 预览图URL
content      TEXT         JSON格式内容
is_official  BOOLEAN      是否官方模板
is_public    BOOLEAN      是否公开
created_by   BIGINT       创建者ID
usage_count  INTEGER      使用次数
created_at   TIMESTAMP    创建时间
updated_at   TIMESTAMP    更新时间
```

### resumes（简历表）
```sql
id           BIGINT       主键
user_id      BIGINT       用户ID（外键）
title        VARCHAR(200) 简历标题
template_id  BIGINT       模板ID
content      TEXT         JSON格式内容
status       VARCHAR(20)  状态（draft/published）
is_primary   BOOLEAN      是否默认简历
created_at   TIMESTAMP    创建时间
updated_at   TIMESTAMP    更新时间
```

---

## 下一步计划

- [ ] 添加用户认证（JWT）
- [ ] PDF/Word 导出功能
- [ ] AI 内容优化功能
- [ ] 前端 Vue 3 + TDesign 实现
