# AI 简历制作工具 - 一期开发完成

## 项目简介

AI简历制作工具是一个智能简历管理平台,支持简历创建、多模板管理、实时预览和PDF/Word导出。

**技术栈:**
- 后端: Spring Boot 3.x + MySQL + MyBatis Plus + JWT + iText
- 前端: Vue 3 + TDesign + Pinia + Vue Router + Axios

## 一期功能清单

### ✅ 已完成功能

1. **用户认证模块**
   - 用户注册/登录
   - JWT Token 认证
   - 密码加密存储
   - 登录状态管理

2. **简历管理模块**
   - 简历创建/编辑/删除
   - 简历列表查看
   - 简历复制功能
   - 简历实时预览
   - 个人信息、工作经历、教育经历管理

3. **模板管理模块**
   - 官方模板库
   - 模板分类筛选
   - 模板预览
   - 模板使用统计

4. **导出功能**
   - PDF 格式导出
   - Word 格式导出(基础实现)

5. **前端界面**
   - 登录/注册页面
   - 主布局(侧边栏导航)
   - 简历列表页面
   - 简历编辑器(左侧编辑,右侧预览)
   - 模板市场页面
   - AI 生成页面(框架,待完善)

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

1. **创建数据库**
```sql
CREATE DATABASE resume_builder CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. **配置数据库连接**
编辑 `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resume_builder?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的密码
```

3. **初始化数据库**
数据库脚本位于: `src/main/resources/db/migration/V1__init.sql`
- 可手动执行该脚本,或使用 Flyway 自动迁移

4. **启动后端服务**
```bash
cd resume-builder
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 前端启动

1. **安装依赖**
```bash
cd frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 访问应用

打开浏览器访问: http://localhost:3000

默认管理员账号(需自行注册):
- 无默认账号,请先注册

## API 接口文档

### 认证接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/auth/register | POST | 用户注册 |
| /api/v1/auth/login | POST | 用户登录 |

### 简历接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/resumes | POST | 创建简历 |
| /api/v1/resumes | GET | 获取简历列表 |
| /api/v1/resumes/{id} | GET | 获取简历详情 |
| /api/v1/resumes/{id} | PUT | 更新简历 |
| /api/v1/resumes/{id} | DELETE | 删除简历 |
| /api/v1/resumes/{id}/copy | POST | 复制简历 |
| /api/v1/resumes/{id}/export/pdf | GET | 导出 PDF |
| /api/v1/resumes/{id}/export/word | GET | 导出 Word |

### 模板接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/v1/templates/public | GET | 获取公开模板列表 |
| /api/v1/templates/public/{id} | GET | 获取模板详情 |

## 项目结构

### 后端结构
```
resume-builder/
├── src/main/java/com/resume/
│   ├── ResumeApplication.java       # 启动类
│   ├── config/                      # 配置类
│   │   ├── SecurityConfig.java      # 安全配置
│   │   ├── MyBatisPlusConfig.java  # MyBatis配置
│   │   └── MetaObjectHandler.java  # 字段自动填充
│   ├── controller/                  # 控制器
│   │   ├── AuthController.java
│   │   ├── ResumeController.java
│   │   ├── TemplateController.java
│   │   └── ExportController.java
│   ├── service/                    # 服务层
│   │   ├── impl/
│   │   ├── AuthService.java
│   │   ├── ResumeService.java
│   │   ├── TemplateService.java
│   │   └── ExportService.java
│   ├── repository/                 # 数据访问层
│   ├── entity/                     # 实体类
│   ├── dto/                        # 数据传输对象
│   │   ├── request/
│   │   ├── response/
│   │   └── common/
│   └── security/                   # 安全相关
│       ├── JwtUtil.java
│       ├── JwtAuthenticationFilter.java
│       ├── CustomUserDetails.java
│       └── UserDetailsServiceImpl.java
└── src/main/resources/
    ├── application.yml
    └── db/migration/V1__init.sql
```

### 前端结构
```
frontend/
├── src/
│   ├── main.js                    # 入口文件
│   ├── App.vue                    # 根组件
│   ├── router/                     # 路由配置
│   │   └── index.js
│   ├── stores/                    # Pinia状态管理
│   │   └── user.js
│   ├── api/                       # API接口
│   │   ├── index.js
│   │   ├── auth.js
│   │   ├── resume.js
│   │   └── template.js
│   ├── pages/                     # 页面组件
│   │   ├── Login/
│   │   ├── Register/
│   │   ├── Resume/
│   │   │   ├── List.vue
│   │   │   └── Editor.vue
│   │   ├── Template/
│   │   │   └── Market.vue
│   │   └── AI/
│   │       └── Generate.vue
│   ├── layouts/                   # 布局组件
│   │   └── MainLayout.vue
│   └── styles/                    # 样式文件
│       └── index.css
├── package.json
├── vite.config.js
└── index.html
```

## 二期规划

### 待开发功能

1. **AI 智能模块**
   - AI 简历内容生成
   - AI 中英文翻译
   - AI 简历优化建议
   - AI 关键词匹配

2. **高级简历功能**
   - 简历版本控制
   - 版本历史查看
   - 版本回滚
   - 版本对比

3. **面试管理模块(核心)**
   - JD 管理
   - 简历与 JD 匹配
   - 面试记录管理
   - 面试反馈录入
   - 数据统计分析

4. **其他功能**
   - 富文本编辑器
   - 模板编辑器
   - 用户自定义模板
   - 简历分享功能

## 开发说明

### 添加新功能

1. **后端**: 在 `controller`、`service`、`repository` 中添加相应代码
2. **前端**: 在 `pages` 中创建新页面,在 `api` 中添加接口调用
3. **路由**: 在 `router/index.js` 中注册新路由

### 代码规范

- 后端遵循 Spring Boot 最佳实践
- 前端使用 Composition API 风格
- 组件命名使用 PascalCase
- API 接口统一使用 Result 包装

## 注意事项

1. JWT Secret 密钥在生产环境需要修改
2. 数据库密码需要配置正确
3. 前端代理配置需要与后端端口一致
4. 导出功能需要完整实现 Word 格式

## 技术支持

如有问题,请参考:
- Spring Boot 文档: https://spring.io/projects/spring-boot
- Vue 3 文档: https://vuejs.org/
- TDesign 文档: https://tdesign.tencent.com/vue-next/overview

---

**开发时间**: 2026-02-04  
**版本**: v1.0 (一期)
