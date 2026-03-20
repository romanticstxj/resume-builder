# AI功能开发 Sprint

## 概述
本Sprint包含三个AI功能模块的开发，旨在提升简历管理的智能化水平。

## 功能模块

---

## 模块1: 简历解析与导入

### 需求描述
支持上传PDF/Word/Text格式的现有简历，AI自动解析内容并导入到系统，生成新简历。

### 功能列表

#### 1.1 后端 - 文件上传与解析
- [ ] 创建 `AiController.java`
  - `POST /api/ai/parse-resume` 接口
  - 接收multipart/form-data文件上传
  - 文件格式校验（.pdf, .doc, .docx, .txt）
  - 文件大小限制（最大10MB）

- [ ] 创建 `ResumeParser.java` 简历解析服务
  - 实现文本提取（PDF解析、Word解析、纯文本）
  - AI结构化解析（返回JSON格式）
  - 数据映射到Resume.content结构
  - 错误处理与部分导入策略

- [ ] AI解析提示词设计
  - 结构化输出格式定义
  - 字段识别规则（姓名、邮箱、电话、工作经历等）
  - 处理缺失字段的策略

#### 1.2 前端 - 导入界面
- [ ] 创建 `ResumeImport.vue` 组件
  - 拖拽上传区域
  - 文件选择按钮
  - 上传进度显示
  - 支持的格式提示

- [ ] 解析结果预览
  - 预览弹窗组件
  - 可编辑的字段
  - 保存/取消按钮
  - 错误提示

- [ ] 路由集成
  - 在简历列表页添加"导入简历"按钮
  - 路由配置 `/resumes/import`

#### 1.3 数据库
- [ ] 无需新增表，复用现有 `resumes` 表

### 接口定义
```
POST /api/ai/parse-resume
Request:
  Content-Type: multipart/form-data
  file: binary
Response:
  {
    "success": true,
    "data": {
      "title": "从导入的简历",
      "content": {
        "name": "张三",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "summary": "...",
        "experience": [...],
        "education": [...],
        "projects": [...],
        "skills": [...],
        "personalSummary": "...",
        "honors": [...],
        "works": [...]
      }
    }
  }
```

---

## 模块2: 简历翻译（国际化）

### 需求描述
将中文简历翻译为英文，支持模板国际化，建立翻译简历的关联关系。

### 功能列表

#### 2.1 数据库变更
- [ ] 修改 `templates` 表
  - 添加 `language` 字段（VARCHAR(10)，默认 'zh'）
  - 添加 `section_labels` 字段（JSONB，存储多语言标签）

- [ ] 修改 `resumes` 表
  - 添加 `translated_from_id` 字段（INTEGER，关联原简历ID）
  - 添加 `language` 字段（VARCHAR(10)，'zh' 或 'en'）

- [ ] 数据库迁移脚本
  - SQL迁移文件添加字段

#### 2.2 后端 - 翻译服务
- [ ] 在 `AiController.java` 添加接口
  - `POST /api/ai/translate-resume/{id}`
  - 获取简历数据
  - 调用翻译服务

- [ ] 创建 `ResumeTranslator.java` 翻译服务
  - 翻译内容字段（保持姓名为拼音）
  - 技术术语不翻译（Vue、React等）
  - 英文简历标题生成（原标题 + " - English"）

- [ ] 模板国际化配置
  - 扩展 `Template` 实体类
  - 支持 `sectionLabels` 多语言配置
  - 英文模板默认标签（Work Experience, Education等）

#### 2.3 前端 - 翻译功能
- [ ] 在简历列表页添加翻译入口
  - 每个简历卡片的翻译按钮
  - 翻译确认弹窗

- [ ] 在编辑器页面添加翻译功能
  - 顶部工具栏"翻译为英文"按钮
  - 翻译进度提示

- [ ] 创建 `TranslationDialog.vue`
  - 翻译配置选项
  - 翻译后打开新简历

#### 2.4 ResumeRenderer 组件国际化
- [ ] 根据 `template.language` 显示对应语言标签
  - 中文：工作经历、教育经历
  - 英文：Work Experience, Education

