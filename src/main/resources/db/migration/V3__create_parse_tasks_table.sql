-- 创建解析任务表 (PostgreSQL)
CREATE TABLE IF NOT EXISTS parse_tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    progress INTEGER DEFAULT 0,
    error_message TEXT,
    parse_result JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_parse_tasks_user_id ON parse_tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_parse_tasks_status ON parse_tasks(status);
CREATE INDEX IF NOT EXISTS idx_parse_tasks_created_at ON parse_tasks(created_at);

-- 创建更新时间自动更新的触发器
CREATE OR REPLACE FUNCTION update_parse_tasks_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS parse_tasks_updated_at_trigger ON parse_tasks;
CREATE TRIGGER parse_tasks_updated_at_trigger
    BEFORE UPDATE ON parse_tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_parse_tasks_updated_at();
