-- ============================================
-- 迁移SQL：为bank表添加cover_image字段
-- 执行时间：2025-11-12
-- 说明：为题库表添加头图URL字段，用于存储题库的头图链接
-- ============================================

-- 添加cover_image字段
ALTER TABLE `bank` 
ADD COLUMN `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '头图URL' 
AFTER `description`;

-- 验证字段是否添加成功（可选，不执行）
-- DESCRIBE `bank`;

