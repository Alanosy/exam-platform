-- -----------------------------------------------------------------------------
-- 白名单参与方式：补齐 exam_user（考试白名单考生表）
--
-- 背景：
--   考试有两种参加方式 —— 白名单 / 公开链接（exam.participant_type，字典 exam_participant_type）。
--   公开链接方式靠 exam_invite 记录「谁加入了这场考试」；
--   白名单方式需要一张「这场考试向哪些人开放」的表，也就是这里的 exam_user。
--   DDL 已于 2026-10-01 补进 ry-exam.sql，本脚本用于在此之前初始化的库。
--
-- 粒度：按用户一行一条（uk_exam_user(exam_id, user_id, del_flag)），
--       部门只是管理后台选人时的筛选维度，选出人后展开成用户入库。
--
-- 幂等：先查 information_schema.TABLES，表已存在则跳过；重复执行无副作用。
-- -----------------------------------------------------------------------------

SET @exist_exam_user := (
  SELECT COUNT(1) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'exam_user'
);

SET @sql_exam_user := IF(@exist_exam_user = 0,
  'CREATE TABLE `exam_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `exam_id` bigint NOT NULL COMMENT ''考试ID'',
  `user_id` bigint NOT NULL COMMENT ''考生用户ID'',
  `tenant_id` bigint DEFAULT NULL COMMENT ''租户ID'',
  `del_flag` tinyint NOT NULL DEFAULT ''0'' COMMENT ''逻辑删除 0未删 1已删'',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '''' COMMENT ''创建者'',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '''' COMMENT ''更新者'',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
  `create_dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT ''创建部门'',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_user` (`exam_id`,`user_id`,`del_flag`),
  KEY `idx_exam_id` (`exam_id`,`del_flag`) USING BTREE,
  KEY `idx_user_id` (`user_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT=''考试白名单考生表''',
  'SELECT 1');

PREPARE stmt FROM @sql_exam_user;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
