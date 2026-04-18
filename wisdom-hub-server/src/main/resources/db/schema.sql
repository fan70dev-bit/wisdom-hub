CREATE DATABASE wisdom_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE wisdom_hub;

-- 用户表
CREATE TABLE `tb_user` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
                           `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
                           `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-禁用',
                           `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `last_login_time` DATETIME NULL COMMENT '最后登录时间',
                           `last_login_ip` VARCHAR(50) NULL COMMENT '最后登录IP',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_email` (`email`),
                           KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';