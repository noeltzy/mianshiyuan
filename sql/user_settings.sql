CREATE TABLE `user_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（逻辑外键：user.id）',
    `setting_key` VARCHAR(100) NOT NULL COMMENT '设置键名（如：email_notification, theme, language等）',
    `setting_value` TEXT COMMENT '设置值（支持字符串、数字、布尔值、JSON等）',
    `setting_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '设置值类型：STRING, NUMBER, BOOLEAN, JSON',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_setting_key` (`user_id`, `setting_key`) COMMENT '同一用户同一设置键只能有一条记录',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_setting_key` (`setting_key`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表（支持动态扩展设置项）';