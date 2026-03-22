-- V4: add plaintext and retry management
ALTER TABLE parse_tasks
  ADD COLUMN IF NOT EXISTS plaintext TEXT,
  ADD COLUMN IF NOT EXISTS retry_count INTEGER DEFAULT 0,
  ADD COLUMN IF NOT EXISTS max_retries INTEGER DEFAULT 3,
  ADD COLUMN IF NOT EXISTS last_error TEXT;

CREATE INDEX IF NOT EXISTS idx_parse_tasks_status ON parse_tasks(status);

