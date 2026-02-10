-- 检查字段是否存在，如果不存在则添加

-- 1. 检查并添加 layout 字段
ALTER TABLE templates ADD COLUMN IF NOT EXISTS layout VARCHAR(50) DEFAULT 'classic';

-- 2. 检查并添加 theme_config 字段
ALTER TABLE templates ADD COLUMN IF NOT EXISTS theme_config TEXT;

-- 3. 检查并添加 section_config 字段
ALTER TABLE templates ADD COLUMN IF NOT EXISTS section_config TEXT;

-- 4. 检查并添加 section_order 字段
ALTER TABLE templates ADD COLUMN IF NOT EXISTS section_order TEXT;

-- 更新现有模板为默认配置
UPDATE templates SET
    layout = 'classic',
    theme_config = '{"primaryColor": "#2c3e50", "secondaryColor": "#3498db", "textColor": "#333333", "fontFamily": "Arial, sans-serif", "fontSize": "14px"}',
    section_config = '{"header": {"show": true, "style": "left", "background": false}, "summary": {"show": true, "style": "plain"}, "experience": {"show": true, "style": "timeline"}, "education": {"show": true, "style": "list"}, "projects": {"show": true, "style": "list"}, "skills": {"show": true, "style": "tags"}, "personalSummary": {"show": true, "style": "plain"}, "honors": {"show": true, "style": "list"}, "works": {"show": true, "style": "list"}}',
    section_order = '["header", "summary", "experience", "education", "projects", "skills", "personalSummary", "honors", "works"]'
WHERE layout IS NULL OR section_order IS NULL;
