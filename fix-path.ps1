# ========================================
# 修复 PATH 环境变量 (PowerShell)
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复 PATH 环境变量" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取 JAVA_HOME
$javaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")

if (-not $javaHome) {
    Write-Host "[错误] JAVA_HOME 环境变量未设置!" -ForegroundColor Red
    Write-Host "请先设置 JAVA_HOME 指向 JDK 17 的安装路径" -ForegroundColor Yellow
    Write-Host "例如: C:\Program Files\Java\jdk-17" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "当前 JAVA_HOME: $javaHome" -ForegroundColor Green

# 检查 JDK 路径
if (-not (Test-Path "$javaHome\bin\java.exe")) {
    Write-Host "[错误] JAVA_HOME 指向的路径无效!" -ForegroundColor Red
    Write-Host "路径: $javaHome" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "[检查] JDK 路径存在" -ForegroundColor Green

# 获取当前 PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")

Write-Host ""
Write-Host "当前 PATH (前 200 字符):" -ForegroundColor Yellow
Write-Host $currentPath.Substring(0, [Math]::Min(200, $currentPath.Length))
Write-Host ""

# 检查是否已在 PATH 中
if ($currentPath -like "*jdk-17*") {
    Write-Host "[检查] JDK 17 已在 PATH 中" -ForegroundColor Green
    Write-Host ""
    Write-Host "验证 Java 版本:" -ForegroundColor Cyan
    & "$javaHome\bin\java.exe" -version
    Write-Host ""
    Read-Host "按回车键退出"
    exit 0
}

Write-Host "[检查] JDK 17 不在 PATH 中,准备添加..." -ForegroundColor Yellow
Write-Host ""

# 创建新的 PATH
$newPath = "$javaHome\bin;$currentPath"

Write-Host "新 PATH (前 200 字符):" -ForegroundColor Yellow
Write-Host $newPath.Substring(0, [Math]::Min(200, $newPath.Length))
Write-Host ""

# 设置新 PATH
try {
    Write-Host "正在设置新的 PATH..." -ForegroundColor Cyan
    [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
    Write-Host "[成功] PATH 已更新!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  重要提示" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. 请关闭当前所有终端窗口" -ForegroundColor Yellow
    Write-Host "2. 重新打开终端" -ForegroundColor Yellow
    Write-Host "3. 验证: java -version" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
} catch {
    Write-Host "[错误] 设置 PATH 失败!" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Read-Host "按回车键退出"
