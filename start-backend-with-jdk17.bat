@echo off
REM 设置 JAVA_HOME 指向 JDK 17
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM 显示 Java 版本验证
echo ========================================
echo 使用 Java 版本:
java -version
echo ========================================
echo.

REM 启动 Spring Boot 应用
echo 正在启动后端服务...
echo.
cd /d "%~dp0"
"D:\apache-maven-3.9.12\bin\mvn.cmd" spring-boot:run

pause
