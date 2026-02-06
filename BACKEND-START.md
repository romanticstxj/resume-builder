# Maven 环境配置和后端启动指南

## Maven 环境变量配置

### 方案 1: 手动配置环境变量(推荐永久生效)

1. **设置 MAVEN_HOME**
   - 右键「此电脑」→「属性」→「高级系统设置」→「环境变量」
   - 在「系统变量」中点击「新建」:
     - 变量名: `MAVEN_HOME`
     - 变量值: `D:\apache-maven-3.9.12`

2. **添加到 PATH**
   - 在「系统变量」中找到 `Path` 变量,点击「编辑」
   - 点击「新建」,添加: `%MAVEN_HOME%\bin`
   - 点击「确定」保存

3. **验证配置**
   - 打开新的 PowerShell 或 CMD
   - 输入: `mvn -version`
   - 如果显示 Maven 版本信息,说明配置成功

### 方案 2: 使用配置脚本(需要管理员权限)

双击运行 `setup-maven.bat` 文件,脚本会自动配置环境变量。

配置完成后需要关闭当前窗口并重新打开终端。

### 方案 3: 临时使用(不推荐)

每次启动时使用完整路径:
```powershell
D:\apache-maven-3.9.12\bin\mvn spring-boot:run
```

## 启动后端服务

### 方法 1: 使用启动脚本(推荐)

双击运行 `start-backend.bat` 文件,脚本会自动:
1. 检查 Maven 环境
2. 检查配置文件
3. 清理并编译项目
4. 启动 Spring Boot 应用

### 方法 2: 使用命令行

如果已配置环境变量:
```powershell
cd c:\Users\lenovo\CodeBuddy\resume-builder
mvn spring-boot:run
```

如果未配置环境变量:
```powershell
cd c:\Users\lenovo\CodeBuddy\resume-builder
D:\apache-maven-3.9.12\bin\mvn spring-boot:run
```

### 方法 3: 分步执行

```powershell
# 1. 进入项目目录
cd c:\Users\lenovo\CodeBuddy\resume-builder

# 2. 清理项目
mvn clean

# 3. 编译项目
mvn compile

# 4. 运行项目
mvn spring-boot:run
```

## 注意事项

### 1. 数据库配置

启动前需要确保 MySQL 已运行,并修改配置文件:

编辑 `src\main\resources\application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resume_builder?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的 MySQL 密码
```

### 2. 创建数据库

如果数据库不存在,需要先创建:
```sql
CREATE DATABASE resume_builder CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

数据库初始化脚本位置: `src\main\resources\db\migration\V1__init.sql`

### 3. Java 版本

当前系统使用的是 Java 1.8 (JDK 8),而项目配置要求 JDK 17。

**选项 A**: 升级到 JDK 17 (推荐)
- 下载 JDK 17: https://www.oracle.com/java/technologies/downloads/#java17
- 配置 JAVA_HOME 环境变量

**选项 B**: 修改 pom.xml 降级到 JDK 8
```xml
<properties>
    <java.version>1.8</java.version>
</properties>
```

### 4. 首次启动

首次启动时 Maven 会下载依赖,可能需要较长时间(5-10分钟),请耐心等待。

看到以下日志表示启动成功:
```
Started ResumeApplication in xx.xxx seconds
```

### 5. 访问应用

后端启动成功后,访问:
- 接口文档: http://localhost:8080/api
- 健康检查: http://localhost:8080/actuator/health (如果配置了)

## 常见问题

### Q1: 提示 "mvn 不是内部或外部命令"
A: Maven 环境变量未正确配置,参考上文手动配置环境变量。

### Q2: 提示 "JAVA_HOME is not set"
A: 需要配置 JAVA_HOME 环境变量指向 JDK 安装路径。

### Q3: 下载依赖很慢
A: 可以配置阿里云镜像,编辑 `D:\apache-maven-3.9.12\conf\settings.xml`:
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### Q4: 数据库连接失败
A: 检查:
1. MySQL 服务是否启动
2. 数据库是否已创建
3. 用户名密码是否正确
4. application.yml 配置是否正确

### Q5: 端口被占用
A: 如果 8080 端口被占用,修改 application.yml:
```yaml
server:
  port: 8081  # 改为其他端口
```

## 停止服务

在启动服务的终端按 `Ctrl + C` 即可停止服务。

---

**配置说明**
- Maven 安装路径: `D:\apache-maven-3.9.12`
- Java 版本: JDK 8 (建议升级到 JDK 17)
- 项目端口: 8080
- 数据库: MySQL 8.0
