CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名/登录账号',
                        `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                        `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '用户头像',
                        `role` INT NOT NULL DEFAULT 0 COMMENT '用户角色：0=USER, 1=ADMIN, 2=REVIEWER',
                        `email` VARCHAR(100) DEFAULT NULL COMMENT '用户邮箱（可选）',
                        `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（可选）',
                        `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志: 0=正常, 1=已删除',
                        `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（支持逻辑删除）';
