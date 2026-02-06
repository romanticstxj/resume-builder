@echo off
REM ========================================
REM 修复 PATH 环境变量脚本
REM ========================================

echo.
echo ========================================
echo   修复 PATH 环境变量
echo ========================================
echo.

REM 获取当前的 JAVA_HOME
for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v JAVA_HOME 2^>nul') do set "JAVA_HOME=%%b"

if "%JAVA_HOME%"=="" (
    echo [错误] JAVA_HOME 环境变量未设置!
    echo 请先设置 JAVA_HOME 指向 JDK 17 的安装路径
    echo 例如: C:\Program Files\Java\jdk-17
    echo.
    pause
    exit /b 1
)

echo 当前 JAVA_HOME: %JAVA_HOME%

REM 检查 JDK 路径是否存在
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [错误] JAVA_HOME 指向的路径无效或不是 JDK 安装路径!
    echo.
    echo 路径: %JAVA_HOME%
    echo.
    pause
    exit /b 1
)

echo [检查] JDK 路径存在

REM 获取当前 PATH
for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "CURRENT_PATH=%%b"

echo.
echo 当前 PATH (前200字符):
echo %CURRENT_PATH:~0,200%
echo.

REM 检查是否已在 PATH 中
echo %CURRENT_PATH% | findstr /i "jdk-17" >nul
if %errorlevel% equ 0 (
    echo [检查] JDK 17 已在 PATH 中
    echo.
    echo 验证 Java 版本:
    "%JAVA_HOME%\bin\java.exe" -version
    echo.
    goto :end
)

echo [检查] JDK 17 不在 PATH 中,准备添加...
echo.

REM 显示新 PATH 的前部分
set "NEW_PATH=%JAVA_HOME%\bin;%CURRENT_PATH%"
echo 新 PATH (前200字符):
echo %NEW_PATH:~0,200%
echo.

echo 正在设置新的 PATH...
echo.

REM 使用 setx 设置 PATH (需要管理员权限)
setx PATH "%NEW_PATH%" /M

if %errorlevel% neq 0 (
    echo [错误] 设置 PATH 失败!
    echo 请以管理员身份运行此脚本
    echo.
    pause
    exit /b 1
)

echo [成功] PATH 已更新!
echo.
echo ========================================
echo   重要提示
echo ========================================
echo.
echo 1. 请关闭当前所有终端窗口
echo 2. 重新打开终端
echo 3. 验证: java -version
echo.
echo ========================================
echo.

:end
pause
