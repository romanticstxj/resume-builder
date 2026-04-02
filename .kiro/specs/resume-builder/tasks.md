# Resume Builder — 实现任务

## 状态说明
- [ ] 待完成
- [-] 进行中
- [x] 已完成

---

## 已完成功能

### 后端基础
- [x] 项目初始化（Spring Boot 3.2 + MyBatis + PostgreSQL + Flyway）
- [x] 用户认证（注册/登录，BCrypt 加密，JWT access token + refresh token）
- [x] GitHub OAuth 登录（绑定已有邮箱账号）
- [x] 登录防暴力破解（LoginRateLimiter，5 次失败锁定 15 分钟）
- [x] 全局异常处理（GlobalExceptionHandler）
- [x] 简历 CRUD API
- [x] 模板 CRUD API（含分类查询、使用次数统计、权限控制）
- [x] Flyway 数据库迁移（V1~V11）
- [x] AI 简历解析（PDF/Word/TXT 上传 + 异步任务队列）
- [x] AI 内容生成（Spring AI + SiliconFlow/Qwen2.5-72B）
- [x] 简历渲染服务（模板 + 数据 → HTML，ResumeSectionEnum 枚举驱动）
- [x] Word 导出（Apache POI，支持主题颜色/字体）
- [x] 解析任务事件驱动（TaskCreatedEvent + AIParseListener @Async）
- [x] 解析任务定时重试（RetryScheduler，指数退避，最多 3 次）
- [x] AI 调用限流（Guava RateLimiter，3 QPS，全局共享）
- [x] AiController userId 从 SecurityContextHolder 获取（非硬编码）

### 前端基础
- [x] 项目初始化（Vue 3 + Vite + TDesign + Pinia）
- [x] 路由配置 + 登录守卫（access token 过期检查 + refresh token 有效性判断）
- [x] 登录/注册页面（含记住我、回车提交）
- [x] GitHub OAuth 回调页面
- [x] 主布局（侧边栏 + 顶部导航）
- [x] 简历列表页（含删除、复制、JSON 导入/导出）
- [x] 简历编辑器页（左侧表单 + 右侧实时预览，中英文切换）
- [x] 模板市场页
- [x] 模板编辑页
- [x] 模板预览组件（动态渲染）
- [x] AI 生成页
- [x] 解析任务列表页（含状态显示、查看结果、重试、取消）
- [x] 简历导入组件（ResumeImport.vue，文件上传 + 轮询 + 结果预览）
- [x] axios 拦截器（自动附加 token，401 自动刷新，统一错误提示）
- [x] Pinia store（token/refreshToken/userInfo，login/logout/register）
- [x] 富文本编辑器组件（RichTextEditor.vue，基于 Tiptap）

---

## 下一阶段：近期任务（高优先级）

### AI 解析健壮性

- [ ] **AI 解析超 token 截断**
  - 在 `ResumeParserServiceImpl.parse()` 中，调用 AI 前先去除多余空白行，再截断至约 7000 字符
  - prompt 中注明"内容已截取"，避免直接报错
  - 同步处理 `parse-resume-sync` 接口

- [ ] **卡死任务恢复**
  - `RetryScheduler` 新增扫描：`status='processing'` 且 `processing_at < now - 10min` 的任务重置为 `pending`
  - 在 `ParseTaskMapper` 中新增 `resetStuckTasks(LocalDateTime threshold)` 方法

### 认证补全

- [ ] **后端：注册时 username 唯一性校验**
  - `UserMapper` 加 `existsByUsername`，注册时同步检查，重复时返回明确错误信息

- [ ] **后端：注册后自动登录**
  - 注册接口改为返回 `LoginResponse`（含 token + refreshToken）
  - 前端注册成功后直接存 token 并跳转主页，无需二次登录

- [ ] **后端：`/api/auth/me` 接口**
  - 返回当前登录用户信息（id、username、email、avatarUrl）
  - 前端刷新页面时调用此接口同步最新用户信息，替代依赖 localStorage 缓存

