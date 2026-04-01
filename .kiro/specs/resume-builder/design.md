# Resume Builder — 设计文档

> 基于 requirements.md 及现有实现整理，最后更新：2026-03-28

---

## 1. 系统架构

```
浏览器 (Vue 3 + TDesign)
    │  HTTP/REST + JWT
    ▼
Nginx (生产反向代理 / 静态资源)
    │
    ▼
Spring Boot 3.2 (port 8080)
  ├── AuthController
  ├── ResumeController
  ├── TemplateController
  └── AiController
    │
    ├── Service 层
    │   ├── AuthService
    │   ├── ResumeService
    │   ├── TemplateService
    │   ├── ResumeRenderService
    │   ├── ResumeParserService   ← Spring AI / SiliconFlow
    │   └── ParseTaskService
    │
    ├── 事件总线 (Spring ApplicationEvent)
    │   ├── TaskCreatedEvent
    │   ├── AIParseListener       ← @Async("aiParsingExecutor")
    │   └── RetryScheduler        ← @Scheduled(fixedDelay=30s)
    │
    └── Repository / Mapper (MyBatis + Spring Data)
          │
          ▼
    PostgreSQL 17
```

**部署（Railway）：**
- 后端：Spring Boot JAR，Dockerfile 构建
- 前端：Vite 构建产物，Nginx 容器服务
- 数据库：Railway PostgreSQL 托管实例
- 环境变量注入：`SPRING_DATASOURCE_URL`、`SPRING_AI_OPENAI_API_KEY` 等

---

## 2. 数据库设计

### 2.1 当前表结构（V1~V8 迁移后）

#### users
| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| username | VARCHAR(50) UNIQUE NOT NULL | |
| email | VARCHAR(100) UNIQUE NOT NULL | 登录凭证 |
| password | VARCHAR(255) NOT NULL | BCrypt 加密 |
| phone | VARCHAR(20) | |
| avatar_url | VARCHAR(255) | |
| status | INTEGER DEFAULT 1 | 1=启用 0=禁用 |
| created_at / updated_at | TIMESTAMP | 触发器自动维护 |

#### resumes
| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK→users | |
| title | VARCHAR(100) NOT NULL | |
| template_id | BIGINT | 关联模板（可为空） |
| content | JSON NOT NULL | 简历内容，见 §2.2 |
| section_order | TEXT | JSON 数组，模块渲染顺序 |
| status | VARCHAR(20) DEFAULT 'draft' | draft / published |
| is_primary | BOOLEAN DEFAULT FALSE | |
| created_at / updated_at | TIMESTAMP | 触发器自动维护 |

#### templates
| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| name | VARCHAR(100) NOT NULL | |
| category | VARCHAR(50) NOT NULL | |
| description | TEXT | |
| preview_url | VARCHAR(255) | |
| content | JSON NOT NULL | 模板初始内容 |
| layout | VARCHAR(50) | classic / modern / minimalist / creative |
| theme_config | TEXT | JSON：primaryColor, textColor, fontFamily, fontSize |
| section_config | TEXT | JSON：各模块 show/style 配置 |
| section_order | TEXT | JSON 数组，默认模块顺序 |
| is_official | BOOLEAN DEFAULT FALSE | |
| is_public | BOOLEAN DEFAULT TRUE | |
| created_by | BIGINT | |
| usage_count | INTEGER DEFAULT 0 | |
| created_at / updated_at | TIMESTAMP | 触发器自动维护 |

#### parse_tasks
| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGSERIAL PK | |
| user_id | BIGINT NOT NULL | |
| file_name | VARCHAR | 原始文件名 |
| file_size | BIGINT | |
| status | VARCHAR(20) | pending / processing / success / failed / canceled |
| progress | INTEGER | 0~100 |
| plain_text | TEXT | 文件提取的纯文本 |
| parse_result | TEXT | AI 返回的 JSON 结果 |
| error_message | TEXT | |
| retry_count | INTEGER DEFAULT 0 | |
| max_retries | INTEGER DEFAULT 3 | |
| last_error | TEXT | |
| processing_by | VARCHAR | worker UUID，防重复处理 |
| processing_at | TIMESTAMP | |
| language | VARCHAR(10) DEFAULT 'zh' | zh / en |
| next_try_at | TIMESTAMP | 下次重试时间 |
| created_at / updated_at | TIMESTAMP | |
| completed_at | TIMESTAMP | |

### 2.2 简历 content JSON 结构

