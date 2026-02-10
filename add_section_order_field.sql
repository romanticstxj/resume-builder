-- 更新模板表结构，添加区块顺序字段

-- 检查并添加区块顺序字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'templates' AND column_name = 'section_order'
    ) THEN
        ALTER TABLE templates ADD COLUMN section_order TEXT;
    END IF;
END $$;

-- 更新现有模板的默认区块顺序
UPDATE templates
SET section_order = '["header", "summary", "experience", "education", "projects", "skills", "personalSummary", "honors", "works"]'
WHERE section_order IS NULL;
