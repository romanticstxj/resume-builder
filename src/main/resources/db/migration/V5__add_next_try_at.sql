-- V5: add next_try_at for persistent retry scheduling
ALTER TABLE parse_tasks
  ADD COLUMN IF NOT EXISTS next_try_at TIMESTAMP;

