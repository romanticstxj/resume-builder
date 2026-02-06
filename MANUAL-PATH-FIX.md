# 手动修复 PATH 环境变量指南

## 问题说明

`setx PATH` 命令可能因为以下原因失败:
1. 没有管理员权限
2. PATH 太长,导致截断
3. 特殊字符导致解析错误

## 解决方案

### 方案 1: 手动添加(最可靠)

1. **打开环境变量设置**
   - 右键点击「此电脑」
   - 选择「属性」
   - 点击「高级系统设置」
   - 点击「环境变量」

2. **找到并编辑 Path**
   - 在「系统变量」区域找到 `Path` 变量
   - 选中后点击「编辑」

3. **添加 JDK 17 到 PATH**
   - 点击「新建」按钮
   - 输入: `C:\Program Files\Java\jdk-17\bin`
   - 注意: 请根据你实际的 JDK 17 安装路径修改

4. **保存设置**
   - 点击「确定」保存
   - 点击「确定」关闭所有对话框

5. **验证**
   - 关闭所有终端窗口
   - 打开新的 PowerShell 或 CMD
   - 输入: `java -version`
   - 应该显示: `java version "17.0.x"`

### 方案 2: 使用 PowerShell 命令(需要管理员)

打开 **管理员 PowerShell**,执行:

```powershell
# 设置 JAVA_HOME (如果还没设置)
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")

# 获取当前 PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")

# 添加 JDK 17 到 PATH
$newPath = "C:\Program Files\Java\jdk-17\bin;$currentPath"
[Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")

# 验证
java -version
```

### 方案 3: 使用修复脚本

#### 选项 A: 运行批处理脚本

1. 右键点击 `fix-path.bat`
2. 选择「以管理员身份运行」
3. 按照提示操作

#### 选项 B: 运行 PowerShell 脚本

1. 右键点击 `fix-path.ps1`
2. 选择「使用 PowerShell 运行」(或右键选择「以管理员身份运行 PowerShell」)
3. 如果提示执行策略错误,先执行:
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```
4. 然后再次运行脚本

## 检查当前的配置

### 检查 JAVA_HOME

在 PowerShell 中执行:
```powershell
[Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
```

应该返回: `C:\Program Files\Java\jdk-17`

如果不是,需要先设置 JAVA_HOME:
```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")
```

### 检查 PATH

在 PowerShell 中执行:
```powershell
$path = [Environment]::GetEnvironmentVariable("Path", "Machine")
$path -split ";" | Where-Object { $_ -like "*java*" }
```

应该看到包含 `jdk-17\bin` 的路径。

## 常见问题

### Q1: 手动设置后 java -version 还是显示旧版本?

**原因**: 可能 PATH 中有多个 Java 路径,旧的排在前面

**解决**:
1. 重新编辑 PATH
2. 将 JDK 17 的路径移到最前面
3. 删除旧版本 Java 的路径
4. 保存后重新打开终端

### Q2: 找不到 JDK 17 的安装路径?

**可能的位置**:
- `C:\Program Files\Java\jdk-17`
- `C:\Program Files\Eclipse Adoptium\jdk-17`
- `C:\Program Files\Amazon Corretto\jdk17.x.x`

**查找方法**:
- 打开文件资源管理器
- 导航到 `C:\Program Files\Java\`
- 查看有哪些版本

### Q3: 想同时保留 JDK 8 和 JDK 17?

**可以!** 设置多个 JAVA_HOME,在 PATH 中只保留需要的版本:

```powershell
# 设置 JDK 8
[Environment]::SetEnvironmentVariable("JAVA_HOME_8", "C:\Program Files\Java\jdk1.8.0_202", "Machine")

# 设置 JDK 17
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")

# PATH 中只放 JDK 17
# JDK 8 需要时可以临时切换:
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_202"
```

### Q4: 环境变量窗口中的 PATH 太长,看不清?

**解决**:
1. 点击「编辑文本」
2. 在文本编辑器中编辑
3. 每个路径用 `;` 分隔
4. 编辑完成后保存

## 验证配置成功

在新的 PowerShell 中执行以下命令:

```powershell
# 1. 检查 Java 版本
java -version

# 2. 检查 Java 可执行文件路径
Get-Command java | Select-Object -ExpandProperty Source

# 3. 检查环境变量
echo $env:JAVA_HOME
```

**预期输出**:
```
java version "17.0.x" ...

C:\Program Files\Java\jdk-17\bin\java.exe

C:\Program Files\Java\jdk-17
```

## 设置完成后

环境变量配置完成后,就可以启动后端了:

```powershell
cd c:\Users\lenovo\CodeBuddy\resume-builder
mvn spring-boot:run
```

或者使用启动脚本:
```powershell
.\start-backend.bat
```

---

**推荐**: 使用「方案 1 手动添加」,这是最可靠的方法。
