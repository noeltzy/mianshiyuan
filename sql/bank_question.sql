CREATE TABLE `bank_question` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `bank_id` BIGINT NOT NULL COMMENT '题库ID（逻辑外键：bank.id）',
                                 `question_id` BIGINT NOT NULL COMMENT '题目ID（逻辑外键：question.id）',
                                 `sort_order` INT DEFAULT 0 COMMENT '排序序号（题库内题目顺序）',
                                 `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
                                 `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_bank_question` (`bank_id`, `question_id`),
                                 KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库-题目关联表（多对多逻辑关系）';
