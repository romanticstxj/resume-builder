---
inclusion: always
---

# Resume Builder — 项目概览

## 技术栈
- 后端：Spring Boot 3.2.0 + MyBatis + PostgreSQL + Flyway + Spring AI (SiliconFlow/Qwen)
- 前端：Vue 3 + Vite + TDesign + Pinia + Vue Router
- 构建：Maven (JDK 17) + npm

## 目录结构
```
src/main/java/com/resume/
  controller/   # REST 接口层
  service/      # 业务逻辑层
  repository/   # 数据访问层 (Spring Data)
  mapper/       # MyBatis mapper
  entity/       # 数据库实体
  dto/          # 请求/响应 DTO
  config/       # 配置类

src/main/resources/
  application.yml              # 主配置
  db/migration/V{n}__*.sql     # Flyway 迁移脚本 (当前最新 V6)

frontend/src/
  pages/        # 页面组件 (Resume, Template, AI, Login, Register, ParseTask)
  components/   # 公共组件
  api/          # axios 封装 (ai.js, auth.js, resume.js, template.js)
  stores/       # Pinia store (user.js)
  router/       # Vue Router
```

## 数据流
前端 `api/*.js` → 后端 `Controller` → `Service` → `Repository/Mapper` → PostgreSQL

## 关键配置
- 后端端口：8080
- 数据库：`jdbc:postgresql://localhost:5432/resume_builder`，用户 `postgres`
- AI：SiliconFlow API，模型 `Qwen/Qwen2.5-72B-Instruct`
- **禁止提交 API Key**，使用环境变量覆盖 `spring.ai.openai.api-key`
