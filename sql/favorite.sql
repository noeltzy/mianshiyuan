CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（逻辑外键：user.id）',
    `target_type` INT NOT NULL COMMENT '收藏目标类型：1=题目, 2=题库, 3=用户题解',
    `target_id` BIGINT NOT NULL COMMENT '收藏目标ID',
    `collection_name` VARCHAR(100) DEFAULT 'default' COMMENT '收藏夹名称（支持用户自定义分类）',
    `note` TEXT DEFAULT NULL COMMENT '收藏备注/标签',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_target_type` (`target_type`),
    KEY `idx_collection` (`user_id`, `collection_name`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用收藏表';