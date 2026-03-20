-- 创建解析任务表
CREATE TABLE IF NOT EXISTS parse_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    status VARCHAR(50) NOT NULL DEFAULT 'pending' COMMENT '任务状态: pending-待处理, processing-处理中, completed-已完成, failed-失败',
    progress INT DEFAULT 0 COMMENT '进度百分比(0-100)',
    error_message TEXT COMMENT '错误信息',
    parse_result JSON COMMENT '解析结果',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历解析任务表';
