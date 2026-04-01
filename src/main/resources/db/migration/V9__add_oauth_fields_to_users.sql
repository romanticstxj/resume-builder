-- 添加 GitHub OAuth 登录所需字段
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS github_id VARCHAR(50),
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(20);

-- github_id 唯一索引（允许 NULL，只对非 NULL 值唯一）
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_github_id ON users (github_id) WHERE github_id IS NOT NULL;

-- OAuth 用户 email/password 可为空（GitHub 不一定公开邮箱）
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
