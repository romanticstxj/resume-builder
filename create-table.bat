@echo off
echo 正在连接数据库并创建parse_tasks表...
echo.
echo 数据库: resume_builder
echo 用户: postgres
echo.

set PGPASSWORD=
if "%PGPASSWORD%"=="" set /p PGPASSWORD=请输入 PostgreSQL 密码: 

psql -h localhost -p 5432 -U postgres -d resume_builder -f create_task_table.sql

if %errorlevel% equ 0 (
    echo.
    echo ✓ 表创建成功！
) else (
    echo.
    echo ✗ 表创建失败，请检查错误信息
)

set PGPASSWORD=

pause
