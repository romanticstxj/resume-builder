-- -- 创建数据库
-- CREATE DATABASE IF NOT EXISTS resume_builder;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 模板表
CREATE TABLE IF NOT EXISTS templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    description VARCHAR(500),
    preview_url VARCHAR(500),
    content TEXT NOT NULL,
    is_official BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 简历表
CREATE TABLE IF NOT EXISTS resumes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    template_id BIGINT,
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'draft',
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 插入测试数据
INSERT INTO users (username, email, password) VALUES
('admin', 'admin@example.com', '123456'),
('test', 'test@example.com', '123456');

-- 插入官方模板示例
INSERT INTO templates (name, category, description, preview_url, content, is_official, is_public, created_by) VALUES
('简约专业模板', '专业', '适合应届生的简约专业简历模板', '', '{"layout": "simple", "sections": ["basic", "education", "experience"]}', true, true, NULL),
('创意设计模板', '设计', '突出个人特色的创意简历模板', '', '{"layout": "creative", "sections": ["basic", "portfolio", "skills"]}', true, true, NULL);