```json
{
  "name": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "summary": "个人简介",
  "language": "zh",
  "skills": [{ "name": "Java" }],
  "experience": [{
    "company": "公司名",
    "position": "职位",
    "period": "2022-2024",
    "description": "工作描述"
  }],
  "projects": [{
    "name": "项目名",
    "period": "2023",
    "description": "项目描述",
    "technologies": ["Vue", "Spring Boot"]
  }],
  "education": [{
    "school": "学校",
    "major": "专业",
    "degree": "本科",
    "period": "2018-2022"
  }],
  "honors": [{ "name": "奖项", "date": "2023", "description": "" }],
  "works": [{ "name": "作品名", "description": "描述", "url": "https://..." }]
}
```

### 2.3 模板 theme_config JSON 结构

```json
{
  "primaryColor": "#2c3e50",
  "textColor": "#333333",
  "fontFamily": "Arial, sans-serif",
  "fontSize": 14
}
```

### 2.4 模板 section_config JSON 结构

```json
{
  "header": { "show": true, "style": "left" },
  "summary": { "show": true },
  "skills": { "show": true },
  "experience": { "show": true },
  "projects": { "show": true },
  "education": { "show": true },
  "honors": { "show": true },
  "works": { "show": true }
}
```

---

## 3. 后端模块设计

### 3.1 认证模块

**流程：**
```
POST /api/auth/register
  → AuthServiceImpl.register()
  → 检查 email 唯一性
  → BCryptPasswordEncoder.encode(password)
  → UserRepository.save()

POST /api/auth/login
  → AuthServiceImpl.login()
  → UserRepository.findByEmail()
  → BCryptPasswordEncoder.matches()
  → JwtUtil.generateToken(email)
  → 返回 LoginResponse { token, userInfo }
```

**JWT 机制：**
- 算法：HS256，密钥从 `jwt.secret` 配置读取（≥256 bit）
- 有效期：`jwt.expiration`（默认 86400000ms = 24h）
- Subject：用户 email
- 过滤器：`JwtAuthenticationFilter` 在 `UsernamePasswordAuthenticationFilter` 之前执行，解析 `Authorization: Bearer <token>` 并写入 `SecurityContextHolder`
- 白名单：`/api/auth/**`、`/swagger-ui/**`、`/v3/api-docs/**`、`/actuator/health`

### 3.2 简历模块

**核心类：**
- `ResumeController` → `ResumeService` → `ResumeMapper`（MyBatis）
- `ResumeRenderService` → 模板 + content JSON → HTML 字符串

**渲染流程：**
```
ResumeController.getResumePreview(id)
  → 加载 Resume + Template
  → 合并 sectionOrder（简历自身 > 模板默认）
  → ResumeRenderServiceImpl.renderToHtml()
      ├── 解析 content JSON
      ├── 解析 themeConfig → CSS 变量
      ├── 按 sectionOrder 遍历，逐模块生成 HTML
      └── 返回完整 HTML 字符串
```

**导出：**
- Word：`exportToWord()` 在 HTML `<html>` 标签加入 Word 命名空间声明，输出 `.doc` 文件（Word 可直接打开）
- PDF：当前返回 HTML，浏览器打印另存为 PDF（待升级为服务端 PDF 生成）

**模块默认顺序：**
`["header", "summary", "skills", "experience", "projects", "works", "education", "honors"]`

### 3.3 模板模块

**核心类：**
- `TemplateController` → `TemplateService` → `TemplateMapper`

**关键字段说明：**
- `layout`：渲染布局类型（classic / modern / minimalist / creative）
- `themeConfig`：主题 JSON（颜色、字体）
- `sectionConfig`：各模块显示/样式配置
- `sectionOrder`：模块渲染顺序 JSON 数组

### 3.4 AI 解析模块

**异步任务流程：**

```
POST /api/ai/parse-resume (multipart file + language)
  │
  ├── FileParserUtil.extractText(file)
  │     ├── PDF → PDFBox
  │     ├── .doc → Apache POI HWPF
  │     ├── .docx → Apache POI XWPF
  │     └── .txt → 直接读取
  │
  ├── ParseTaskService.createTask() → INSERT parse_tasks (status=pending)
  │
  └── ApplicationEventPublisher.publishEvent(TaskCreatedEvent)
        │
        └── [AFTER_COMMIT] AIParseListener.onTaskCreated()  @Async("aiParsingExecutor")
              ├── taskService.claimTaskForProcessing(taskId, workerId)  ← 原子更新防重复
              ├── rateLimiter.tryAcquire(5s)  ← 全局 3 QPS
              ├── ResumeParserServiceImpl.parse(plaintext, language)
              │     └── ChatModel.call(prompt) → SiliconFlow Qwen2.5-72B
              ├── taskService.saveParseResult()
              └── taskService.updateTaskStatus("success")
```

