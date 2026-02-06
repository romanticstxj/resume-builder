-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    phone VARCHAR(20) COMMENT '手机号',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 简历表
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(100) NOT NULL COMMENT '简历标题',
    template_id BIGINT COMMENT '使用的模板ID',
    content JSON NOT NULL COMMENT '简历内容JSON',
    status ENUM('draft', 'completed') DEFAULT 'draft' COMMENT '状态:草稿-完成',
    is_primary BOOLEAN DEFAULT FALSE COMMENT '是否主简历',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

-- 模板表
CREATE TABLE IF NOT EXISTS templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    category VARCHAR(50) NOT NULL COMMENT '分类:技术-设计-产品-其他',
    description TEXT COMMENT '模板描述',
    preview_url VARCHAR(255) COMMENT '预览图URL',
    content JSON NOT NULL COMMENT '模板配置JSON',
    is_official BOOLEAN DEFAULT FALSE COMMENT '是否官方模板',
    is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    created_by BIGINT COMMENT '创建者ID',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_is_official (is_official),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板表';

-- AI任务表
CREATE TABLE IF NOT EXISTS ai_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_id BIGINT COMMENT '关联简历ID',
    task_type ENUM('generate', 'translate', 'optimize', 'match') NOT NULL COMMENT '任务类型:生成-翻译-优化-匹配',
    status ENUM('pending', 'processing', 'completed', 'failed') DEFAULT 'pending' COMMENT '任务状态',
    input_data JSON COMMENT '输入数据',
    result_data JSON COMMENT '结果数据',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI任务表';

-- 插入官方模板示例数据
INSERT INTO templates (name, category, description, preview_url, content, is_official, is_public, usage_count) VALUES
('简洁经典', '技术', '简洁大方的经典简历模板，适合技术岗位', '/templates/classic.png', 
 '{"style": "classic", "colors": {"primary": "#333333", "secondary": "#666666"}, "sections": ["contact", "summary", "experience", "education", "skills"]}', 
 true, true, 0),
('现代简约', '设计', '现代简约风格，突出个人特色', '/templates/modern.png',
 '{"style": "modern", "colors": {"primary": "#007bff", "secondary": "#6c757d"}, "sections": ["contact", "summary", "experience", "education", "skills", "projects"]}',
 true, true, 0),
('极简主义', '产品', '极简设计，内容精炼', '/templates/minimal.png',
 '{"style": "minimal", "colors": {"primary": "#212529", "secondary": "#495057"}, "sections": ["contact", "summary", "experience", "skills"]}',
 true, true, 0);
