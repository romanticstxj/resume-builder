# 系统架构设计文档

## 1. 总体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      用户层                                 │
│  Web浏览器 / 移动浏览器                                    │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP/HTTPS
                        ↓
┌─────────────────────────────────────────────────────────────────┐
│                   Nginx 反向代理                              │
│              (负载均衡 + 静态资源 + SSL)                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot 后端                           │
│  ┌──────────────┬──────────────┬──────────────┬──────────┐ │
│  │  简历模块    │  模板模块      │  AI模块      │ 面试模块 │ │
│  │  (一期)      │  (一期)       │  (一期)      │ (二期)    │ │
│  └──────┬───────┴──────┬───────┴──────┬───────┴────────┘ │
└─────────┼────────────────┼──────────────┼──────────────────┘
          │                │              │
          ↓                ↓              ↓
    ┌─────────┐    ┌────────┐  ┌────────────┐
    │  MySQL  │    │ Redis  │  │ AI服务API    │
    │  数据库  │    │  缓存  │  │(智谱/OpenAI)│
    └─────────┘    └────────┘  └────────────┘
                                       │
                                       ↓
                              ┌────────────┐
                              │ Oss存储    │
                              │ 文件/模板  │
                              └────────────┘
```

## 2. 技术栈分层

### 2.1 表现层 (Presentation Layer)

```
浏览器/Vue3客户端
    ↓ HTTP/REST
┌─────────────────────────────────────┐
│      Spring Boot Controller      │
│  - ResumeController            │
│  - TemplateController          │
│  - AuthController            │
│  - AiController             │
│  - InterviewController       │
└─────────────────────────────────────┘
```

### 2.2 业务逻辑层 (Service Layer)

```
┌─────────────────────────────────────┐
│      Service Interface          │
│  - ResumeService              │
│  - TemplateService            │
│  - ExportService             │
│  - AiService                │
│  - InterviewService          │
└─────────────┬───────────────────┘
              │
    ┌─────────┴──────────┐
    │                    │
┌───────────┐      ┌──────────────┐
│ServiceImpl│      │AiServiceImpl │
└───────────┘      └──────────────┘
```

### 2.3 数据访问层 (Repository Layer)

```
┌─────────────────────────────────────┐
│    MyBatis Plus Repository     │
│  - UserRepository            │
│  - ResumeRepository           │
│  - TemplateRepository         │
│  - InterviewRepository      │
└─────────────┬───────────────────┘
              │
              ↓
      ┌─────────────┐
      │   MySQL     │
      └─────────────┘
```

## 3. 核心组件设计

### 3.1 PDF导出组件

```java
public interface PdfExporter {
    byte[] export(ResumeContent content, Template template);
}

@Component
public class PdfExporterImpl implements PdfExporter {

    @Override
    public byte[] export(ResumeContent content, Template template) {
        // 1. 加载模板
        Document document = loadTemplate(template);

        // 2. 填充数据
        fillData(document, content);

        // 3. 生成PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos).close();

        return baos.toByteArray();
    }
}
```

### 3.2 Word导出组件

```java
public interface WordExporter {
    byte[] export(ResumeContent content, Template template);
}

@Component
public class WordExporterImpl implements WordExporter {

    @Override
    public byte[] export(ResumeContent content, Template template) {
        // 1. 加载Word模板
        XWPFDocument doc = loadTemplate(template);

        // 2. 替换占位符
        replacePlaceholders(doc, content);

        // 3. 导出
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.write(baos);

        return baos.toByteArray();
    }
}
```

### 3.3 AI服务组件

```java
@Service
public class AiService {

    @Autowired
    private AiProvider aiProvider; // 智谱AI/OpenAI

    public ResumeContent generateResume(GenerateRequest request) {
        String prompt = buildGeneratePrompt(request);

        AiResponse response = aiProvider.chat(prompt);

        return parseResumeContent(response.getContent());
    }

    public String translateResume(Long resumeId, String targetLang) {
        Resume resume = resumeRepository.findById(resumeId);
        String prompt = buildTranslatePrompt(resume, targetLang);

        AiResponse response = aiProvider.chat(prompt);

        return response.getContent();
    }

    public MatchResult matchResume(Long resumeId, Long jdId) {
        Resume resume = resumeRepository.findById(resumeId);
        JobDescription jd = jdRepository.findById(jdId);

        String prompt = buildMatchPrompt(resume, jd);

        AiResponse response = aiProvider.chat(prompt);

        return parseMatchResult(response.getContent());
    }
}
```

### 3.4 模板引擎

```java
@Component
public class TemplateEngine {

    private Map<String, TemplateRenderer> renderers;

