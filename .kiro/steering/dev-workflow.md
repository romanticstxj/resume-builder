---
inclusion: always
---

# 开发工作流

## 启动项目

### 后端
```bash
# 编译
compile.bat
# 或
mvn -DskipTests package

# 运行
start-backend.bat
# 或（需要指定 JDK17 时）
start-backend-with-jdk17.bat
```

### 前端
```bash
cd frontend
npm install
npm run dev
```

## 数据库变更规范
- 新增迁移文件：`src/main/resources/db/migration/V{n+1}__描述.sql`
- 当前最新版本：V6，下一个应为 V7
- 禁止修改已有的 migration 文件
- 执行 SQL：`run-sql.bat`

## 代码规范

### 后端
- 新增接口：`controller` → `service` 接口 → `service/impl` 实现 → `repository` 或 `mapper`
- DTO 放在 `dto/request`（入参）和 `dto/response`（出参）
- 统一返回 `ApiResponse<T>` 或 `Result<T>`
- 异步任务加 `@Async`（已启用 `@EnableAsync`）

### 前端
- API 调用统一封装在 `frontend/src/api/*.js`，不在组件里直接用 axios
- 页面组件放 `pages/`，复用组件放 `components/`
- 状态管理用 Pinia（`stores/user.js`）

## 安全
- 不提交 `application.yml` 中的真实 API Key
- 敏感配置用环境变量覆盖
