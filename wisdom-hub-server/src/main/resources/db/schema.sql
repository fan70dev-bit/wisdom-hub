-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS wisdom_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wisdom_hub;

-- 2. 创建用户表
CREATE TABLE IF NOT EXISTS `tb_user` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `account_id` VARCHAR(10) UNIQUE KEY COMMENT '账号ID（8-10位随机数字）',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱（登录凭证）',
    `username` VARCHAR(50) NOT NULL DEFAULT 'Wisdom用户' COMMENT '显示名称',
    `nickname` VARCHAR(50) NULL COMMENT '昵称（兼容旧版）',
    `avatar_url` VARCHAR(500) DEFAULT 'https://java-test-with-ai.oss-cn-beijing.aliyuncs.com/assets/default-croco.png' COMMENT '头像地址',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-临时封禁，2-已注销',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_login_time` DATETIME NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) NULL COMMENT '最后登录IP',
    `last_profile_update` DATETIME NULL COMMENT '上次修改资料时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_account_id` (`account_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 创建帖子表 (整合了去封面图版本和后续增加的统计字段)
CREATE TABLE IF NOT EXISTS `tb_post` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `title` VARCHAR(200) NULL COMMENT '标题（长文可选）',
    `content` TEXT NOT NULL COMMENT '内容（支持Markdown插入图片URL）',
    `video_url` VARCHAR(500) NULL COMMENT '视频链接（BV号）',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型：0-长文，1-碎碎念',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-公开，1-私密，2-待审核，3-违规封禁，4-用户已删除',
    `author_id` VARCHAR(10) NOT NULL COMMENT '作者账号ID（关联 tb_user.account_id）',
    `audit_remark` VARCHAR(500) NULL COMMENT '审核意见或封禁原因',
    `ip_address` VARCHAR(50) NULL COMMENT '发帖IP地址',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_like_count` (`like_count`),
    KEY `idx_favorite_count` (`favorite_count`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章/碎碎念表';

-- 4. 点赞表
CREATE TABLE IF NOT EXISTS `tb_post_like` (
                                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                              `post_id` BIGINT NOT NULL COMMENT '帖子ID',
                                              `user_id` VARCHAR(10) NOT NULL COMMENT '用户账号ID（关联 tb_user.account_id）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞记录表';

-- 5. 收藏表
CREATE TABLE IF NOT EXISTS `tb_post_favorite` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
                                                  `user_id` VARCHAR(10) NOT NULL COMMENT '用户账号ID（关联 tb_user.account_id）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏记录表';

-- 6. 评论表
CREATE TABLE IF NOT EXISTS `tb_comment` (
                                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            `post_id` BIGINT NOT NULL COMMENT '帖子ID',
                                            `user_id` VARCHAR(10) NOT NULL COMMENT '评论者账号ID（关联 tb_user.account_id）',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID（0 表示顶级评论）',
    `reply_to_user_id` VARCHAR(10) DEFAULT NULL COMMENT '回复目标用户ID（用于显示"回复@某人"）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表（支持无限级嵌套）';

-- -- 删除用户表
-- DROP TABLE IF EXISTS tb_user;3429709
--
-- -- 删除帖子表
-- DROP TABLE IF EXISTS tb_post;
--
-- -- 删除评论表
-- DROP TABLE IF EXISTS tb_comment;