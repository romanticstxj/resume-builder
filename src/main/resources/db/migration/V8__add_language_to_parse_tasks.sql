-- Add language column to parse_tasks to track resume language (zh/en)
ALTER TABLE parse_tasks ADD COLUMN IF NOT EXISTS language VARCHAR(10) DEFAULT 'zh';
