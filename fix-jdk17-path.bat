@echo off
echo ========================================
echo 修复 JDK 17 PATH 环境变量
echo ========================================
echo.

REM 检查 JDK 17 是否存在
if not exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    echo [错误] 未找到 JDK 17！
    echo 请确保 JDK 17 安装在: C:\Program Files\Java\jdk-17
    echo.
    pause
    exit /b 1
)

echo [步骤 1] 设置 JAVA_HOME 环境变量...
setx JAVA_HOME "C:\Program Files\Java\jdk-17" /M
if %errorlevel% equ 0 (
    echo [成功] JAVA_HOME 已设置为: C:\Program Files\Java\jdk-17
) else (
    echo [失败] 设置 JAVA_HOME 失败，请以管理员身份运行此脚本
    pause
    exit /b 1
)
echo.

echo [步骤 2] 修复 PATH 环境变量...
REM 读取当前系统 PATH
for /f "tokens=2*" %%A in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path') do set CURRENT_PATH=%%B

echo 当前 PATH 长度: %CURRENT_PATH:~0,100%...
echo.

REM 检查是否已包含 JDK 17
echo %CURRENT_PATH% | findstr /i "jdk-17\\bin" >nul
if %errorlevel% equ 0 (
    echo [提示] PATH 中已存在 JDK 17，正在调整顺序...
    REM 从 PATH 中移除旧的 JDK 17 路径（如果有）
    set CURRENT_PATH=%CURRENT_PATH:C:\Program Files\Java\jdk-17\bin;=%
    set CURRENT_PATH=%CURRENT_PATH:;C:\Program Files\Java\jdk-17\bin=%
)

REM 将 JDK 17 bin 目录添加到 PATH 最前面
set NEW_PATH=C:\Program Files\Java\jdk-17\bin;%CURRENT_PATH%

REM 写入注册表
reg add "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path /t REG_EXPAND_SZ /d "%NEW_PATH%" /f
if %errorlevel% equ 0 (
    echo [成功] PATH 已更新，JDK 17 已添加到最前面
) else (
    echo [失败] 更新 PATH 失败，请以管理员身份运行此脚本
    pause
    exit /b 1
)
echo.

echo ========================================
echo 配置完成！
echo ========================================
echo.
echo 重要提示:
echo 1. 请关闭所有终端窗口
echo 2. 重新打开终端
echo 3. 执行: java -version
echo.
echo 应该显示: java version "17.0.x"
echo.
pause
