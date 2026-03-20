# 简历解析功能实现说明

## 功能概述
实现了基于Qwen2.5-72B的简历解析和导入功能，支持PDF、Word、Text格式的简历文件。

## 后端实现

### 1. 依赖配置
在 `pom.xml` 中添加了以下依赖：
- Spring AI OpenAI Starter (1.0.0-M4)
- Apache PDFBox (PDF解析)
- Apache POI (Word解析)

### 2. 配置文件
在 `application.yml` 中添加了AI配置：
```yaml
spring:
  ai:
    openai:
      api-key: ${SILICONFLOW_API_KEY:your-api-key}
      base-url: https://api.siliconflow.cn/v1
      chat:
        options:
          model: Qwen/Qwen2.5-72B-Instruct
          temperature: 0.3
          max-tokens: 4000
```

### 3. 核心服务类
- `FileParserUtil.java`: 文件内容提取工具
  - `extractPdfText()`: PDF文本提取
  - `extractDocText()`: Word .doc文本提取
  - `extractDocxText()`: Word .docx文本提取

- `ResumeParserServiceImpl.java`: AI解析服务
  - `parse()`: 调用Qwen2.5-72B解析简历
  - 自动清理AI响应中的JSON标记
  - 详细的提示词设计

- `AiController.java`: AI功能控制器
  - `POST /api/ai/parse-resume`: 解析简历文件
  - 文件验证（大小、格式）
  - 错误处理

- `ResumeController.java`: 新增接口
  - `POST /api/resumes/from-parsed`: 从解析结果创建简历

### 4. 数据传输对象
- `ParseResumeResponse.java`: 简历解析响应
  - 包含所有简历字段的结构
  - ExperienceItem, EducationItem, ProjectItem等

## 前端实现

### 1. API封装
- `api/ai.js`: AI相关API
  - `parseResume()`: 解析简历文件
  - `createResumeFromParsed()`: 从解析结果创建简历

### 2. 组件
- `ResumeImport.vue`: 简历导入对话框组件
  - 拖拽上传
  - 文件选择上传
  - 解析结果预览
  - 可编辑的解析结果
  - 确认导入

### 3. 页面集成
- `pages/Resume/List.vue`: 简历列表页
  - 添加"导入简历"按钮
  - 集成ResumeImport组件
  - 导入成功后跳转到编辑器

## 使用流程

### 1. 配置API密钥
在环境变量中设置硅基流动API密钥：
```bash
# Windows (PowerShell)
$env:SILICONFLOW_API_KEY="your-api-key-here"

# Linux/Mac
export SILICONFLOW_API_KEY="your-api-key-here"
```

或在 `application.yml` 中直接配置（不推荐生产环境）。

### 2. 启动后端
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```

### 4. 使用导入功能
1. 打开简历列表页
2. 点击"导入简历"按钮
3. 拖拽或选择PDF/Word/Text文件
4. 等待AI解析（约2-5秒）
5. 预览并编辑解析结果
6. 点击"确认导入"保存为新简历
7. 自动跳转到简历编辑器

## API接口说明

### POST /api/ai/parse-resume
解析简历文件

**请求**:
- Content-Type: multipart/form-data
- file: 上传的文件（最大10MB）
- 支持格式: .pdf, .doc, .docx, .txt

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "title": "从导入的简历",
    "content": {
      "name": "张三",
      "email": "zhangsan@example.com",
      "phone": "13800138000",
      "summary": "5年产品经理经验...",
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

### POST /api/resumes/from-parsed
从解析结果创建简历

**请求**:
```json
{
  "title": "产品经理简历",
  "content": {
    // 与解析接口返回的content结构相同
  }
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 123,
    "title": "产品经理简历",
    "status": "draft",
    "createdAt": "2025-02-11T10:30:00"
  }
}
```

## 特性

### 1. 支持的文件格式
- ✅ PDF (.pdf)
- ✅ Word (.doc, .docx)
- ✅ Text (.txt)

### 2. 文件限制
- 最大文件大小: 10MB
- AI解析内容长度: 约10,000字符

### 3. AI解析能力
- ✅ 精确提取基本信息（姓名、邮箱、电话）
- ✅ 识别工作经历（公司、职位、时间、描述）
- ✅ 识别教育经历（学校、专业、学位、时间）
- ✅ 识别项目经历（名称、时间、描述、技术栈）
- ✅ 识别专业技能（技能名称、熟练度）
- ✅ 识别个人总结、荣誉奖项、个人作品

### 4. 用户体验
- ✅ 拖拽上传
- ✅ 文件选择上传
- ✅ 实时进度显示
- ✅ 解析结果可编辑
- ✅ 解析失败友好提示
- ✅ 导入成功自动跳转

## 错误处理

### 文件上传错误
- 400: 文件为空
- 400: 不支持的文件格式
- 400: 文件大小超过10MB
- 500: 文件读取失败

### AI解析错误
- 500: 简历解析失败
  - AI返回格式错误
  - JSON解析失败
  - 网络超时

## 成本估算

### 硅基流动Qwen2.5-72B价格
- 输入: 约2000 tokens
- 输出: 约3000 tokens
- 单次成本: 约 ¥0.15

### 月成本估算（中等使用量）
- 解析: 20次 × ¥0.15 = ¥3/月

## 后续优化建议

### 1. 性能优化
- [ ] 添加文件解析缓存
- [ ] AI调用异步处理
- [ ] 批量上传支持

### 2. 功能增强
- [ ] 支持更多文件格式（RTF, ODT等）
- [ ] 解析准确度提升
- [ ] 解析结果校验和提示
- [ ] 支持从URL导入（如在线简历）

### 3. 用户体验
- [ ] 解析进度条
- [ ] 解析结果高亮显示（AI置信度）
- [ ] 一键修正常见错误

## 注意事项

1. **API密钥管理**: 生产环境请使用环境变量，不要硬编码
2. **文件大小限制**: 可根据实际情况调整，注意服务器资源
3. **AI准确度**: 复杂排版或扫描件PDF可能解析效果不佳
4. **数据验证**: 导入后建议用户检查，AI可能存在识别错误
5. **并发限制**: 硅基流动RPM=1000，注意并发请求控制

## 相关文件清单

### 后端
- `pom.xml`: Maven依赖配置
- `application.yml`: Spring Boot配置
- `src/main/java/com/resume/util/FileParserUtil.java`: 文件解析工具
- `src/main/java/com/resume/dto/response/ParseResumeResponse.java`: 解析响应DTO
- `src/main/java/com/resume/service/ResumeParserService.java`: 解析服务接口
- `src/main/java/com/resume/service/impl/ResumeParserServiceImpl.java`: 解析服务实现
- `src/main/java/com/resume/controller/AiController.java`: AI控制器
- `src/main/java/com/resume/controller/ResumeController.java`: 简历控制器（新增接口）
- `src/main/java/com/resume/service/ResumeService.java`: 简历服务接口
- `src/main/java/com/resume/service/impl/ResumeServiceImpl.java`: 简历服务实现

### 前端
- `frontend/src/api/ai.js`: AI API封装
- `frontend/src/components/ResumeImport.vue`: 导入组件
- `frontend/src/pages/Resume/List.vue`: 简历列表页（已修改）
