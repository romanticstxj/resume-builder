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
- [x] 导出pdf和预览里面，skills模块的颜色和其他模块保持一致，现在是红色的
- [x] projects模块加上Company字段
- [x] Projects和Works模块的Role都换行，用区别于标题和内容的样式显示
- [x] Work Experience模块的Role也换行，用区别于标题和内容的样式显示；Projects模块的Company和Role在一行，Company在前显示，Role在后
- [x] Bullet Point的宽度小一点；导出和预览的照片再稍微比例放大一点点
- [x] 添加简历删除功能，模板删除也需要
- [x] 开发环境中帮我后端代码弄成热启动，不需要每次改动代码后重启了
- [x] 去掉简历自身的sectionOrder，只用模板里的sectionOrder进行渲染和导出，这个sectionOrder和简历自身绑定估计是之前的误会

### 高优先级（影响核心体验）

- [x] JWT 拦截器完善（当前为简化版，缺少 Token 验证中间件）
- [x] 密码加密（当前明文存储，需改为 BCrypt）
- [x] 全局异常处理（GlobalExceptionHandler）
- [x] 前端 axios 拦截器（统一错误处理、401 自动跳转登录）
- [x] 简历预览/导出功能完善（前端导出按钮接通后端接口）
- [x] 前端表单校验和错误提示优化

### 简历编辑器优化

- [x] 简历模块标题中英文切换（根据 content.language 字段渲染对应语言标题）
- [x] 修正默认模块排序：个人简介→专业技能→工作经历→项目经历→个人作品→教育经历→荣誉证书，移除"个人总结"模块
- [x] 导入/创建时支持中英文语言选择，AI 解析按语言生成对应字段内容（V8 迁移：parse_tasks.language 列）

### 模块注册表重构（架构改善）

- [x] 后端：新建 `ResumeSectionEnum` 枚举，包含 key、中英文标签、渲染函数引用
- [x] 后端：重构 `ResumeRenderServiceImpl`，将 switch 替换为枚举驱动的渲染 Map
- [x] 前端：新建 `src/config/sectionRegistry.js`，统一注册所有模块元数据
- [x] 前端：重构 `ResumeRenderer.vue`，从注册表读取 `defaultSectionOrder` 和 `sectionLabels`

### JSON 导入导出

- [x] 前端 Editor.vue：导出按钮新增"导出 JSON"选项，构造 `{ _version, title, content }` 并下载
- [x] 前端 Resume/List.vue：新增"从 JSON 导入"入口，读取文件 → 校验 → 调用创建接口 → 跳转编辑器

### 中优先级（完善功能）

- [ ] AI 解析超 token 限制处理：上传文件内容超过模型 max_tokens 时，在 `ResumeParserServiceImpl` 中对 `fileContent` 做截断（保留前 N 个字符，约 6000~8000 字符），截断前先去除多余空白行，并在 prompt 中注明"内容已截取"，避免直接报错
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

- [ ] 富文本编辑器：工作描述、项目描述等 textarea 升级为支持 bullet point、有序列表、左/右/居中对齐的富文本编辑器，存储格式改为 HTML，渲染时直接 v-html 输出，导出时同步渲染
- [ ] 卡死任务恢复：`RetryScheduler` 扫描时，将 `status = 'processing'` 且 `processing_at < now - 10min` 的任务重置为 `pending`，防止 worker 崩溃后任务永久卡住
