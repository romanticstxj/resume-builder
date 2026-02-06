@echo off
setlocal enabledelayedexpansion

echo ========================================
echo 设置 JAVA_HOME 并启动 Maven
echo ========================================

REM 设置 JDK 17 路径
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "JDK_HOME=C:\Program Files\Java\jdk-17"

REM 验证 Java 版本
echo.
echo [检查] Java 版本:
"%JAVA_HOME%\bin\java.exe" -version
if %errorlevel% neq 0 (
    echo [错误] 无法找到 JDK 17!
    echo 请确认路径: C:\Program Files\Java\jdk-17
    pause
    exit /b 1
)

echo.
echo [信息] JAVA_HOME: %JAVA_HOME%
echo [信息] JDK_HOME: %JDK_HOME%

REM 设置 PATH
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM 显示 Maven 信息
echo.
echo [检查] Maven 版本:
"D:\apache-maven-3.9.12\bin\mvn.cmd" -version
if %errorlevel% neq 0 (
    echo [错误] Maven 执行失败!
    pause
    exit /b 1
)

echo.
echo ========================================
echo 开始编译项目
echo ========================================
echo.

cd /d "%~dp0"

REM 执行 Maven 命令
"D:\apache-maven-3.9.12\bin\mvn.cmd" clean install -DskipTests

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 编译成功!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo 编译失败!
    echo ========================================
)

pause
