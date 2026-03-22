-- 补充 users 表 password 字段（若已存在则跳过）
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT '';
