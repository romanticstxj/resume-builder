@echo off
echo ========================================
echo Java 环境检查工具
echo ========================================
echo.

echo [1] 检查 JAVA_HOME...
if defined JAVA_HOME (
    echo JAVA_HOME = %JAVA_HOME%
) else (
    echo JAVA_HOME 未设置
)
echo.

echo [2] 检查当前 Java 版本...
java -version 2>&1 | findstr /i "version"
echo.

echo [3] 检查 Java 可执行文件位置...
where java
echo.

echo [4] 检查 PATH 中的 Java 目录...
echo %PATH% | findstr /i "java"
echo.

echo [5] 检查 JDK 17 是否存在...
if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    echo [√] JDK 17 已安装在: C:\Program Files\Java\jdk-17
    echo.
    echo 尝试直接使用 JDK 17:
    "C:\Program Files\Java\jdk-17\bin\java.exe" -version
) else (
    echo [×] 未找到 JDK 17 在: C:\Program Files\Java\jdk-17
    echo 请确认 JDK 17 的实际安装路径
)
echo.

echo ========================================
pause
