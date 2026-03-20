@echo off
set PGPASSWORD=1234qwer
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d resume_builder -f create_task_table.sql
if %errorlevel% equ 0 (
    echo ✓ 表创建成功！
) else (
    echo ✗ 表创建失败
)
set PGPASSWORD=
pause