- [ ] 英文模板样式调整
  - 英文默认字体设置
  - 行高/间距微调

### 接口定义
```
POST /api/ai/translate-resume/{id}
Response:
{
  "success": true,
  "data": {
    "id": 123,
    "title": "产品经理简历 - English",
    "language": "en",
    "translatedFromId": 100,
    "content": {
      "name": "Zhang San",
      "email": "zhangsan@example.com",
      "phone": "+86 13800138000",
      "summary": "Experienced product manager...",
      ...
    }
  }
}
```

### 数据结构示例
```json
// templates.section_labels
{
  "zh": {
    "header": "个人信息",
    "summary": "个人简介",
    "experience": "工作经历",
    "education": "教育经历",
    "projects": "项目经历",
    "skills": "专业技能",
    "personalSummary": "个人总结",
    "honors": "荣誉奖项",
    "works": "个人作品"
  },
  "en": {
    "header": "Contact",
    "summary": "Summary",
    "experience": "Work Experience",
    "education": "Education",
    "projects": "Projects",
    "skills": "Skills",
    "personalSummary": "Summary",
    "honors": "Honors",
    "works": "Portfolio"
  }
}
```

---

## 模块3: AI简历优化（对话式）

### 需求描述
基于对话的AI简历优化助手，分阶段引导用户优化简历，提供可采纳的建议。

### 功能列表

#### 3.1 数据库
- [ ] 创建 `ai_conversations` 表
  ```sql
  CREATE TABLE ai_conversations (
    id SERIAL PRIMARY KEY,
    resume_id INTEGER REFERENCES resumes(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    messages JSONB,
    stage VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
  );

  CREATE INDEX idx_conversations_resume ON ai_conversations(resume_id);
  CREATE INDEX idx_conversations_user ON ai_conversations(user_id);
  ```

#### 3.2 后端 - 对话服务
- [ ] 创建 `OptimizerChatService.java`
  - 对话状态管理（evaluation → direction → content → suggestion → confirmation）
  - 上下文维护
  - 消息类型处理

- [ ] 在 `AiController.java` 添加接口
  - `POST /api/ai/optimize/chat` - 发送消息
  - `GET /api/ai/optimize/conversation/{id}` - 获取对话历史
  - `POST /api/ai/optimize/apply-suggestion` - 采纳建议

- [ ] AI提示词设计
  - 总体评价生成
  - 优化方向建议
  - 具体内容改写建议
  - 对比视图生成（原文vs优化）

#### 3.3 前端 - 聊天界面
- [ ] 创建 `AiOptimizeChat.vue` 组件
  - 聊天消息列表
  - 输入框（后期阶段可手动输入）
  - 选项卡片组件（Direction选择）
  - 内容选择器（Experience/Projects等）
  - 建议采纳组件（对比视图）
  - 一键采纳/手动编辑按钮

- [ ] 消息类型处理
  - `system`: AI评价/建议
  - `choice`: 可点击的选项卡片
  - `content-select`: 内容选择器
  - `suggestion`: 优化建议+对比
  - `user`: 用户操作记录

- [ ] 路由集成
  - 在编辑器右侧添加"AI优化"浮窗
  - 或单独页面 `/resumes/{id}/optimize`

#### 3.4 对话状态机
```
INITIAL ──> EVALUATION ──> DIRECTION ──> CONTENT_SELECT ──> SUGGESTION ──> CONFIRMATION
                                                    │              │              │
                                                    └──────────────┴──────────────┘
                                                       (可循环重新选择)
```

