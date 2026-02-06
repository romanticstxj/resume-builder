# JDK 17 安装和配置指南

## 推荐下载选项

### 选项 1: Oracle JDK 17 (推荐)

**优点**: 官方版本,稳定可靠
**下载地址**: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

**下载步骤**:
1. 访问上方链接
2. 找到 "Java SE Development Kit 17.0.9" 或最新版本
3. 接受 Oracle 许可协议
4. 下载 `jdk-17.0.9_windows-x64_bin.exe` (约 160MB)

**安装步骤**:
1. 双击下载的 `.exe` 文件
2. 点击"下一步"
3. 安装路径: `C:\Program Files\Java\jdk-17`
4. 点击"下一步"完成安装

### 选项 2: Eclipse Temurin 17 (免费)

**优点**: 完全免费,开源,基于 OpenJDK
**下载地址**: https://adoptium.net/temurin/releases/?version=17

**下载步骤**:
1. 访问上方链接
2. Version: 选择 `17 (LTS)`
3. Operating System: 选择 `Windows`
4. Architecture: 选择 `x64`
5. Package Type: 选择 `MSI Installer` (`.msi` 文件)
6. 下载安装包

**安装步骤**:
1. 双击下载的 `.msi` 文件
2. 安装路径: `C:\Program Files\Eclipse Adoptium\jdk-17` (默认)
3. 点击"Install"完成安装

### 选项 3: Amazon Corretto 17 (免费)

**优点**: AWS 维护,免费,生产级质量
**下载地址**: https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html

**下载步骤**:
1. 访问上方链接
2. 找到 Windows x64 版本
3. 下载 `.msi` 安装包

## 配置环境变量

### 方法 1: 使用自动配置脚本(推荐)

1. 右键点击 `setup-java17.bat`
2. 选择"以管理员身份运行"
3. 等待脚本完成
4. 关闭当前窗口,重新打开终端

### 方法 2: 手动配置

1. **打开环境变量设置**
   - 右键「此电脑」→「属性」
   - 点击「高级系统设置」
   - 点击「环境变量」

2. **设置 JAVA_HOME**
   - 在「系统变量」区域点击「新建」
   - 变量名: `JAVA_HOME`
   - 变量值: `C:\Program Files\Java\jdk-17` (或你的安装路径)
   - 点击「确定」

3. **添加到 PATH**
   - 在「系统变量」中找到 `Path` 变量
   - 点击「编辑」
   - 点击「新建」
   - 输入: `%JAVA_HOME%\bin`
   - 点击「确定」保存

4. **验证配置**
   - 关闭所有终端窗口
   - 打开新的 PowerShell 或 CMD
   - 输入: `java -version`
   - 应该显示: `java version "17.0.9"`

## 验证安装

### 完整验证命令

在新的 PowerShell 中执行:

```powershell
# 查看 Java 版本
java -version

# 查看 Java 编译器版本
javac -version

# 查看 JAVA_HOME 环境变量
echo $env:JAVA_HOME
```

**预期输出**:
```
java version "17.0.9" 2023-10-17 LTS
Java(TM) SE Runtime Environment (build 17.0.9+11-LTS-201)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.9+11-LTS-201, mixed mode, sharing)
```

## 配置 Maven 使用 JDK 17

Maven 会自动使用 JAVA_HOME 指向的 JDK,无需额外配置。

如果要手动指定,可以编辑 `D:\apache-maven-3.9.12\conf\settings.xml`:

```xml
<settings>
    ...
    <profiles>
        <profile>
            <id>jdk-17</id>
            <activation>
                <activeByDefault>true</activeByDefault>
                <jdk>17</jdk>
            </activation>
            <properties>
                <maven.compiler.source>17</maven.compiler.source>
                <maven.compiler.target>17</maven.compiler.target>
                <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
            </properties>
        </profile>
    </profiles>
    ...
</settings>
```

## 更新 Maven 配置(可选)

确保 `pom.xml` 使用正确的 Java 版本:

```xml
<properties>
    <java.version>17</java.version>
</properties>
```

## 测试项目编译

JDK 17 安装配置完成后,测试项目是否能正常编译:

```powershell
cd c:\Users\lenovo\CodeBuddy\resume-builder
mvn clean compile
```

如果编译成功,说明 JDK 17 配置正确。

## 常见问题

### Q1: 同时安装了多个 JDK 版本怎么办?

A: 系统会使用 JAVA_HOME 指向的版本。可以在不同的终端临时切换:

```powershell
# 临时使用 JDK 8
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_202"

# 临时使用 JDK 17
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

### Q2: 安装后 java -version 还是显示旧版本?

A:
1. 检查 Path 中是否有旧版本的 Java 路径,删除它
2. 确认 JAVA_HOME 设置正确
3. 重启电脑确保环境变量生效
4. 在新的终端中验证

### Q3: Maven 编译时提示不支持的类文件?

A: 清理 Maven 缓存后重新编译:

```powershell
mvn clean install -U
```

## 卸载旧 JDK(可选)

如果确认 JDK 17 正常工作,可以卸载 JDK 8:

1. 打开「控制面板」→「程序和功能」
2. 找到 "Java(TM) SE Development Kit 8"
3. 右键卸载

**注意**: 如果有其他项目依赖 JDK 8,请不要卸载。

---

**下一步**: JDK 17 安装完成后,就可以启动后端服务了!

参考文档: `BACKEND-START.md`
