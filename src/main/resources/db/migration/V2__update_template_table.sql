-- 更新模板表结构，添加模板布局和样式配置字段 (PostgreSQL)

-- 添加布局配置字段
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_name='templates' AND column_name='layout') THEN
        ALTER TABLE templates ADD COLUMN layout VARCHAR(50) DEFAULT 'classic';
    END IF;
END $$;

-- 添加主题配置字段（JSON格式存储颜色、字体等）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_name='templates' AND column_name='theme_config') THEN
        ALTER TABLE templates ADD COLUMN theme_config TEXT;
    END IF;
END $$;

-- 添加区块配置字段（JSON格式存储各区块的显示和样式）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_name='templates' AND column_name='section_config') THEN
        ALTER TABLE templates ADD COLUMN section_config TEXT;
    END IF;
END $$;

-- 添加区块顺序字段（JSON格式存储区块顺序）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_name='templates' AND column_name='section_order') THEN
        ALTER TABLE templates ADD COLUMN section_order TEXT;
    END IF;
END $$;

-- 更新现有模板为默认配置
UPDATE templates SET
    layout = 'classic',
    theme_config = '{
        "primaryColor": "#2c3e50",
        "secondaryColor": "#3498db",
        "textColor": "#333333",
        "fontFamily": "Arial, sans-serif",
        "fontSize": "14px"
    }',
    section_config = '{
        "header": {"show": true, "style": "left", "background": false},
        "summary": {"show": true, "style": "plain"},
        "experience": {"show": true, "style": "timeline"},
        "education": {"show": true, "style": "list"},
        "projects": {"show": true, "style": "list"},
        "skills": {"show": true, "style": "tags"},
        "personalSummary": {"show": true, "style": "plain"},
        "honors": {"show": true, "style": "list"},
        "works": {"show": true, "style": "list"}
    }',
    section_order = '["header", "summary", "experience", "education", "projects", "skills", "personalSummary", "honors", "works"]'
WHERE layout IS NULL;


