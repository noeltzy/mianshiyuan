-- ============================================
-- 题目表新增字段迁移脚本
-- 添加：是否需要VIP、收藏量、查看量
-- ============================================

-- 添加是否需要VIP字段
ALTER TABLE `question` 
ADD COLUMN `is_vip_only` TINYINT(1) DEFAULT 0 
COMMENT '是否需要VIP才能查看：0否 1是' 
AFTER `status`;

-- 添加收藏量字段
ALTER TABLE `question` 
ADD COLUMN `favorite_count` INT DEFAULT 0 
COMMENT '收藏量' 
AFTER `is_vip_only`;

-- 添加查看量字段
ALTER TABLE `question` 
ADD COLUMN `view_count` INT DEFAULT 0 
COMMENT '查看量' 
AFTER `favorite_count`;

-- 为现有数据设置默认值（如果字段已存在但值为NULL）
UPDATE `question` SET `is_vip_only` = 0 WHERE `is_vip_only` IS NULL;
UPDATE `question` SET `favorite_count` = 0 WHERE `favorite_count` IS NULL;
UPDATE `question` SET `view_count` = 0 WHERE `view_count` IS NULL;

-- 迁移完成！

