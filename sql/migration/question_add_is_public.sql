-- 为 question 表新增 is_public 字段
ALTER TABLE `question` 
ADD COLUMN `is_public` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否公开：0私有 1公开' 
AFTER `status`;


