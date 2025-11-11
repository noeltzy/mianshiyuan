CREATE TABLE `bank` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题库ID',
                        `name` VARCHAR(100) NOT NULL COMMENT '题库名称',
                        `description` TEXT COMMENT '题库简介',
                        `tag_list` VARCHAR(500) DEFAULT '[]' COMMENT '标签列表，JSON格式：["tag1","tag2"]',
                        `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                        `status` INT DEFAULT 0 COMMENT '状态：0草稿 1待审 2通过 3驳回',
                        `review_id` BIGINT DEFAULT NULL COMMENT '最新审核记录ID',
                        `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
                        `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';



