CREATE TABLE `review` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核ID',
                          `content_id` BIGINT NOT NULL COMMENT '审核对象ID（逻辑外键：bank.id 或 question.id）',
                          `content_type` INT NOT NULL COMMENT '审核对象类型：1题库 2题目 其他待扩展',
                          `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID（逻辑外键：user.id）',
                          `reviewer_type` INT DEFAULT 1 COMMENT '审核方式：1人工 2AI',
                          `result` INT DEFAULT 0 COMMENT '审核结果：0待审 1通过 2驳回',
                          `comments` TEXT COMMENT '审核意见',
                          `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
                          `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          KEY `idx_content_id_type` (`content_id`, `content_type`),
                          KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核表（逻辑外键版）';
