# 简历制作工具 - 项目设计文档

## 项目概述

一个智能简历制作与管理平台,支持简历创建、多模板管理、AI辅助优化、PDF/Word导出,以及未来的面试管理功能。

**技术栈:**
- 后端: Spring Boot 3.x + MySQL
- 前端: Vue 3 + TDesign + Pinia
- AI集成: 智谱AI / OpenAI
- 导出: iText (PDF) + Apache POI (Word)

---

## 目录

1. [系统架构](#系统架构)
2. [扩展性分析](#扩展性分析)
3. [功能模块](#功能模块)
4. [数据库设计](#数据库设计)
5. [API接口设计](#api接口设计)
6. [二期规划:面试管理](#二期规划面试管理)

---

## 系统架构

### 技术选型

| 层级 | 技术 | 说明 |
|-------|------|------|
| 后端框架 | Spring Boot 3.x | 成熟稳定,生态丰富 |
| 数据库 | MySQL 8.0 | 开源免费,适合中小型应用 |
| ORM | MyBatis Plus | 简化CRUD操作 |
| PDF生成 | iText 7 | 功能强大,支持中文 |
| Word生成 | Apache POI | Apache官方库,兼容性好 |
| 前端框架 | Vue 3 | 轻量高效,学习成本低 |
| UI组件库 | TDesign Vue Next | 腾讯开源,设计优秀 |
| 状态管理 | Pinia | Vue 3官方推荐 |
| AI服务 | 智谱AI/OpenAI | 国内访问稳定 |
| 认证授权 | JWT + Spring Security | 无状态认证 |

### 项目结构

**后端结构:**
```
resume-builder-backend/
├── src/main/java/com/resume/
│   ├── ResumeApplication.java          # 启动类
│   ├── config/                      # 配置类
│   │   ├── SecurityConfig.java        # 安全配置
│   │   ├── AiConfig.java            # AI配置
│   │   ├── StorageConfig.java        # 存储配置
│   │   └── CorsConfig.java         # 跨域配置
│   ├── controller/                  # 控制器
│   │   ├── ResumeController.java     # 简历API
│   │   ├── TemplateController.java   # 模板API
│   │   ├── AuthController.java      # 认证API
│   │   ├── AiController.java       # AI功能API
│   │   └── InterviewController.java # 面试管理API(二期)
│   ├── service/                    # 服务层
│   │   ├── impl/                 # 服务实现
│   │   ├── ResumeService.java
│   │   ├── TemplateService.java
│   │   ├── ExportService.java
│   │   ├── AiService.java
│   │   └── InterviewService.java  # 面试服务(二期)
│   ├── repository/                 # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── ResumeRepository.java
│   │   ├── TemplateRepository.java
│   │   └── InterviewRepository.java
│   ├── entity/                    # 实体类
│   │   ├── User.java
│   │   ├── Resume.java
│   │   ├── ResumeVersion.java
│   │   ├── Template.java
│   │   ├── Interview.java          # 面试实体(二期)
│   │   └── JobDescription.java    # JD实体(二期)
│   ├── dto/                       # 数据传输对象
│   │   ├── request/              # 请求DTO
│   │   ├── response/             # 响应DTO
│   │   └── common/              # 通用DTO
│   ├── enums/                     # 枚举类
│   │   ├── ResumeStatus.java
│   │   ├── TaskStatus.java
│   │   └── InterviewStatus.java   # 面试状态(二期)
│   └── util/                      # 工具类
│       ├── JwtUtil.java
│       ├── PdfExportUtil.java
│       ├── WordExportUtil.java
│       ├── JsonUtil.java
│       └── AiPromptBuilder.java  # AI提示词构建
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/              # 数据库迁移脚本
│       ├── V1__init.sql
│       ├── V2__add_interview_tables.sql
│       └── V3__add_indexes.sql
└── pom.xml
```

**前端结构:**
```
resume-builder-frontend/
├── src/
│   ├── App.vue                   # 根组件
│   ├── main.js                   # 入口文件
│   ├── api/                     # API接口
│   │   ├── index.js
│   │   ├── resume.js
│   │   ├── template.js
│   │   ├── auth.js
│   │   ├── ai.js
│   │   └── interview.js         # 面试API(二期)
│   ├── components/               # 公共组件
│   │   ├── RichTextEditor.vue    # 富文本编辑器
│   │   ├── ResumePreview.vue     # 简历预览
│   │   ├── TemplateCard.vue      # 模板卡片
│   │   └── InterviewTimeline.vue # 面试时间轴(二期)
│   ├── pages/                   # 页面
│   │   ├── Login/
│   │   │   └── index.vue
│   │   ├── Dashboard/
│   │   │   └── index.vue
│   │   ├── Resume/
│   │   │   ├── List.vue
│   │   │   ├── Editor.vue
│   │   │   └── Preview.vue
│   │   ├── Template/
│   │   │   ├── Market.vue        # 模板市场
│   │   │   └── Editor.vue       # 模板编辑
│   │   ├── AI/
│   │   │   ├── Generate.vue      # AI生成
│   │   │   ├── Translate.vue     # AI翻译
│   │   │   └── Optimize.vue     # AI优化
│   │   └── Interview/          # 面试管理(二期)
│   │       ├── List.vue         # 面试列表
│   │       ├── Detail.vue       # 面试详情
│   │       ├── JDMatch.vue      # JD匹配
│   │       └── Feedback.vue     # 反馈录入
│   ├── router/                  # 路由配置
│   │   └── index.js
│   ├── stores/                  # Pinia状态管理
│   │   ├── user.js
│   │   ├── resume.js
│   │   └── interview.js       # 面试状态(二期)
│   └── utils/                   # 工具函数
│       ├── request.js
│       ├── format.js
│       └── validate.js
├── package.json
└── vite.config.js
```

---

## 扩展性分析

### 1. 架构扩展性

**优势:**

✅ **微服务友好**
- 模块化设计(简历、模板、AI、面试)
- 各模块独立,可拆分为微服务
- 支持水平扩展和独立部署

✅ **技术栈灵活**
- Spring Boot 易于集成新功能
- 前后端分离,独立演进
- 数据库可替换(MySQL → PostgreSQL/MongoDB)
- AI提供商可热切换

✅ **插件化设计**
- 模板系统支持热插拔
- AI功能可扩展到多提供商
- 导出格式可扩展(PDF/Word/HTML/Markdown)

✅ **数据存储灵活**
- JSON存储简历内容,无需频繁改表结构
- 版本控制设计,支持回滚
- 支持多租户扩展

### 2. 功能扩展性

| 扩展方向 | 扩展难度 | 说明 |
|-----------|-----------|------|
| 新增简历字段 | ⭐ | JSON存储,无需改表结构 |
| 新增导出格式 | ⭐⭐ | 实现新的Exporter接口 |
| 新增AI功能 | ⭐⭐ | 已有AI模块,易扩展 |
| 多语言支持 | ⭐⭐ | 前端i18n + 后端国际化 |
| 团队协作 | ⭐⭐⭐ | 需增加权限和版本控制 |
| 面试管理 | ⭐⭐ | 依赖简历模块,可快速集成 |
| 简历分享 | ⭐ | 增加分享链接生成 |
| 在线协作 | ⭐⭐⭐⭐ | WebSocket实时协作 |

### 3. 性能扩展性

**优化点:**

- **数据库层**: 读写分离,主从复制
- **缓存层**: Redis缓存热点数据(模板、用户信息)
- **静态资源**: CDN加速(PDF、图片)
- **文件存储**: 支持OSS对象存储
- **导出优化**: 异步任务队列,避免阻塞

---

## 功能模块

### 一期功能 (MVP)

#### 1. 用户管理模块
- 用户注册/登录
- JWT认证
- 个人信息管理
- 密码找回

#### 2. 简历管理模块
- 简历CRUD操作
- 简历版本控制
- 简历实时预览
- PDF/Word导出
- 简历复制/删除

#### 3. 模板管理模块
- 官方模板库
- 用户自定义模板
- 模板分类筛选
- 模板预览
- 模板编辑器

#### 4. AI智能模块
- AI生成简历内容(根据职位描述)
- AI中英文翻译
- AI简历优化建议
- AI关键词匹配

### 二期功能 (面试管理)

#### 5. 面试管理模块
- **JD分析**: 提取职位关键词和要求
- **简历匹配**: 根据JD动态调整简历内容
- **面试申请**: 记录投递的公司和职位
- **面试反馈**: 录入面试结果和评价
- **进度追踪**: 可视化面试流程(投递→笔试→面试→Offer)
- **数据统计**: 面试转化率、成功率分析

---

## 数据库设计

### 核心表结构

#### 1. 用户表 (users)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    phone VARCHAR(20) COMMENT '手机号',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 2. 简历表 (resumes)
```sql
CREATE TABLE resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(100) NOT NULL COMMENT '简历标题',
    template_id BIGINT COMMENT '使用的模板ID',
    content JSON NOT NULL COMMENT '简历内容JSON',
    status ENUM('draft', 'completed') DEFAULT 'draft' COMMENT '状态:草稿-完成',
    is_primary BOOLEAN DEFAULT FALSE COMMENT '是否主简历',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES templates(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';
```

#### 3. 简历版本表 (resume_versions)
```sql
CREATE TABLE resume_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    version_number INT NOT NULL COMMENT '版本号',
    content JSON NOT NULL COMMENT '简历内容JSON',
    change_reason VARCHAR(255) COMMENT '变更原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    INDEX idx_resume_id (resume_id),
    INDEX idx_version_number (resume_id, version_number DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历版本表';
```

#### 4. 模板表 (templates)
```sql
CREATE TABLE templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    category VARCHAR(50) NOT NULL COMMENT '分类:技术-设计-产品-其他',
    description TEXT COMMENT '模板描述',
    preview_url VARCHAR(255) COMMENT '预览图URL',
    content JSON NOT NULL COMMENT '模板配置JSON',
    is_official BOOLEAN DEFAULT FALSE COMMENT '是否官方模板',
    is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    created_by BIGINT COMMENT '创建者ID',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_category (category),
    INDEX idx_is_official (is_official),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板表';
```

#### 5. AI任务表 (ai_tasks)
```sql
CREATE TABLE ai_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_id BIGINT COMMENT '关联简历ID',
    task_type ENUM('generate', 'translate', 'optimize', 'match') NOT NULL COMMENT '任务类型:生成-翻译-优化-匹配',
    status ENUM('pending', 'processing', 'completed', 'failed') DEFAULT 'pending' COMMENT '任务状态',
    input_data JSON COMMENT '输入数据',
    result_data JSON COMMENT '结果数据',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI任务表';
```

#### 6. JD表 (job_descriptions) - 二期
```sql
CREATE TABLE job_descriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'JD ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    company_name VARCHAR(100) NOT NULL COMMENT '公司名称',
    job_title VARCHAR(100) NOT NULL COMMENT '职位名称',
    jd_content TEXT NOT NULL COMMENT '职位描述',
    keywords JSON COMMENT '提取的关键词',
    salary_range VARCHAR(50) COMMENT '薪资范围',
    location VARCHAR(50) COMMENT '工作地点',
    job_url VARCHAR(255) COMMENT '职位链接',
    status ENUM('draft', 'applied', 'interview', 'offer', 'rejected') DEFAULT 'draft' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位描述表';
```

#### 7. 面试表 (interviews) - 二期
```sql
CREATE TABLE interviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    jd_id BIGINT COMMENT '关联JD ID',
    resume_id BIGINT COMMENT '使用的简历ID',
    company_name VARCHAR(100) NOT NULL COMMENT '公司名称',
    job_title VARCHAR(100) NOT NULL COMMENT '职位名称',
    interview_type ENUM('phone', 'video', 'onsite', 'technical', 'hr') COMMENT '面试类型',
    interview_time DATETIME COMMENT '面试时间',
    interviewer VARCHAR(100) COMMENT '面试官',
    interview_round INT COMMENT '面试轮次',
    status ENUM('scheduled', 'completed', 'cancelled', 'passed', 'rejected') DEFAULT 'scheduled' COMMENT '状态',
    feedback TEXT COMMENT '面试反馈',
    score INT COMMENT '面试评分(0-100)',
    next_step DATETIME COMMENT '下一步时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (jd_id) REFERENCES job_descriptions(id) ON DELETE SET NULL,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_interview_time (interview_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试表';
```

#### 8. 简历匹配表 (resume_matches) - 二期
```sql
CREATE TABLE resume_matches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '匹配ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    original_resume_id BIGINT NOT NULL COMMENT '原始简历ID',
    jd_id BIGINT NOT NULL COMMENT '关联JD ID',
    matched_resume_id BIGINT COMMENT '优化后的简历ID',
    match_score DECIMAL(5,2) COMMENT '匹配度(0-100)',
    ai_suggestions JSON COMMENT 'AI建议',
    key_points JSON COMMENT '关键点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (original_resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    FOREIGN KEY (jd_id) REFERENCES job_descriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (matched_resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_jd_id (jd_id),
    INDEX idx_match_score (match_score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历匹配表';
```

### 数据ER图

```
users (1) ----< (N) resumes (1) ----< (N) resume_versions
  |
  | (1) ----< (N) templates (1) ----< (N) resumes
  |
  | (1) ----< (N) ai_tasks (0/1) ----< (1) resumes
  |
  | (1) ----< (N) job_descriptions (二期)
  |                       |
  | (1) ----< (N) interviews
  |                       |
  | (1) ----< (N) resume_matches
```

---

## API接口设计

### 简历管理 API

```java
// 创建简历
POST /api/v1/resumes
Request: {
  "title": "Java开发工程师",
  "templateId": 1,
  "content": { /* 简历JSON */ }
}
Response: {
  "code": 200,
  "data": { "id": 123, ... }
}

// 获取简历列表
GET /api/v1/resumes?page=1&pageSize=10&status=draft
Response: {
  "code": 200,
  "data": {
    "total": 50,
    "list": [...]
  }
}

// 获取简历详情
GET /api/v1/resumes/{id}

// 更新简历
PUT /api/v1/resumes/{id}
Request: {
  "title": "新标题",
  "content": { /* 更新后的简历JSON */ }
}

// 删除简历
DELETE /api/v1/resumes/{id}

// 简历预览
GET /api/v1/resumes/{id}/preview

// 导出PDF
GET /api/v1/resumes/{id}/export/pdf?templateId=1

// 导出Word
GET /api/v1/resumes/{id}/export/word?templateId=1

// 复制简历
POST /api/v1/resumes/{id}/copy

// 获取版本历史
GET /api/v1/resumes/{id}/versions

// 创建新版本
POST /api/v1/resumes/{id}/versions
Request: {
  "changeReason": "针对阿里优化"
}
```

### 模板管理 API

```java
// 获取模板列表
GET /api/v1/templates?category=技术&page=1

// 获取模板详情
GET /api/v1/templates/{id}

// 创建自定义模板
POST /api/v1/templates
Request: {
  "name": "我的模板",
  "category": "技术",
  "content": { /* 模板JSON */ }
}

// 更新模板
PUT /api/v1/templates/{id}

// 删除模板
DELETE /api/v1/templates/{id}

// 获取模板分类
GET /api/v1/templates/categories
```

### AI功能 API

```java
// AI生成简历内容
POST /api/v1/ai/generate
Request: {
  "jobTitle": "Java高级工程师",
  "targetCompany": "阿里巴巴",
  "experience": "5年Java开发经验",
  "requirements": ["Spring Boot", "微服务", "分布式"]
}
Response: {
  "taskId": "task-123",
  "status": "processing"
}

// AI翻译(中英文)
POST /api/v1/ai/translate
Request: {
  "resumeId": 123,
  "targetLanguage": "english" // chinese/english
}

// AI优化简历
POST /api/v1/ai/optimize
Request: {
  "resumeId": 123,
  "jobDescription": "职位描述..."
}

// 获取AI任务状态
GET /api/v1/ai/tasks/{id}

// JD匹配(二期)
POST /api/v1/ai/match
Request: {
  "resumeId": 123,
  "jdId": 456
}
Response: {
  "matchScore": 85.5,
  "suggestions": [...],
  "optimizedResumeId": 789
}
```

### 面试管理 API (二期)

```java
// 创建JD记录
POST /api/v1/jds
Request: {
  "companyName": "阿里巴巴",
  "jobTitle": "Java高级工程师",
  "jdContent": "职位描述...",
  "jobUrl": "https://..."
}

// 获取JD列表
GET /api/v1/jds?status=applied

// JD智能匹配简历
POST /api/v1/jds/{jdId}/match
Request: { "resumeId": 123 }
Response: {
  "matchScore": 85,
  "suggestions": "建议增加XX技能",
  "optimizedResumeId": 456
}

// 创建面试记录
POST /api/v1/interviews
Request: {
  "jdId": 123,
  "resumeId": 456,
  "interviewType": "video",
  "interviewTime": "2026-02-10 14:00:00",
  "interviewer": "张经理"
}

// 获取面试列表
GET /api/v1/interviews?status=scheduled

// 更新面试反馈
PUT /api/v1/interviews/{id}/feedback
Request: {
  "status": "completed",
  "feedback": "技术基础扎实...",
  "score": 85,
  "nextStep": "2026-02-15 10:00:00"
}

// 获取面试统计
GET /api/v1/interviews/statistics
Response: {
  "totalInterviews": 20,
  "passedCount": 8,
  "rejectedCount": 12,
  "conversionRate": 40.0
}
```

---

## 二期规划:面试管理

### 功能概述

面试管理模块是对简历系统的扩展,帮助用户:
1. **动态调整简历**: 根据不同公司的JD自动优化简历
2. **追踪面试流程**: 记录从投递到Offer的完整流程
3. **数据分析**: 统计面试成功率,提供改进建议

### 核心功能

#### 1. JD智能分析

**功能:**
- 提取JD中的关键技能关键词
- 分析职位要求和期望
- 生成职位标签

**实现:**
```java
public class JdAnalyzer {
    public JdAnalysisResult analyze(String jdContent) {
        // AI提取关键词
        // 分析技能要求
        // 生成职位画像
    }
}
```

#### 2. 简历智能匹配

**功能:**
- 计算简历与JD的匹配度
- AI生成优化建议
- 自动创建优化后的简历版本

**工作流程:**
```
用户选择简历 + JD
    ↓
AI分析差异点
    ↓
生成优化建议
    ↓
创建新简历版本(针对该JD)
    ↓
用户确认/手动调整
```

**AI提示词示例:**
```
以下是我的简历内容和目标职位的JD,请帮我:
1. 评估匹配度(0-100)
2. 列出简历中缺失的关键技能
3. 优化简历内容(突出与JD匹配的部分)
4. 生成针对性的项目经历描述

简历内容: {resume}
JD内容: {jd}
```

#### 3. 面试流程追踪

**状态机:**
```
投递 → 笔试 → 面试 → 复试 → Offer
  ↓       ↓      ↓      ↓      ↓
 已拒绝   已拒绝  已拒绝  已拒绝  已拒绝
```

**数据可视化:**
- 时间轴展示面试流程
- 面试转化漏斗图
- 公司投递分布图

#### 4. 面试反馈管理

**功能:**
- 录入面试问题
- 记录回答情况
- 面试官评价
- 改进建议

**数据结构:**
```json
{
  "questions": [
    {
      "question": "讲一下你做过最有挑战的项目?",
      "answer": "...",
      "evaluation": "回答清晰,但可增加数据支撑"
    }
  ],
  "overallFeedback": "...",
  "nextStep": "二面:2月15日"
}
```

#### 5. 数据统计分析

**指标:**
- 面试转化率: Offer数 / 面试总数
- 平均面试轮次: 总轮次 / 面试数
- 成功率最高的公司类型
- 需要提升的技能统计

**报表:**
- 月度面试趋势图
- 技能匹配度热力图
- 面试反馈词云图

### 数据流图

```
用户上传JD
    ↓
系统分析JD (AI提取关键词)
    ↓
用户选择简历
    ↓
AI匹配分析 (简历 vs JD)
    ↓
生成优化建议 + 创建新简历版本
    ↓
用户确认调整
    ↓
开始面试流程
    ↓
录入面试反馈
    ↓
数据分析 & 改进建议
```

---

## 开发计划

### 阶段1: 核心功能 (2周)

**后端:**
- [x] 用户认证模块
- [x] 简历CRUD
- [ ] 模板基础管理
- [ ] PDF/Word导出

**前端:**
- [x] 登录/注册页面
- [x] 简历列表和编辑器
- [x] 模板市场页面
- [ ] 导出功能

### 阶段2: 增强功能 (1周)

**后端:**
- [ ] 简历版本控制
- [ ] 模板编辑器
- [ ] 富文本存储

**前端:**
- [ ] 版本历史页面
- [ ] 模板编辑器
- [ ] 富文本编辑器集成

### 阶段3: AI功能 (1周)

**后端:**
- [ ] AI服务集成
- [ ] 简历生成API
- [ ] 中英文翻译API
- [ ] 简历优化API

**前端:**
- [ ] AI生成页面
- [ ] 翻译功能
- [ ] 优化建议页面

### 阶段4: 面试管理 (2周) - 二期

**后端:**
- [ ] JD表和API
- [ ] 面试表和API
- [ ] JD匹配服务
- [ ] 统计分析API

**前端:**
- [ ] JD管理页面
- [ ] 面试列表和详情
- [ ] 匹配结果页面
- [ ] 数据统计图表

### 阶段5: 优化部署 (1周)

- [ ] 性能优化
- [ ] 安全加固
- [ ] Docker部署
- [ ] CI/CD配置

---

## 总结

本设计提供了:
- ✅ 完整的系统架构和技术选型
- ✅ 良好的扩展性(支持微服务和二期功能)
- ✅ 一期核心功能(简历制作、模板管理、AI辅助)
- ✅ 二期规划(面试管理、JD匹配、流程追踪)
- ✅ 详细的数据库设计和API文档

**项目启动建议:**
1. 先实现一期MVP,快速验证核心价值
2. 积累用户反馈后规划二期开发
3. 面试管理作为独立模块,可独立迭代

---

**文档版本:** v1.0
**创建时间:** 2026-02-03
**最后更新:** 2026-02-03
