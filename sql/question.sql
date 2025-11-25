CREATE TABLE `question` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
                            `title` VARCHAR(255) NOT NULL COMMENT '题目标题',
                            `description` TEXT COMMENT '题目描述',
                            `tag_list` VARCHAR(500) DEFAULT '[]' COMMENT '标签列表，JSON格式：["tag1","tag2"]',
                            `answer` TEXT COMMENT '标准答案',
                            `difficulty` INT DEFAULT 1 COMMENT '难度：0简单 1中等 2困难',
                            `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                            `status` INT DEFAULT 0 COMMENT '状态：0草稿 1待审 2通过 3驳回',
                            `is_vip_only` TINYINT(1) DEFAULT 0 COMMENT '是否需要VIP才能查看：0否 1是',
                            `favorite_count` INT DEFAULT 0 COMMENT '收藏量',
                            `view_count` INT DEFAULT 0 COMMENT '查看量',
                            `review_id` BIGINT DEFAULT NULL COMMENT '最新审核记录ID',
                            `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
                            `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';


