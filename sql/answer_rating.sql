CREATE TABLE `answer_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分ID',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID（逻辑外键：comment.id）',
    `feedback` TEXT COMMENT '评价内容',
    `score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '总体得分（0-100分）',
    `rater_type` INT NOT NULL DEFAULT 0 COMMENT '评分者类型：0=AI 1=人工',
    `rater_id` BIGINT DEFAULT NULL COMMENT '评分者ID（逻辑外键：user.id，AI评分时为NULL）',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_comment_id` (`comment_id`),
    KEY `idx_rater_type` (`rater_type`),
    KEY `idx_rater_id` (`rater_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回答评分表';