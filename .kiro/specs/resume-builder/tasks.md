# Resume Builder — 实现任务

## 状态说明
- [ ] 待完成
- [x] 已完成

---

## 已完成功能

### 后端
- [x] 项目初始化（Spring Boot 3.2 + MyBatis + PostgreSQL + Flyway）
- [x] 用户认证（注册/登录，JWT 简化版）
- [x] 简历 CRUD API
- [x] 模板 CRUD API（含分类查询、使用次数统计）
- [x] Flyway 数据库迁移（V1~V6）
- [x] AI 简历解析（PDF/Word 上传 + 异步任务队列）
- [x] AI 内容生成（Spring AI + SiliconFlow/Qwen2.5-72B）
- [x] 简历渲染服务（模板 + 数据 → HTML）
- [x] Word 导出（Apache POI）
- [x] 解析任务事件驱动（TaskCreatedEvent + AIParseListener）
- [x] 解析任务定时重试（RetryScheduler，指数退避）
- [x] AI 调用限流（Guava RateLimiter，3 QPS，AIParseListener 和 RetryScheduler 共享同一 Bean）

### 前端
- [x] 项目初始化（Vue 3 + Vite + TDesign + Pinia）
- [x] 路由配置 + 登录守卫
- [x] 登录/注册页面
- [x] 主布局（侧边栏 + 顶部导航）
- [x] 简历列表页
- [x] 简历编辑器页
- [x] 模板市场页
- [x] 模板编辑页
- [x] 模板预览组件（动态渲染）
- [x] AI 生成页
- [x] 解析任务列表页（含状态显示、查看结果、重试）
- [x] 简历导入组件（ResumeImport.vue）

---

## 待完成

### 高优先级（影响核心体验）

- [ ] JWT 拦截器完善（当前为简化版，缺少 Token 验证中间件）
- [ ] 密码加密（当前明文存储，需改为 BCrypt）
- [ ] 全局异常处理（GlobalExceptionHandler）
- [ ] 前端 axios 拦截器（统一错误处理、401 自动跳转登录）
- [ ] 简历预览/导出功能完善（前端导出按钮接通后端接口）
- [ ] 前端表单校验和错误提示优化

### 中优先级（完善功能）

- [ ] 简历翻译（中→英，AI-SPRINT.md 模块2）
  - 后端翻译服务 + `/api/ai/translate-resume/{id}`
  - 模板多语言标签（section_labels）
  - 前端翻译入口和 TranslationDialog
- [ ] AI 对话式简历优化（AI-SPRINT.md 模块3）
  - 对话状态机（评价→方向→内容→建议→确认）
  - 后端 OptimizerChatService
  - 前端聊天界面 AiOptimizeChat.vue
- [ ] 简历版本控制（历史版本记录、回滚）
- [ ] 登出功能 + Token 自动刷新
- [ ] application.yml 中 API Key 改为环境变量

### 低优先级（二期规划）

- [ ] 面试管理模块（JD 分析、简历匹配、面试流程追踪）
- [ ] 简历分享（生成分享链接、有效期、密码保护）
- [ ] 数据统计（面试转化率、AI 使用量）
- [ ] 响应式设计（移动端适配）
- [ ] Docker 部署 + CI/CD

---

## 技术债务

- [ ] 密码明文存储 → BCrypt 加密
- [ ] JWT 简化版 → 完整验证链路
- [ ] 缺少单元测试和集成测试
- [ ] 前后端状态值不统一（已修复 `success`/`completed`，需全面排查）
- [ ] 日志规范化（统一 MDC 追踪 taskId）
