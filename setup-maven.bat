@echo off
echo 正在设置 Maven 环境变量...

REM 设置 MAVEN_HOME
setx MAVEN_HOME "D:\apache-maven-3.9.12" /M

REM 将 Maven 添加到 PATH (需要管理员权限)
for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "CURRENT_PATH=%%b"

REM 检查是否已经在 PATH 中
echo %CURRENT_PATH% | findstr /i "apache-maven" >nul
if %errorlevel% neq 0 (
    echo 添加 Maven 到 PATH...
    setx PATH "%CURRENT_PATH%;%%MAVEN_HOME%%\bin" /M
) else (
    echo Maven 已在 PATH 中
)

echo.
echo 环境变量设置完成!
echo 请关闭当前窗口并重新打开以使更改生效
echo.
pause
