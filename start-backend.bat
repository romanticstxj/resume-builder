@echo off
setlocal enabledelayedexpansion

echo ========================================
echo 启动 Resume Builder 后端服务
echo ========================================

REM 设置 JDK 17 路径
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "JDK_HOME=C:\Program Files\Java\jdk-17"

REM 设置 PATH
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM 验证 Java 版本
echo.
echo [检查] Java 版本:
java -version
if %errorlevel% neq 0 (
    echo [错误] 无法找到 JDK 17!
    echo 请确认路径: C:\Program Files\Java\jdk-17
    pause
    exit /b 1
)

echo.
echo ========================================
echo 启动 Spring Boot 应用
echo ========================================
echo.

cd /d "%~dp0"

REM 执行 Spring Boot
"D:\apache-maven-3.9.12\bin\mvn.cmd" spring-boot:run

pause
