@echo off
REM ========================================
REM JDK 17 环境变量自动配置脚本
REM ========================================

echo.
echo ========================================
echo   JDK 17 环境变量配置脚本
echo ========================================
echo.

REM 检查是否已安装 JDK 17
if exist "C:\Program Files\Java\jdk-17" (
    echo [检查] JDK 17 已安装在 C:\Program Files\Java\jdk-17
    set JAVA_HOME=C:\Program Files\Java\jdk-17
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17" (
    echo [检查] JDK 17 已安装在 C:\Program Files\Eclipse Adoptium\jdk-17
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
) else (
    echo [错误] 未找到 JDK 17 安装目录!
    echo.
    echo 请先安装 JDK 17:
    echo 1. Oracle JDK 17: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
    echo 2. Eclipse Temurin: https://adoptium.net/temurin/releases/?version=17
    echo.
    echo 安装后,请修改此脚本中的 JAVA_HOME 路径
    pause
    exit /b 1
)

echo.
echo 正在配置环境变量...

REM 设置 JAVA_HOME
echo 设置 JAVA_HOME = %JAVA_HOME%
setx JAVA_HOME "%JAVA_HOME%" /M

if %errorlevel% neq 0 (
    echo [错误] 设置 JAVA_HOME 失败,需要管理员权限!
    echo 请右键以管理员身份运行此脚本
    pause
    exit /b 1
)

REM 检查是否已经在 PATH 中
for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "CURRENT_PATH=%%b"

echo %CURRENT_PATH% | findstr /i "jdk-17" >nul
if %errorlevel% neq 0 (
    echo 将 JDK 17 添加到 PATH...
    setx PATH "%CURRENT_PATH%;%%JAVA_HOME%%\bin" /M
) else (
    echo JDK 17 已在 PATH 中
)

echo.
echo ========================================
echo   配置完成!
echo ========================================
echo.
echo 请关闭当前窗口并重新打开以使更改生效
echo.
echo 验证安装:
echo   java -version
echo.
pause