    @PostConstruct
    public void init() {
        renderers = Map.of(
            "classic", new ClassicTemplateRenderer(),
            "modern", new ModernTemplateRenderer(),
            "minimal", new MinimalTemplateRenderer()
        );
    }

    public String render(String templateType, ResumeContent content) {
        TemplateRenderer renderer = renderers.get(templateType);
        return renderer.render(content);
    }
}
```

## 4. 数据流设计

### 4.1 简历创建流程

```
用户填写简历表单
    ↓
前端验证
    ↓
POST /api/v1/resumes
    ↓
后端验证
    ↓
保存到MySQL (JSON格式)
    ↓
返回简历ID
    ↓
用户选择模板
    ↓
POST /api/v1/resumes/{id}/export/pdf
    ↓
加载模板 + 填充数据
    ↓
生成PDF文件
    ↓
返回文件流下载
```

### 4.2 AI生成简历流程

```
用户输入职位信息
    ↓
POST /api/v1/ai/generate
    ↓
构建AI提示词
    ↓
调用智谱AI API
    ↓
异步处理 (返回taskId)
    ↓
AI返回简历JSON
    ↓
解析并保存为新简历
    ↓
用户查看并调整
    ↓
导出PDF/Word
```

### 4.3 面试管理流程 (二期)

```
用户上传JD
    ↓
AI分析JD (提取关键词)
    ↓
用户选择简历
    ↓
AI匹配分析
    ↓
生成优化建议 + 新简历版本
    ↓
用户确认调整
    ↓
创建面试记录
    ↓
追踪面试进度
    ↓
录入反馈
    ↓
统计分析
```

## 5. 扩展点设计

### 5.1 插件化模板系统

```java
public interface TemplatePlugin {
    String getName();
    String getCategory();
    String getThumbnail();
    String render(ResumeContent content);
}

// 实现插件
@Component
public class TechTemplatePlugin implements TemplatePlugin {
    @Override
    public String render(ResumeContent content) {
        // 技术类模板渲染逻辑
    }
}
```

### 5.2 多AI提供商支持

```java
public interface AiProvider {
    AiResponse chat(String prompt);
    String getProviderName();
}

@Component("zhipu")
public class ZhipuAiProvider implements AiProvider { ... }

@Component("openai")
public class OpenAiProvider implements AiProvider { ... }

@Service
public class AiService {
    @Autowired
    private Map<String, AiProvider> providers;

    public AiResponse chat(String prompt, String provider) {
        return providers.get(provider).chat(prompt);
    }
}
```

### 5.3 导出格式扩展

```java
public interface FileExporter {
    byte[] export(ResumeContent content, Template template);
    String getMimeType();
    String getFileExtension();
}

@Component("pdf")
public class PdfExporter implements FileExporter { ... }

@Component("word")
public class WordExporter implements FileExporter { ... }

@Component("html")
public class HtmlExporter implements FileExporter { ... }
```

## 6. 性能优化策略

### 6.1 缓存策略

```
热点数据缓存:
- 模板列表 (Redis, 1小时)
- 官方模板详情 (Redis, 24小时)
- 用户信息 (Redis, 30分钟)
- AI任务结果 (Redis, 24小时)
```

### 6.2 数据库优化

```
索引优化:
- users: idx_email, idx_username
- resumes: idx_user_id, idx_status
- templates: idx_category, idx_is_official
- interviews: idx_user_id, idx_status, idx_interview_time

分表策略:
- ai_tasks: 按月分表 (保留6个月)
- resume_versions: 冷数据归档
```

### 6.3 异步处理

```
耗时任务异步化:
- AI生成任务: 使用线程池
- PDF导出: 使用队列
- 邮件发送: 异步任务
```

## 7. 安全设计

### 7.1 认证授权

```
JWT Token认证:
- 登录成功返回token (有效期24小时)
- 每次请求携带token
- Token过期自动刷新

权限控制:
- 用户只能访问自己的简历
- 管理员可管理模板
- API接口权限注解
```

### 7.2 数据安全

```
敏感数据加密:
- 密码: BCrypt加密
- JWT: 签名验证
- PDF/Word: 临时文件自动删除

SQL注入防护:
- MyBatis参数化查询
- 输入校验
```

## 8. 部署架构

### 8.1 Docker化部署

```
version: '3'
services:
  backend:
    image: resume-builder-backend:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    volumes:
      - mysql-data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=xxx

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

### 8.2 CI/CD流程

```
Git Push
    ↓
GitHub Actions
    ↓
编译测试
    ↓
构建Docker镜像
    ↓
推送到镜像仓库
    ↓
部署到生产环境
    ↓
健康检查
```