**失败重试策略：**
- 指数退避：delay = min(2^retryCount * 2, 120) 秒
- 最大重试次数：3（可配置）
- 超过上限 → 状态置为 `failed`
- `RetryScheduler` 每 30 秒扫描 `next_try_at <= now` 的 pending 任务，重新认领执行

**AI 提示词设计原则：**
- 严格要求原文搬运，禁止修改/润色/翻译
- 输出固定 JSON schema（name, email, phone, summary, skills, experience, projects, education, honors, works）
- 支持中英文语言参数，英文简历加 `"The resume is in English."` 指令
- 自动清理 AI 响应中的 ` ```json ` 代码块标记

**线程池配置（AsyncConfig）：**
```
aiParsingExecutor:
  corePoolSize: 2
  maxPoolSize: 4
  queueCapacity: 100
  threadNamePrefix: "ai-parse-"

aiRateLimiter: RateLimiter.create(3.0)  ← AIParseListener 和 RetryScheduler 共享
```

---

## 4. 前端模块设计

### 4.1 目录结构

```
frontend/src/
  api/
    auth.js       # login, register
    resume.js     # CRUD, copy, export, fromParsed
    template.js   # CRUD, list
    ai.js         # parseResume, getTasks, cancelTask
  pages/
    Login/index.vue
    Register/index.vue
    Resume/
      List.vue    # 简历列表 + 导入入口
      Editor.vue  # 左侧表单 + 右侧实时预览
    Template/
      Market.vue  # 模板市场
      List.vue    # 我的模板
      Edit.vue    # 模板编辑器
      Preview.vue
      components/TemplatePreview.vue
    AI/
      Generate.vue
    ParseTask/
      List.vue    # 解析任务列表
  components/
    ResumeRenderer.vue   # 简历预览组件（iframe 渲染后端 HTML）
    ResumeImport.vue     # 文件上传 + 解析结果预览弹窗
  stores/
    user.js       # Pinia：token, userInfo, login/logout
  router/
    index.js      # 路由 + 登录守卫
```

### 4.2 路由设计

| 路径 | 组件 | 守卫 |
|------|------|------|
| `/login` | Login/index.vue | 公开 |
| `/register` | Register/index.vue | 公开 |
| `/resumes` | Resume/List.vue | 需登录 |
| `/resumes/:id/edit` | Resume/Editor.vue | 需登录 |
| `/templates` | Template/Market.vue | 需登录 |
| `/templates/my` | Template/List.vue | 需登录 |
| `/templates/:id/edit` | Template/Edit.vue | 需登录 |
| `/ai/generate` | AI/Generate.vue | 需登录 |
| `/ai/tasks` | ParseTask/List.vue | 需登录 |

### 4.3 状态管理（Pinia）

```js
// stores/user.js
{
  state: { token, userInfo },
  actions: {
    login(credentials),   // 调用 api/auth.js，存 token 到 localStorage
    logout(),             // 清除 token，跳转 /login
    loadFromStorage()     // 页面刷新时恢复状态
  }
}
```

### 4.4 axios 拦截器

```js
// api/index.js
请求拦截：自动附加 Authorization: Bearer <token>
响应拦截：
  - code !== 0 → 抛出错误，显示 TDesign MessagePlugin
  - HTTP 401 → 清除 token，跳转 /login
```

### 4.5 简历编辑器（Editor.vue）

```
┌─────────────────────────────────────────────────────┐
│  左侧：表单编辑区（TDesign Form）                      │
│  ├── 基本信息（姓名、邮箱、电话、个人简介）              │
│  ├── 各模块折叠面板（技能、工作经历、项目等）            │
│  └── 语言切换（zh/en）                               │
│                                                     │
│  右侧：实时预览（ResumeRenderer.vue）                  │
│  └── iframe 加载 GET /api/resumes/:id/preview        │
└─────────────────────────────────────────────────────┘
```

### 4.6 简历导入流程（ResumeImport.vue）

```
用户点击"导入简历"
  → 弹出 ResumeImport 对话框
  → 拖拽/选择文件 + 选择语言（zh/en）
  → POST /api/ai/parse-resume (multipart)
  → 返回 ParseTaskResponse { taskId, status }
  → 轮询 GET /api/ai/tasks/:id 直到 status=success
  → 展示解析结果（可编辑）
  → 确认 → POST /api/resumes/from-parsed
  → 跳转到 /resumes/:id/edit