- [ ] **后端：refresh token 定期清理**
  - 新建 `TokenCleanupScheduler`，`@Scheduled(cron = "0 0 3 * * ?")` 每天凌晨 3 点调用 `RefreshTokenServiceImpl.cleanExpired()`

- [ ] **前端：登出按钮**
  - 在主布局顶部导航用户头像/名称下拉菜单中加"退出登录"
  - 调用 `userStore.logout()`（已实现，调用后端 logout 接口 + 清除本地 token）

### 代码质量

- [ ] **后端：`AuthController` 清理未使用 import**
  - 删除未使用的 `SecurityUtils` import，消除编译警告

- [ ] **后端：密码强度校验**
  - 注册时要求密码包含字母 + 数字，在 `RegisterRequest` 加 `@Pattern` 注解

---

## 下一阶段：中期任务

### 富文本编辑器集成

- [ ] **前端：Editor.vue 集成 RichTextEditor**
  - 将 `experience.description`、`projects.description`、`works.description` 字段替换为 `<RichTextEditor>` 组件
  - 存储格式改为 HTML 字符串（content JSON 中对应字段）

- [ ] **后端：Word 导出支持 HTML 内容**
  - `ResumeRenderServiceImpl.exportToWord()` 中，对 HTML 格式的描述字段做基础解析（去标签或保留段落结构）

### 简历翻译

- [ ] **后端：简历翻译服务**
  - 新增 `ResumeTranslateService`，调用 Spring AI 翻译 content 各字段（保留结构，仅翻译文本值）
  - 新增接口 `POST /api/ai/translate-resume/{id}`，翻译结果创建为新简历，返回新简历 ID

- [ ] **前端：翻译入口**
  - 简历列表页和编辑器页加"翻译为英文"按钮
  - 翻译完成后跳转到新简历编辑器

### 简历版本控制

- [ ] **后端：版本控制数据层**
  - 新增 Flyway 迁移 V12：创建 `resume_versions` 表（id, resume_id, version_num, content JSON, created_at）
  - `ResumeMapper` 新增 `insertVersion`、`listVersions`、`getVersion` 方法

- [ ] **后端：版本控制接口**
  - `ResumeService.update()` 中，保存前先将当前版本写入 `resume_versions`
  - 新增接口：`GET /api/resumes/{id}/versions`、`POST /api/resumes/{id}/versions/{versionId}/restore`

- [ ] **前端：历史版本入口**
  - 编辑器右上角加"历史版本"按钮，弹出版本列表，支持预览和回滚

### AI 对话式简历优化

- [ ] **后端：对话优化服务**
  - 新增 Flyway 迁移 V13：创建 `ai_conversations` 表（id, resume_id, user_id, messages JSONB, stage, created_at）
  - 新增 `OptimizerChatService`，实现状态机：`evaluation → direction → content_select → suggestion → confirmation`
  - 新增接口：`POST /api/ai/optimize/chat`、`POST /api/ai/optimize/apply-suggestion`

- [ ] **前端：聊天优化界面**
  - 新增 `AiOptimizeChat.vue` 聊天界面
  - 嵌入编辑器右侧面板或独立页面 `/ai/optimize/:resumeId`

---

## 低优先级（二期规划）

- [ ] 服务端 PDF 生成（iText 或 wkhtmltopdf，替代浏览器打印）
- [ ] 简历分享（生成分享链接、有效期、密码保护）
- [ ] 响应式设计（移动端适配）
- [ ] 面试管理模块（JD 分析、简历匹配、面试流程追踪）
- [ ] 数据统计（AI 使用量、简历查看次数）
- [ ] Docker 部署优化 + GitHub Actions CI/CD
- [ ] 单元测试 + 集成测试（核心 Service 层覆盖）
- [ ] 日志规范化（MDC 统一追踪 taskId）