### 接口定义
```
POST /api/ai/optimize/chat
Request:
{
  "conversationId": 123,
  "message": "我想优化工作经历"
}
Response:
{
  "success": true,
  "data": {
    "conversationId": 123,
    "stage": "content_select",
    "messages": [
      {
        "role": "assistant",
        "type": "content_select",
        "content": "请选择要优化哪份工作经历：",
        "options": [
          {"id": "exp_0", "label": "腾讯 - 产品经理", "period": "2020-2023"},
          {"id": "exp_1", "label": "阿里巴巴 - 高级产品经理", "period": "2023-至今"}
        ]
      }
    ]
  }
}

POST /api/ai/optimize/apply-suggestion
Request:
{
  "conversationId": 123,
  "sectionType": "experience",
  "itemIndex": 0,
  "field": "description",
  "optimizedText": "优化后的描述..."
}
Response:
{
  "success": true,
  "data": {
    "resumeId": 100,
    "updatedContent": {...}
  }
}
```

### 消息数据结构
```json
// 评价阶段
{
  "role": "assistant",
  "type": "system",
  "content": "简历整体结构完整，但工作经历描述较简略。建议：..."
}

// 方向选择阶段
{
  "role": "assistant",
  "type": "choice",
  "content": "请选择要优化的方向：",
  "options": [
    {"id": "experience", "label": "工作经历", "icon": "briefcase"},
    {"id": "projects", "label": "项目经历", "icon": "code"},
    {"id": "skills", "label": "技能描述", "icon": "star"},
    {"id": "structure", "label": "整体结构", "icon": "layout"}
  ]
}

// 建议阶段
{
  "role": "assistant",
  "type": "suggestion",
  "sectionType": "experience",
  "itemIndex": 0,
  "field": "description",
  "original": "负责产品规划和设计",
  "optimized": "主导公司核心产品从0到1的规划与设计，通过用户调研和数据分析...",
  "reason": "增加了量化的成果描述和更具体的职责范围",
  "content": "建议修改如下..."
}
```

---

## 通用技术要求

### AI服务提供者配置
- [ ] 设计可插拔的AI Provider接口
  - `AiProvider` 抽象类
  - `OpenAiProvider` 实现
  - `QwenProvider` 实现（阿里通义千问）
  - `ErnieProvider` 实现（百度文心一言）

- [ ] 配置文件扩展（application.yml）
  ```yaml
  ai:
    provider: openai  # 或 qwen, ernie
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o
      base-url: https://api.openai.com/v1
    qwen:
      api-key: ${QWEN_API_KEY}
      model: qwen-plus
  ```

### 错误处理
- [ ] AI调用失败重试机制
- [ ] 超时处理（30秒）
- [ ] 降级策略（解析失败时提供手动导入）

### 成本控制
- [ ] API调用次数限制
- [ ] Token使用监控
- [ ] 用户配额管理（可选）

---

## 实现顺序建议

### Phase 1: 基础设施
1. AI Provider接口设计与实现
2. 数据库迁移
3. 配置文件扩展

### Phase 2: 模块1 - 简历导入
4. 后端解析服务
5. 前端导入界面
6. 集成测试

### Phase 3: 模块2 - 简历翻译
7. 数据库变更
8. 后端翻译服务
9. 前端翻译功能
10. 模板国际化
11. ResumeRenderer国际化

### Phase 4: 模块3 - AI优化
12. 数据库表创建
13. 后端对话服务
14. 前端聊天界面
15. 状态机实现
16. 端到端测试

---

## 验收标准

### 模块1
- [ ] 可上传PDF/Word/Text文件并成功解析
- [ ] 解析结果正确映射到Resume数据结构
- [ ] 用户可预览和修改后保存
- [ ] 支持中文简历解析准确率 > 80%

### 模块2
- [ ] 可将中文简历翻译为英文
- [ ] 翻译后简历与原简历有关联关系
- [ ] 英文简历使用英文标签显示
- [ ] 技术术语不翻译（Vue, React等）

### 模块3
- [ ] 对话流程完整（评价→方向→内容→建议→确认）
- [ ] AI建议可一键采纳
- [ ] 采纳后简历内容实时更新
- [ ] 对话历史可保存和恢复

---

## 备注
- 本Sprint暂不实现具体AI调用逻辑，仅设计接口和数据结构
- AI Provider的具体实现将在开发时根据实际选择的云服务商补充
- 前端UI细节可在开发时根据实际需求调整
