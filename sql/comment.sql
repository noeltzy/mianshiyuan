CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID（逻辑外键：question.id）',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID（逻辑外键：user.id）',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（空为顶级评论）',
    `comment_type` INT NOT NULL COMMENT '评论类型：1=用户答案, 2=用户评论, 3=AI评分',
    `content` TEXT NOT NULL COMMENT '评论内容（纯文本）',
    `is_pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶：0否 1是',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `sort_order` INT DEFAULT 0 COMMENT '排序（置顶评论优先，然后按时间）',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_question_id` (`question_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_comment_type` (`comment_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表（支持嵌套回复，无需审核）';

/**
  用于定位用户回答 查出comment Id
 */
CREATE INDEX idx_comment_question_user ON comment (question_id, user_id, id);