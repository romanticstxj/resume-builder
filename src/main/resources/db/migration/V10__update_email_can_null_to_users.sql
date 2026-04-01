
-- OAuth 用户 email/password 可为空（GitHub 不一定公开邮箱）
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