```

---

## 5. API 接口汇总

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录，返回 JWT |

### 简历
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/resumes` | 列表（分页，可按 status 过滤） |
| GET | `/api/resumes/{id}` | 详情 |
| POST | `/api/resumes` | 创建 |
| PUT | `/api/resumes/{id}` | 更新 |
| DELETE | `/api/resumes/{id}` | 删除 |
| POST | `/api/resumes/{id}/copy` | 复制 |
| GET | `/api/resumes/{id}/preview` | 返回渲染后的 HTML |
| GET | `/api/resumes/{id}/export/word` | 下载 .doc 文件 |
| GET | `/api/resumes/{id}/export/pdf` | 下载 HTML（浏览器打印为 PDF） |
| POST | `/api/resumes/from-parsed` | 从解析结果创建简历 |

### 模板
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/templates` | 列表 |
| GET | `/api/templates/{id}` | 详情 |
| POST | `/api/templates` | 创建 |
| PUT | `/api/templates/{id}` | 更新 |
| DELETE | `/api/templates/{id}` | 删除 |
| GET | `/api/templates/category/{category}` | 按分类查询 |

### AI / 解析任务
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/parse-resume` | 上传文件，创建异步解析任务 |
| POST | `/api/ai/parse-resume-sync` | 同步解析（备用） |
| GET | `/api/ai/tasks` | 任务列表（分页） |
| GET | `/api/ai/tasks/{id}` | 任务详情/状态 |
| POST | `/api/ai/tasks/{id}/cancel` | 取消任务 |

---

## 6. 安全设计

- 密码：BCrypt 加密存储，不可逆
- JWT：HS256 签名，Subject 为 email，24h 有效期，密钥从环境变量注入
- 接口权限：`/api/**` 全部需要有效 JWT，`/api/auth/**` 公开
- CSRF：禁用（无状态 JWT 不需要）
- Session：STATELESS
- API Key：通过环境变量 `SPRING_AI_OPENAI_API_KEY` 注入，禁止硬编码提交

---

## 7. 模块注册表设计

### 7.1 问题背景

重构前，同一份模块列表散落在 5 处，新增模块需同步修改所有位置：

| 位置 | 硬编码内容 |
|------|-----------|
| `ResumeRenderServiceImpl.java` | `switch(sectionName)` 8 个 case + 默认 sectionOrder |
| `ResumeRenderer.vue` | `defaultSectionOrder` + `sectionLabels` 中英文映射 |
| `ResumeRenderer.vue` | 每个 `v-else-if="sectionKey === 'xxx'"` 模板块 |
| `Editor.vue` | 每个模块的表单区块 |

### 7.2 后端：ResumeSectionEnum

```java
// src/main/java/com/resume/enums/ResumeSectionEnum.java
public enum ResumeSectionEnum {
    HEADER("header", "个人信息", "Contact"),
    SUMMARY("summary", "个人简介", "Professional Summary"),
    SKILLS("skills", "专业技能", "Skills"),
    EXPERIENCE("experience", "工作经历", "Work Experience"),
    PROJECTS("projects", "项目经历", "Projects"),
    WORKS("works", "个人作品", "Portfolio"),
    EDUCATION("education", "教育经历", "Education"),
    HONORS("honors", "荣誉证书", "Awards & Honors");

    private final String key;
    private final String labelZh;
    private final String labelEn;

    // DEFAULT_ORDER 从枚举声明顺序自动推导
    public static final List<String> DEFAULT_ORDER =
        Arrays.stream(values()).map(ResumeSectionEnum::getKey).toList();

    public static Optional<ResumeSectionEnum> fromKey(String key) { ... }
}
```

`ResumeRenderServiceImpl` 将 switch 替换为 `Map<String, RenderFunction>`，在静态初始化块中按枚举注册渲染器，渲染循环变为：

```java
for (String sectionName : sectionOrder) {
    ResumeSectionEnum.fromKey(sectionName).ifPresent(section -> {
        String html = RENDERERS.get(section).render(content, sections, lang);
        if (html != null) builder.append(html);
    });
}
```

### 7.3 前端：sectionRegistry.js

