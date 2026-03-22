-- V6: add processing_by and processing_at to track which worker claimed the task
ALTER TABLE parse_tasks
  ADD COLUMN IF NOT EXISTS processing_by VARCHAR(200),
  ADD COLUMN IF NOT EXISTS processing_at TIMESTAMP;

