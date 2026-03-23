# Resume Builder — 问题追踪日志

记录开发过程中遇到的 bug、根因分析和修复方案，便于日后回溯。

---

## 2026-03-23

### BUG-001 · pom.xml 缺少 artifact 导致项目无法启动

**现象：** IDE 报错 `Missing artifact org.flywaydb:flyway-database-postgresql:jar:9.22.0`，项目无法编译。

**根因：** `flyway-database-postgresql` 是 Flyway 10.x 才拆出来的独立模块，9.x 里 PostgreSQL 支持已内置在 `flyway-core` 中，9.22.0 根本不存在这个 jar。

**修复：** 删除 `pom.xml` 中多余的 `flyway-database-postgresql` 依赖，只保留 `flyway-core`。

---

### BUG-002 · VS Code Java 扩展报 `No delegateCommandHandler for vscode.java.resolveMainMethod`

**现象：** 直接运行 `ResumeApplication` 报错，提示找不到类。

**根因：** VS Code Java 扩展尚未完成初始化（后台还在索引项目），不是代码问题。

**修复：** `Ctrl+Shift+P` → `Reload Window`，等待 Java 扩展加载完成（右下角状态变为 "Java Ready"）后再运行。

---

### BUG-003 · 解析任务列表"查看结果"按钮不显示

**现象：** 状态为 `success` 的任务没有"查看结果"按钮。

**根因：** 前后端状态值不一致。后端 `ParseTaskServiceImpl` 将成功状态写为 `"success"`，但前端 `List.vue` 的按钮条件判断是 `row.status === 'completed'`，两者不匹配。同样，`getTaskStatusText` 和 `getTaskStatusTheme` 的映射表里也只有 `completed`，没有 `success`。

**修复：** 前端 `ParseTask/List.vue` 中：
- 按钮条件改为 `v-if="row.status === 'success' || row.status === 'completed'"`
- 重试按钮条件改为 `v-if="row.status === 'failed' || row.status === 'error'"`
- `getTaskStatusText` / `getTaskStatusTheme` 补充 `success`、`error`、`canceled` 的映射

**文件：** `frontend/src/pages/ParseTask/List.vue`

---

### BUG-004 · AI 调用限流器不共享，两条路径可能超过 3 QPS

**现象：** `AIParseListener` 和 `RetryScheduler` 各自独立限流，合计可能超过配置的 3 QPS。

**根因：** `AIParseListener` 用 `private final RateLimiter rateLimiter = RateLimiter.create(3.0)` 硬编码了一个实例级别的限流器，而 `RetryScheduler` 注入的是 `AsyncConfig` 里的 Bean，两者是独立的令牌桶。

**修复：** `AIParseListener` 改为注入 `AsyncConfig` 中定义的 `RateLimiter` Bean，与 `RetryScheduler` 共享同一个令牌桶。

**文件：** `src/main/java/com/resume/event/AIParseListener.java`

---

### BUG-005 · 本地 AI 解析报 `cannot retry due to server authentication, in streaming mode`

**现象：** 上传简历后任务一直 retry，日志报 `RestClientException: Error while extracting response` → `Caused by: HttpRetryException: cannot retry due to server authentication, in streaming mode`。

**根因：** 本地未提交的代码在 `ParseTaskRepository` 的 INSERT 语句中加入了 `language` 列，但对应的 V8 Flyway 迁移文件（`V8__add_language_to_parse_tasks.sql`）是 untracked 状态，本地数据库表里没有这个列，导致 INSERT 失败。任务创建失败后触发重试，重试时调用 AI 接口，SiliconFlow 返回认证相关响应，JDK 内置的 `HttpURLConnection` 在 streaming 模式下无法处理，抛出该异常。Railway 正常是因为跑的是 origin/main，没有这些未提交的改动。

**修复：**
1. 将所有本地改动（含 `V8__add_language_to_parse_tasks.sql`）一起 commit + push
2. Railway 重新部署时 Flyway 自动执行 V8 迁移，`parse_tasks` 表加上 `language` 列
3. 删除错误引入的 `AiHttpConfig.java`（`OpenAiApi` 构造函数不匹配）和 `pom.xml` 中多余的 OkHttp 依赖

**教训：** 修改 Repository 的 SQL 时，必须同步创建对应的 Flyway 迁移文件并一起提交，否则本地/生产数据库结构与代码不一致会引发难以定位的连锁错误。

**文件：**
- `src/main/resources/db/migration/V8__add_language_to_parse_tasks.sql`（新增）
- `src/main/java/com/resume/repository/ParseTaskRepository.java`
- `pom.xml`（移除 OkHttp）

---

## 模板

```
### BUG-XXX · 标题

**现象：** 

**根因：** 

**修复：** 

**教训：**（可选）

**文件：** 
```
