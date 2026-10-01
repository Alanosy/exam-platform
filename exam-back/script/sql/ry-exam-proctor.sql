-- ----------------------------
-- 防作弊模块表结构（考试主库 ry-exam）
--
-- 说明：ruoyi-exam-proctor 服务的数据源指向考试主库（见 script/config/nacos/ruoyi-exam-proctor.yml），
--       所以三张表都建在 ry-exam，便于与 exam / exam_record(跨库只存ID) 关联。
--       答卷明细在 ry-exam-answer 库，跨库一律走 Dubbo，不做直连。
--
-- 三张表的关系：
--   exam_proctor_session   一场考试 × 一份答卷 一条：计数汇总 + 在线状态 + 风险等级（监考列表用它）
--   exam_proctor_event     每一次可疑动作的流水（切屏 / 粘贴 / 退出全屏 / 开开发者工具 …）
--   exam_proctor_snapshot  摄像头抓拍（图片存 OSS，这里只存 ossId 与访问地址）
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exam_proctor_session
-- ----------------------------
CREATE TABLE IF NOT EXISTS `exam_proctor_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `record_id` bigint NOT NULL COMMENT '答卷记录ID（答题库 exam_record 主键，跨库只存ID）',
  `user_id` bigint DEFAULT NULL COMMENT '考生用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '考生账号（登录名）',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '考生姓名快照',
  `attempt_no` int NOT NULL DEFAULT '1' COMMENT '第几次参加（快照）',
  `switch_count` int NOT NULL DEFAULT '0' COMMENT '切屏 / 离屏次数（visibilitychange 隐藏 + window 失焦去重后计一次）',
  `blur_count` int NOT NULL DEFAULT '0' COMMENT '窗口失焦次数',
  `copy_count` int NOT NULL DEFAULT '0' COMMENT '复制次数',
  `paste_count` int NOT NULL DEFAULT '0' COMMENT '粘贴次数',
  `cut_count` int NOT NULL DEFAULT '0' COMMENT '剪切次数',
  `contextmenu_count` int NOT NULL DEFAULT '0' COMMENT '右键菜单次数（禁复制时的辅助信号）',
  `exit_fullscreen_count` int NOT NULL DEFAULT '0' COMMENT '退出全屏次数',
  `camera_count` int NOT NULL DEFAULT '0' COMMENT '摄像头抓拍张数',
  `devtool_count` int NOT NULL DEFAULT '0' COMMENT '疑似打开开发者工具次数',
  `multitab_count` int NOT NULL DEFAULT '0' COMMENT '同账号多标签页 / 多端同时作答次数',
  `max_switch` int NOT NULL DEFAULT '0' COMMENT '允许切屏次数（开卷快照），0 不限制，超过强制交卷',
  `max_exit_fullscreen` int NOT NULL DEFAULT '0' COMMENT '允许退出全屏次数（开卷快照），0 不限制',
  `max_paste` int NOT NULL DEFAULT '0' COMMENT '允许粘贴次数（开卷快照），0 不限制',
  `camera_interval` int NOT NULL DEFAULT '60' COMMENT '摄像头抓拍间隔（秒，开卷快照）',
  `force_submit` tinyint NOT NULL DEFAULT '0' COMMENT '是否已因违规触发强制交卷 0否 1是',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'online' COMMENT 'online作答中 / offline掉线 / submitted已交卷 / force_submit强制交卷',
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'normal' COMMENT '风险等级 normal正常 / suspect可疑 / serious严重',
  `risk_score` int NOT NULL DEFAULT '0' COMMENT '风险分，越可疑越高，用于排序与分级',
  `start_time` datetime DEFAULT NULL COMMENT '进入答题页时间',
  `last_active_time` datetime DEFAULT NULL COMMENT '最近一次心跳 / 事件时间，用来判断掉线',
  `end_time` datetime DEFAULT NULL COMMENT '交卷 / 离开时间',
  `duration_seconds` int NOT NULL DEFAULT '0' COMMENT '在线时长（秒）',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '浏览器 UA',
  `device` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '设备信息（屏幕 / 系统 / 浏览器，前端拼接）',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_record` (`exam_id`, `record_id`, `del_flag`) COMMENT '一份答卷只有一条监考会话',
  KEY `idx_exam_status` (`exam_id`, `status`),
  KEY `idx_exam_risk` (`exam_id`, `risk_level`),
  KEY `idx_user` (`user_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '监考会话（一场考试 × 一份答卷）';

-- ----------------------------
-- Table structure for exam_proctor_event
-- ----------------------------
CREATE TABLE IF NOT EXISTS `exam_proctor_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '监考会话ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `record_id` bigint DEFAULT NULL COMMENT '答卷记录ID',
  `user_id` bigint DEFAULT NULL COMMENT '考生用户ID',
  `event_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '事件类型 switch_screen / blur / copy / paste / cut / contextmenu / exit_fullscreen / camera / camera_deny / devtool / multitab / force_submit',
  `event_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '事件名称（中文，列表直接展示）',
  `level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'info' COMMENT '级别 info提示 / warn可疑 / danger严重',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '事件摘要，如被粘贴内容的前 200 字',
  `extra` json DEFAULT NULL COMMENT '扩展信息（键位、屏幕尺寸、抓拍 ossId 等）',
  `event_time` datetime DEFAULT NULL COMMENT '事件发生的客户端时间',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`),
  KEY `idx_session_time` (`session_id`, `event_time`),
  KEY `idx_exam_time` (`exam_id`, `event_time`),
  KEY `idx_exam_type` (`exam_id`, `event_type`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '防作弊事件流水';

-- ----------------------------
-- Table structure for exam_proctor_snapshot
-- ----------------------------
CREATE TABLE IF NOT EXISTS `exam_proctor_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '监考会话ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `record_id` bigint DEFAULT NULL COMMENT '答卷记录ID',
  `user_id` bigint DEFAULT NULL COMMENT '考生用户ID',
  `oss_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'OSS 记录ID',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '抓拍图片访问地址',
  `event_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'periodic' COMMENT '触发场景 periodic定时 / enter入场 / switch_screen切屏 / resume恢复 / manual手动',
  `capture_time` datetime DEFAULT NULL COMMENT '抓拍时间',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`),
  KEY `idx_session_time` (`session_id`, `capture_time`),
  KEY `idx_exam_time` (`exam_id`, `capture_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摄像头抓拍';

SET FOREIGN_KEY_CHECKS = 1;
