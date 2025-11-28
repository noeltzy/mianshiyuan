-- 修复 bank 表的 public 列名为 is_public
-- public 是 MySQL 保留字，改为 is_public 更规范

-- 检查列是否存在，如果存在则重命名
ALTER TABLE `bank` CHANGE COLUMN `public` `is_public` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否公开';

