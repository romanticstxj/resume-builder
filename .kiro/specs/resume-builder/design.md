# Resume Builder — 设计文档

## 架构概览

```
┌─────────────────────────────────────┐
│  Vue 3 Frontend (Vite, port 5173)   │
│  pages/ components/ api/ stores/    │
└──────────────┬──────────────────────┘
               │ HTTP (axios)
               ▼
┌─────────────────────────────────────┐
│  Spring Boot Backend (port 8080)    │
│  Controller → Service → Repository  │
└──────────┬──────────────────────────┘
           │
    ┌──────┴──────┐
    ▼             ▼
PostgreSQL    SiliconFlow AI
(Flyway)      (Qwen2.5-72B)
```

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `users` | 用户账号 |
| `resumes` | 简历数据（JSON 存储内容） |
| `templates` | 简历模板（HTML/CSS） |
| `parse_tasks` | AI 解析任务队列 |

## API 设计

### 认证
- `POST /api/auth/register` — 注册
- `POST /api/auth/login` — 登录，返回 token

### 简历
- `GET /api/resumes` — 列表（分页）
- `POST /api/resumes` — 创建
- `GET /api/resumes/{id}` — 详情
- `PUT /api/resumes/{id}` — 更新
- `DELETE /api/resumes/{id}` — 删除

### 模板
- `GET /api/templates` — 市场列表
- `POST /api/templates` — 创建
- `PUT /api/templates/{id}` — 更新
- `DELETE /api/templates/{id}` — 删除
- `GET /api/templates/{id}/preview` — 预览渲染

### AI
- `POST /api/ai/parse` — 上传文件，创建解析任务
- `GET /api/ai/tasks` — 查看解析任务列表
- `POST /api/ai/generate` — AI 生成简历内容（流式）

## 前端路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | Login | 登录 |
| `/register` | Register | 注册 |
| `/resumes` | Resume/List | 简历列表（默认首页） |
| `/resumes/create` | Resume/Editor | 创建简历 |
| `/resumes/:id/edit` | Resume/Editor | 编辑简历 |
| `/templates` | Template/Market | 模板市场 |
| `/template/list` | Template/List | 我的模板 |
| `/ai/generate` | AI/Generate | AI 生成 |
| `/parse-tasks` | ParseTask/List | 解析任务 |

## 关键技术决策
- 简历内容以 JSON 存储在 `resumes.content` 字段，灵活扩展
- AI 解析任务异步执行（`@Async`），通过 `parse_tasks` 表轮询状态
- 模板渲染在后端完成（`ResumeRenderService`），前端只负责展示
