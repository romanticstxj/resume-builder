---
inclusion: fileMatch
fileMatchPattern: "**/application*.yml,**/application*.yaml"
---

# API Key 安全规范

## 禁止提交的内容
- `spring.ai.openai.api-key` 的真实值
- 数据库密码（`spring.datasource.password`）

## 正确做法
使用环境变量覆盖：
```bash
# Windows
set SPRING_AI_OPENAI_API_KEY=your-key
set SPRING_DATASOURCE_PASSWORD=your-password
```

或在本地创建 `application-local.yml`（已加入 .gitignore）并激活 `local` profile。

## 检查点
每次修改 `application.yml` 前确认没有硬编码的真实密钥。