```js
// frontend/src/config/sectionRegistry.js
export const SECTION_REGISTRY = [
  { key: 'header',     labels: { zh: '个人信息', en: 'Contact' },              contentKey: null,         defaultItem: null },
  { key: 'summary',    labels: { zh: '个人简介', en: 'Professional Summary' },  contentKey: 'summary',    defaultItem: null },
  { key: 'skills',     labels: { zh: '专业技能', en: 'Skills' },                contentKey: 'skills',     defaultItem: { name: '', level: 80 } },
  { key: 'experience', labels: { zh: '工作经历', en: 'Work Experience' },        contentKey: 'experience', defaultItem: { company: '', position: '', period: '', description: '' } },
  { key: 'projects',   labels: { zh: '项目经历', en: 'Projects' },              contentKey: 'projects',   defaultItem: { name: '', period: '', description: '', technologiesString: '' } },
  { key: 'works',      labels: { zh: '个人作品', en: 'Portfolio' },             contentKey: 'works',      defaultItem: { name: '', description: '', url: '' } },
  { key: 'education',  labels: { zh: '教育经历', en: 'Education' },             contentKey: 'education',  defaultItem: { school: '', major: '', degree: '', period: '' } },
  { key: 'honors',     labels: { zh: '荣誉证书', en: 'Awards & Honors' },       contentKey: 'honors',     defaultItem: { name: '', date: '', description: '' } },
]

export const DEFAULT_SECTION_ORDER = SECTION_REGISTRY.map(s => s.key)
export const getSectionMeta = (key) => SECTION_REGISTRY.find(s => s.key === key)
```

`ResumeRenderer.vue` 的 `sectionLabels` 和 `defaultSectionOrder` 直接从注册表读取，不再硬编码。

### 7.4 JSON 导入导出设计

**导出（前端纯客户端）：**
```
Editor.vue 导出按钮 → 新增"导出 JSON"选项
  → 构造 { _version: "1", title, content }
  → JSON.stringify + Blob 下载，文件名 "{title}.resume.json"
```

**导入（前端纯客户端）：**
```
Resume/List.vue 导入入口 → 新增"从 JSON 导入"选项
  → 读取 .json 文件
  → 校验 _version 字段存在
  → 提取 title + content
  → POST /api/resumes（走现有创建接口）
  → 跳转到编辑器
```

导入导出均为纯前端操作，不需要新增后端接口。

---

## 8. 待实现功能设计（规划）

### 7.1 简历翻译

**方案：**
- 新增 Flyway 迁移 V9：`resumes` 表加 `language VARCHAR(10) DEFAULT 'zh'` 和 `translated_from_id BIGINT`
- 新增 Flyway 迁移 V10：`templates` 表加 `section_labels JSONB`（多语言标签映射）
- 新增 `ResumeTranslateService`，调用 Spring AI 翻译 content 各字段
- 新增接口 `POST /api/ai/translate-resume/{id}`
- 前端：简历列表/编辑器加"翻译为英文"按钮，翻译完成后打开新简历

**section_labels 结构：**
```json
{
  "zh": { "experience": "工作经历", "education": "教育经历", ... },
  "en": { "experience": "Work Experience", "education": "Education", ... }
}
```

### 7.2 AI 对话式简历优化

**方案：**
- 新增 Flyway 迁移：创建 `ai_conversations` 表（id, resume_id, user_id, messages JSONB, stage, created_at, updated_at）
- 新增 `OptimizerChatService`，实现状态机：`evaluation → direction → content_select → suggestion → confirmation`
- 新增接口：
  - `POST /api/ai/optimize/chat`
  - `GET /api/ai/optimize/conversation/{id}`
  - `POST /api/ai/optimize/apply-suggestion`
- 前端：新增 `AiOptimizeChat.vue` 聊天界面，嵌入编辑器右侧或独立页面

### 7.3 AiController 用户 ID 修复

当前 `AiController` 中 `userId` 硬编码为 `1L`，需改为从 `SecurityContextHolder` 获取，与 `ResumeController` 保持一致：

```java
private Long getCurrentUserId() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email).map(User::getId).orElseThrow();
}
```

---

## 8. 技术债务

| 问题 | 优先级 | 说明 |
|------|--------|------|
| AiController userId 硬编码为 1L | 高 | 影响多用户场景 |
| 登出功能缺失 | 中 | 前端清除 token 即可，后端可加 token 黑名单 |
| Token 自动刷新 | 中 | 前端拦截 401 后刷新 token |
| 缺少单元/集成测试 | 中 | 核心 Service 层需覆盖 |
| PDF 导出为服务端生成 | 低 | 当前返回 HTML，依赖浏览器打印 |
| 前后端状态值统一 | 低 | parse_tasks.status 已修复，需全面排查 |
