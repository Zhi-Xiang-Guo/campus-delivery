-- =============================================
-- 新增评论功能
-- =============================================

-- 1. 评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '绑定的订单id',
  `user_id` bigint NOT NULL COMMENT '评价的用户id',
  `score` tinyint NOT NULL DEFAULT '5' COMMENT '评分：1-5星',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价文字内容',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0:隐藏 1:正常显示',
  `create_time` datetime NOT NULL COMMENT '创建时间/评价时间',
  `reply_content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '商家回复时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';

-- 2. 评论图片表
CREATE TABLE IF NOT EXISTS `comment_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `comment_id` bigint NOT NULL COMMENT '评论表主键',
  `url` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '图片OSS存储地址',
  PRIMARY KEY (`id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='评论图片表';

-- 3. 给订单表新增 is_commented 字段（安全添加，已存在则跳过）
DROP PROCEDURE IF EXISTS add_comment_column;
DELIMITER //
CREATE PROCEDURE add_comment_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'orders'
        AND COLUMN_NAME = 'is_commented'
    ) THEN
        ALTER TABLE `orders`
        ADD COLUMN `is_commented` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已评价 0:否 1:是';
    END IF;
END //
DELIMITER ;
CALL add_comment_column();
DROP PROCEDURE IF EXISTS add_comment_column;
