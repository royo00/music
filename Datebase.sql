-- 数据库名： "music_db"
-- 用户表
CREATE TABLE `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(加密)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin-管理员, user-普通用户, actor-艺人',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 音乐表
CREATE TABLE `t_music` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '音乐ID',
  `music_name` VARCHAR(100) NOT NULL COMMENT '音乐名称',
  `artist` VARCHAR(100) NOT NULL COMMENT '艺术家/作者',
  `album` VARCHAR(100) DEFAULT NULL COMMENT '专辑名称',
  `duration` INT DEFAULT NULL COMMENT '时长(秒)',
  `file_uuid` VARCHAR(50) NOT NULL COMMENT '文件UUID',
  `file_path` VARCHAR(255) NOT NULL COMMENT '文件存储路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已发布, 2-已下架',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '简介',
  `play_count` BIGINT NOT NULL DEFAULT 0 COMMENT '播放次数',
  `upload_user_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_uuid` (`file_uuid`),
  KEY `idx_music_name` (`music_name`),
  KEY `idx_artist` (`artist`),
  KEY `idx_status` (`status`),
  KEY `idx_upload_user_id` (`upload_user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐表';

-- 收藏表
CREATE TABLE `t_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `music_id` BIGINT NOT NULL COMMENT '音乐ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_music` (`user_id`, `music_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_music_id` (`music_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 播放历史表
CREATE TABLE `t_play_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `music_id` BIGINT NOT NULL COMMENT '音乐ID',
  `play_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_music_id` (`music_id`),
  KEY `idx_play_time` (`play_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播放历史表';

-- 评分表
CREATE TABLE `t_rating` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `music_id` BIGINT NOT NULL COMMENT '音乐ID',
  `score` TINYINT NOT NULL COMMENT '评分(1-5分)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_music` (`user_id`, `music_id`) COMMENT '唯一索引防刷',
  KEY `idx_music_id` (`music_id`), -- 用于查单曲评分
  CONSTRAINT `fk_rating_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rating_music` FOREIGN KEY (`music_id`) REFERENCES `t_music` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_score` CHECK (`score` >= 1 AND `score` <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分表';
