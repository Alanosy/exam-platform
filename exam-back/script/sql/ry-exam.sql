/*
 Navicat Premium Data Transfer

 Source Server         : 8.137.151.232
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : 8.137.151.232:3306
 Source Schema         : ry-exam

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 30/09/2026 20:29:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exam
-- ----------------------------
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '考试ID',
  `exam_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考试名称',
  `exam_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '考试描述说明',
  `paper_id` bigint NOT NULL COMMENT '关联试卷ID，paper表主键',
  `exam_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '任务类型 1正式考试 2练习考试，取字典 exam_type',
  `start_time` datetime NULL COMMENT '考试开始时间，练习考试可为空表示长期有效',
  `end_time` datetime NULL COMMENT '考试结束时间，练习考试可为空表示长期有效',
  `duration` int DEFAULT '0' COMMENT '本场考试限时(分钟)，0不限时',
  `allow_late` tinyint NOT NULL DEFAULT '0' COMMENT '是否允许迟到入场 0否 1是',
  `late_minute` int DEFAULT '0' COMMENT '允许迟到多少分钟，超过无法进入',
  `allow_retry` tinyint NOT NULL DEFAULT '0' COMMENT '是否允许重考 0否 1是',
  `max_retry_count` int DEFAULT '1' COMMENT '单个考生最大重考次数',
  `show_answer_mode` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'none' COMMENT '答案展示 none不展示 / after_submit交卷后 / after_exam考试结束',
  `anti_cheat_config` json DEFAULT NULL COMMENT '防作弊配置：切屏次数、禁止复制粘贴、摄像头抓拍、全屏限制等',
  `participant_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'white' COMMENT '考生准入类型 white白名单 / public公开链接',
  `join_password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公开考试参与密码，public模式生效，为空无密码',
  `join_expire_time` datetime DEFAULT NULL COMMENT '公开考试链接有效期，NULL和考试结束时间一致',
  `join_code` varchar(32) DEFAULT NULL COMMENT '公开考试加入码，participant_type=public 时有效',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'not_start' COMMENT 'not_start未开始 / ongoing进行中 / finished已结束 / archived归档',
  `creator_id` bigint NOT NULL COMMENT '考试创建人ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_join_code` (`join_code`),
  KEY `idx_paper_id` (`paper_id`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`) USING BTREE,
  KEY `idx_status` (`status`,`del_flag`) USING BTREE,
  KEY `idx_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105193340510367746 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试主表';

-- ----------------------------
-- Records of exam
-- ----------------------------
BEGIN;
INSERT INTO `exam` (`id`, `exam_name`, `exam_desc`, `paper_id`, `start_time`, `end_time`, `duration`, `allow_late`, `late_minute`, `allow_retry`, `max_retry_count`, `show_answer_mode`, `anti_cheat_config`, `participant_type`, `join_password`, `join_expire_time`, `join_code`, `status`, `creator_id`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104961294205345793, 'ces', 'sdfasd', 2104943974758174722, '2026-09-01 00:00:00', '2026-09-23 00:00:00', 60, 0, 0, 0, 0, 'none', '{\"camera\": 0, \"copyPaste\": 1, \"fullScreen\": 0, \"switchScreen\": 0}', 'public', 'WYR6XU', NULL, 'Mh7hJt8xhy', 'ongoing', 1, 0, 0, '1', '2026-09-29 23:47:35', '1', '2026-09-29 23:47:35', '103');
INSERT INTO `exam` (`id`, `exam_name`, `exam_desc`, `paper_id`, `start_time`, `end_time`, `duration`, `allow_late`, `late_minute`, `allow_retry`, `max_retry_count`, `show_answer_mode`, `anti_cheat_config`, `participant_type`, `join_password`, `join_expire_time`, `join_code`, `status`, `creator_id`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104961647806144514, 'aa', 'jjjjj', 2104943974758174722, '2026-09-09 00:00:00', '2026-09-30 00:00:00', 60, 0, 0, 0, 0, 'none', '{\"camera\": 0, \"copyPaste\": 1, \"fullScreen\": 0, \"switchScreen\": 0}', 'public', '2BNT4K', NULL, 'RLSL7Y6oQI', 'not_start', 1, 0, 0, '1', '2026-09-29 23:49:00', '1', '2026-09-29 23:49:19', '103');
INSERT INTO `exam` (`id`, `exam_name`, `exam_desc`, `paper_id`, `start_time`, `end_time`, `duration`, `allow_late`, `late_minute`, `allow_retry`, `max_retry_count`, `show_answer_mode`, `anti_cheat_config`, `participant_type`, `join_password`, `join_expire_time`, `join_code`, `status`, `creator_id`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104965440593616897, 'weert', '', 2104943974758174722, '2026-09-02 00:00:00', '2026-09-25 00:00:00', 0, 0, 0, 0, 0, 'none', '{\"camera\": 0, \"copyPaste\": 1, \"fullScreen\": 0, \"switchScreen\": 0}', 'public', '37BXDM', NULL, 'XZhTGhMjpp', 'ongoing', 1, 0, 0, '1', '2026-09-30 00:04:04', '1', '2026-09-30 00:04:04', '103');
INSERT INTO `exam` (`id`, `exam_name`, `exam_desc`, `paper_id`, `start_time`, `end_time`, `duration`, `allow_late`, `late_minute`, `allow_retry`, `max_retry_count`, `show_answer_mode`, `anti_cheat_config`, `participant_type`, `join_password`, `join_expire_time`, `join_code`, `status`, `creator_id`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105157288596815874, 'aaaaaa', '', 2104943974758174722, '2026-09-15 00:00:00', '2026-10-01 12:45:43', 60, 0, 0, 0, 0, 'none', '{\"camera\": 0, \"copyPaste\": 1, \"fullScreen\": 0, \"switchScreen\": 0}', 'public', 'FUJZH4', NULL, 'si6NtwvjEe', 'ongoing', 1, 0, 0, '1', '2026-09-30 12:46:24', '1', '2026-09-30 12:46:24', '103');
INSERT INTO `exam` (`id`, `exam_name`, `exam_desc`, `paper_id`, `start_time`, `end_time`, `duration`, `allow_late`, `late_minute`, `allow_retry`, `max_retry_count`, `show_answer_mode`, `anti_cheat_config`, `participant_type`, `join_password`, `join_expire_time`, `join_code`, `status`, `creator_id`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105174045227184129, 'aaaabbb', '', 2104943974758174722, '2026-09-30 13:52:44', '2026-10-01 00:00:00', 888, 1, 9999999, 1, 999, 'after_exam', '{\"camera\": 0, \"copyPaste\": 1, \"fullScreen\": 0, \"switchScreen\": 0}', 'public', NULL, NULL, 'diLpRYm6Ht', 'ongoing', 1, 0, 0, '1', '2026-09-30 13:52:59', '1', '2026-09-30 15:07:42', '103');
INSERT INTO `exam` (`id`, `exam_name`, `exam_desc`, `paper_id`, `start_time`, `end_time`, `duration`, `allow_late`, `late_minute`, `allow_retry`, `max_retry_count`, `show_answer_mode`, `anti_cheat_config`, `participant_type`, `join_password`, `join_expire_time`, `join_code`, `status`, `creator_id`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105193340510367745, 'ces', 'aaa', 2104943974758174722, '2026-09-30 15:09:05', '2026-10-01 00:00:00', 0, 1, 9999, 1, 9999, 'after_submit', '{\"camera\": 0, \"copyPaste\": 1, \"fullScreen\": 0, \"switchScreen\": 666}', 'public', NULL, NULL, 'iGSBDiXTgN', 'ongoing', 1, 0, 0, '1', '2026-09-30 15:09:40', '1', '2026-09-30 15:09:40', '103');
COMMIT;

-- ----------------------------
-- Table structure for exam_invite
-- ----------------------------
DROP TABLE IF EXISTS `exam_invite`;
CREATE TABLE `exam_invite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `invite_account` varchar(100) NOT NULL COMMENT '邀请账号：手机号/邮箱',
  `invite_type` varchar(20) NOT NULL COMMENT 'sms短信 / email邮件',
  `invite_status` varchar(20) NOT NULL DEFAULT 'send' COMMENT 'send已发送 / accept已进入考试 / expire已过期',
  `invite_time` datetime NOT NULL COMMENT '邀请发送时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105193458294812674 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试邀请记录表';

-- ----------------------------
-- Records of exam_invite
-- ----------------------------
BEGIN;
INSERT INTO `exam_invite` (`id`, `exam_id`, `invite_account`, `invite_type`, `invite_status`, `invite_time`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105157355592433666, 2105157288596815874, 'admin', 'link', 'accept', '2026-09-30 12:46:40', 0, 0, '1', '2026-09-30 12:46:40', '1', '2026-09-30 12:46:40', '103');
INSERT INTO `exam_invite` (`id`, `exam_id`, `invite_account`, `invite_type`, `invite_status`, `invite_time`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105174136117751809, 2105174045227184129, 'admin', 'link', 'accept', '2026-09-30 13:53:21', 0, 0, '1', '2026-09-30 13:53:21', '1', '2026-09-30 13:53:21', '103');
INSERT INTO `exam_invite` (`id`, `exam_id`, `invite_account`, `invite_type`, `invite_status`, `invite_time`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105193458294812673, 2105193340510367745, 'admin', 'link', 'accept', '2026-09-30 15:10:08', 0, 0, '1', '2026-09-30 15:10:08', '1', '2026-09-30 15:10:08', '103');
COMMIT;

-- ----------------------------
-- Table structure for exam_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` bigint NOT NULL COMMENT '试卷ID',
  `question_id` bigint NOT NULL COMMENT '题库试题ID',
  `exam_question_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '该题在本试卷的分值，覆盖原题默认分值',
  `sort` int NOT NULL DEFAULT '0' COMMENT '题目在试卷内排序',
  `is_question_snapshot` tinyint NOT NULL DEFAULT '0' COMMENT '0引用原题 1开启快照，独立存储题目',
  `snapshot_content` json DEFAULT NULL COMMENT '快照内容，is_question_snapshot=1时生效，存储题干、选项、答案',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_exam_id` (`exam_id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='试卷试题关联表';

-- ----------------------------
-- Records of exam_question
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for exam_user
-- ----------------------------
DROP TABLE IF EXISTS `exam_user`;
CREATE TABLE `exam_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `user_id` bigint NOT NULL COMMENT '考生用户ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_user` (`exam_id`,`user_id`,`del_flag`),
  KEY `idx_exam_id` (`exam_id`,`del_flag`) USING BTREE,
  KEY `idx_user_id` (`user_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试白名单考生表';

-- ----------------------------
-- Records of exam_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for paper
-- ----------------------------
DROP TABLE IF EXISTS `paper`;
CREATE TABLE `paper` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '试卷主键ID',
  `paper_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试卷名称',
  `paper_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '试卷描述',
  `paper_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)',
  `total_score` decimal(6,2) DEFAULT '0.00' COMMENT '试卷总分',
  `pass_score` decimal(6,2) DEFAULT '0.00' COMMENT '及格分数',
  `time_limit` int DEFAULT '0' COMMENT '已废弃：考试时长属于活动规则，改由 exam.duration 配置，代码不再读写该列',
  `visibility` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'private' COMMENT '可见性 private私有 / public公开',
  `share_password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公开分享密码，公开模式生效，空则无密码',
  `share_expire_time` datetime DEFAULT NULL COMMENT '已废弃：分享链接有效期属于活动规则，改由 exam.join_expire_time 配置，代码不再读写该列',
  `random_rule` json DEFAULT NULL COMMENT '随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT 'draft草稿 / ready已组卷 / archived归档',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `category` varchar(64) DEFAULT NULL COMMENT '试卷分类，取字典 paper_category 的字典值',
  `default_score` int DEFAULT '0' COMMENT '默认单题分值',
  `question_shuffle` char(1) DEFAULT '0' COMMENT '是否开启题目乱序 0否 1是',
  `option_shuffle` char(1) DEFAULT '0' COMMENT '是否开启选项乱序 0否 1是',
  `auto_judge` char(1) DEFAULT '1' COMMENT '客观题是否自动判分 0否 1是',
  `manual_review` char(1) DEFAULT '1' COMMENT '主观题是否人工阅卷 0否 1是',
  `partial_score` char(1) DEFAULT '0' COMMENT '是否支持部分得分 0否 1是',
  `wrong_deduct` char(1) DEFAULT '0' COMMENT '答错是否扣分 0否 1是',
  `share_scope` varchar(20) DEFAULT 'SELF' COMMENT '可见范围 SELF仅自己可编辑 SHARED共享给其他管理员',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`) USING BTREE,
  KEY `idx_visibility` (`visibility`) USING BTREE,
  KEY `idx_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2104965721985339395 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试卷主表';

-- ----------------------------
-- Records of paper
-- ----------------------------
BEGIN;
INSERT INTO `paper` (`id`, `paper_name`, `paper_desc`, `paper_type`, `total_score`, `pass_score`, `time_limit`, `visibility`, `share_password`, `share_expire_time`, `random_rule`, `status`, `creator_id`, `category`, `default_score`, `question_shuffle`, `option_shuffle`, `auto_judge`, `manual_review`, `partial_score`, `wrong_deduct`, `share_scope`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104943974758174722, '测试试卷', 'sdfsdf', 'MANUAL', 0.00, 4.00, 1, 'private', NULL, NULL, NULL, 'ready', 1, 'entry', 5, '0', '0', '1', '1', '0', '0', 'SELF', 0, 0, '1', '2026-09-29 22:38:46', '1', '2026-09-29 22:38:46', '103');
INSERT INTO `paper` (`id`, `paper_name`, `paper_desc`, `paper_type`, `total_score`, `pass_score`, `time_limit`, `visibility`, `share_password`, `share_expire_time`, `random_rule`, `status`, `creator_id`, `category`, `default_score`, `question_shuffle`, `option_shuffle`, `auto_judge`, `manual_review`, `partial_score`, `wrong_deduct`, `share_scope`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104965721985339394, 'sdfgdfg', '', 'MANUAL', 20.00, 0.00, 0, 'private', NULL, NULL, NULL, 'ready', 1, 'entry', 5, '0', '0', '1', '1', '0', '0', 'SELF', 0, 0, '1', '2026-09-30 00:05:11', '1', '2026-09-30 00:05:11', '103');
COMMIT;

-- ----------------------------
-- Table structure for paper_question
-- ----------------------------
DROP TABLE IF EXISTS `paper_question`;
CREATE TABLE `paper_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `question_id` bigint NOT NULL COMMENT '试题ID，关联question表',
  `paper_score` decimal(5,2) DEFAULT NULL COMMENT '该题目在本试卷内分值，null使用question表默认score',
  `sort` int NOT NULL DEFAULT '0' COMMENT '题目在试卷中的排序',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_paper_question` (`paper_id`,`question_id`,`del_flag`),
  KEY `idx_paper_id` (`paper_id`) USING BTREE,
  KEY `idx_question_id` (`question_id`) USING BTREE,
  KEY `idx_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2104965724539670531 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试卷-试题中间表';

-- ----------------------------
-- Records of paper_question
-- ----------------------------
BEGIN;
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104943975911608322, 2104943974758174722, 2104846742441881602, 5.00, 1, 0, 0, '1', '2026-09-29 22:38:46', '1', '2026-09-29 22:38:46', '103');
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104943976435896322, 2104943974758174722, 2104846994150453249, 5.00, 2, 0, 0, '1', '2026-09-29 22:38:47', '1', '2026-09-29 22:38:47', '103');
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104943976905658370, 2104943974758174722, 2104612232406966273, 5.00, 3, 0, 0, '1', '2026-09-29 22:38:47', '1', '2026-09-29 22:38:47', '103');
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104943977299922945, 2104943974758174722, 2104847655688663041, 5.00, 4, 0, 0, '1', '2026-09-29 22:38:47', '1', '2026-09-29 22:38:47', '103');
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104965723126190082, 2104965721985339394, 2104612232406966273, 5.00, 1, 0, 0, '1', '2026-09-30 00:05:11', '1', '2026-09-30 00:05:11', '103');
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104965723742752770, 2104965721985339394, 2104846742441881602, 5.00, 2, 0, 0, '1', '2026-09-30 00:05:12', '1', '2026-09-30 00:05:12', '103');
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104965724162183170, 2104965721985339394, 2104846994150453249, 5.00, 3, 0, 0, '1', '2026-09-30 00:05:12', '1', '2026-09-30 00:05:12', '103');
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `paper_score`, `sort`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104965724539670530, 2104965721985339394, 2104847655688663041, 5.00, 4, 0, 0, '1', '2026-09-30 00:05:12', '1', '2026-09-30 00:05:12', '103');
COMMIT;

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bank_id` bigint NOT NULL COMMENT '所属题库ID',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干富文本',
  `question_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题',
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'medium' COMMENT '难度 easy简单 medium中等 hard困难',
  `score` decimal(5,2) DEFAULT NULL COMMENT '题目默认分值',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '试题解析富文本',
  `answer` json DEFAULT NULL COMMENT '参考答案JSON，不同题型结构不同',
  `create_user` bigint NOT NULL COMMENT '题目创建人ID',
  `status` varchar(30) NOT NULL DEFAULT '1' COMMENT '0草稿 1启用 2废弃',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_bank_id` (`bank_id`) USING BTREE,
  KEY `idx_question_type` (`question_type`) USING BTREE,
  KEY `idx_create_user` (`create_user`) USING BTREE,
  KEY `idx_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105150442150469635 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试题主表';

-- ----------------------------
-- Records of question
-- ----------------------------
BEGIN;
INSERT INTO `question` (`id`, `bank_id`, `title`, `question_type`, `difficulty`, `score`, `analysis`, `answer`, `create_user`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104612232406966273, 2104768454784602114, '123123', 'SINGLE', 'easy', 5.00, '123123', '{\"rightKeys\": [\"B\"]}', 1, '1', 0, 0, '1', '2026-09-29 00:40:33', '1', '2026-09-29 11:14:04', '103');
INSERT INTO `question` (`id`, `bank_id`, `title`, `question_type`, `difficulty`, `score`, `analysis`, `answer`, `create_user`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104846742441881602, 2104768454784602114, 'asdfasdfa', 'SINGLE', 'easy', 5.00, 'asdfasdfa', '{\"rightKeys\": [\"A\"]}', 1, '0', 0, 0, '1', '2026-09-29 16:12:24', '1', '2026-09-29 17:02:22', '103');
INSERT INTO `question` (`id`, `bank_id`, `title`, `question_type`, `difficulty`, `score`, `analysis`, `answer`, `create_user`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104846994150453249, 2104768454784602114, 'asdfasdfa546545646', 'SINGLE', 'easy', 5.00, 'asdfasdfa', '{\"rightKeys\": [\"A\"]}', 1, '1', 0, 0, '1', '2026-09-29 16:13:24', '1', '2026-09-29 16:14:16', '103');
INSERT INTO `question` (`id`, `bank_id`, `title`, `question_type`, `difficulty`, `score`, `analysis`, `answer`, `create_user`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2104847655688663041, 2104765751853502466, '<p>asdfas<strong><em><u>dfasdfasdf1231231</u></em></strong></p>', 'JUDGE', 'easy', 5.00, '<p>1231</p>', '{\"rightKeys\": [\"A\"]}', 1, '1', 0, 0, '1', '2026-09-29 16:16:02', '1', '2026-09-29 17:01:45', '103');
INSERT INTO `question` (`id`, `bank_id`, `title`, `question_type`, `difficulty`, `score`, `analysis`, `answer`, `create_user`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105128355440594946, 2104765751853502466, '<p><img src=\"http://8.137.151.232:9000/ruoyi/2026/09/30/bf90701732404a4093f4d319c9d95304.jpeg\" width=\"480\" height=\"480\">asdasd</p>', 'SINGLE', 'easy', 5.00, '', '{\"rightKeys\": [\"D\"]}', 1, '1', 0, 0, '1', '2026-09-30 10:51:26', '1', '2026-09-30 10:51:26', '103');
INSERT INTO `question` (`id`, `bank_id`, `title`, `question_type`, `difficulty`, `score`, `analysis`, `answer`, `create_user`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`) VALUES (2105150442150469634, 2104765751853502466, '<p><img src=\"http://8.137.151.232:9000/ruoyi/2026/09/30/c1fc52a58d544639a35978295e7ab1d6.jpeg\" width=\"480\" height=\"480\">21321231213</p>', 'SINGLE', 'easy', 5.00, '', '{\"rightKeys\": [\"B\"]}', 1, '1', 0, 0, '1', '2026-09-30 12:19:12', '1', '2026-09-30 12:19:12', '103');
COMMIT;

-- ----------------------------
-- Table structure for question_bank
-- ----------------------------
DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bank_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题库名称',
  `bank_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '题库描述',
  `creator_id` bigint NOT NULL COMMENT '创建人用户ID',
  `visibility` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'private' COMMENT '可见性 private私有 / public公开',
  `status` varchar(20) NOT NULL DEFAULT '1' COMMENT '状态 0草稿 1正常 2归档',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID，单租户可不用',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  `category_id` bigint DEFAULT NULL COMMENT '所属分类ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`) USING BTREE,
  KEY `idx_visibility` (`visibility`) USING BTREE,
  KEY `idx_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2104794732917100547 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题库表';

-- ----------------------------
-- Records of question_bank
-- ----------------------------
BEGIN;
INSERT INTO `question_bank` (`id`, `bank_name`, `bank_desc`, `creator_id`, `visibility`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`, `category_id`) VALUES (2104500934286733314, '1', '1', 1, 'public', '1', 0, 1, '1', '2026-09-28 17:18:17', '1', '2026-09-29 11:01:27', '103', NULL);
INSERT INTO `question_bank` (`id`, `bank_name`, `bank_desc`, `creator_id`, `visibility`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`, `category_id`) VALUES (2104765751853502466, 'test', 'aaa', 1, 'private', 'draft', 0, 0, '1', '2026-09-29 10:50:35', '1', '2026-09-29 10:50:35', '103', NULL);
INSERT INTO `question_bank` (`id`, `bank_name`, `bank_desc`, `creator_id`, `visibility`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`, `category_id`) VALUES (2104768454784602114, 'aaa', '131', 1, 'private', 'draft', 0, 0, '1', '2026-09-29 11:01:19', '1', '2026-09-29 11:01:19', '103', NULL);
INSERT INTO `question_bank` (`id`, `bank_name`, `bank_desc`, `creator_id`, `visibility`, `status`, `tenant_id`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `create_dept`, `category_id`) VALUES (2104794732917100546, '123', '', 1, 'private', 'draft', 0, 0, '1', '2026-09-29 12:45:44', '1', '2026-09-29 12:45:44', '103', 2104794635667968002);
COMMIT;

-- ----------------------------
-- Table structure for question_bank_category
-- ----------------------------
DROP TABLE IF EXISTS `question_bank_category`;
CREATE TABLE `question_bank_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类id',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类id，0根节点',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `create_dept` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2104794846561767427 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库分类目录';

-- ----------------------------
-- Records of question_bank_category
-- ----------------------------
BEGIN;
INSERT INTO `question_bank_category` (`id`, `parent_id`, `category_name`, `sort`, `create_by`, `create_time`, `update_by`, `create_dept`, `update_time`, `is_deleted`, `tenant_id`) VALUES (2104794596736438274, 0, '测试分类', 0, '1', '2026-09-29 12:45:12', '1', '103', '2026-09-29 12:45:12', 0, 0);
INSERT INTO `question_bank_category` (`id`, `parent_id`, `category_name`, `sort`, `create_by`, `create_time`, `update_by`, `create_dept`, `update_time`, `is_deleted`, `tenant_id`) VALUES (2104794635667968002, 2104794596736438274, '测试分类2', 0, '1', '2026-09-29 12:45:21', '1', '103', '2026-09-29 12:45:21', 0, 0);
INSERT INTO `question_bank_category` (`id`, `parent_id`, `category_name`, `sort`, `create_by`, `create_time`, `update_by`, `create_dept`, `update_time`, `is_deleted`, `tenant_id`) VALUES (2104794846561767426, 0, '测试分类2', 0, '1', '2026-09-29 12:46:11', '1', '103', '2026-09-29 12:46:11', 0, 0);
COMMIT;

-- ----------------------------
-- Table structure for question_option
-- ----------------------------
DROP TABLE IF EXISTS `question_option`;
CREATE TABLE `question_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `option_key` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项标识 A/B/C/D',
  `option_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项内容富文本',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_question_id` (`question_id`) USING BTREE,
  KEY `idx_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105150443081605123 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试题选项表';

-- ----------------------------
-- Records of question_option
-- ----------------------------
BEGIN;
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104612234135019521, 2104612232406966273, 'A', '1', 1, 0, 0, '2026-09-29 00:40:33', '2026-09-29 00:40:33', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104612234806108161, 2104612232406966273, 'B', '2', 2, 0, 0, '2026-09-29 00:40:33', '2026-09-29 00:40:33', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104612235166818306, 2104612232406966273, 'C', '3', 3, 0, 0, '2026-09-29 00:40:33', '2026-09-29 00:40:33', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104612235510751234, 2104612232406966273, 'D', '4', 4, 0, 0, '2026-09-29 00:40:33', '2026-09-29 00:40:33', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846742555127810, 2104846742441881602, 'A', 'asdfasdfa', 1, 0, 1, '2026-09-29 16:12:24', '2026-09-29 16:12:46', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846742639013889, 2104846742441881602, 'B', 'asdfasdfa', 2, 0, 1, '2026-09-29 16:12:24', '2026-09-29 16:12:46', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846742760648705, 2104846742441881602, 'C', 'asdfasdfa', 3, 0, 1, '2026-09-29 16:12:24', '2026-09-29 16:12:46', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846742819368961, 2104846742441881602, 'D', 'asdfasdfa', 4, 0, 1, '2026-09-29 16:12:24', '2026-09-29 16:12:46', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846836054552578, 2104846742441881602, 'A', 'asdfasdfa', 1, 0, 1, '2026-09-29 16:12:47', '2026-09-29 17:02:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846836134244354, 2104846742441881602, 'B', 'asdfasdfa', 2, 0, 1, '2026-09-29 16:12:47', '2026-09-29 17:02:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846836213936130, 2104846742441881602, 'C', 'asdfasdfa', 3, 0, 1, '2026-09-29 16:12:47', '2026-09-29 17:02:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846836297822210, 2104846742441881602, 'D', 'asdfasdfa', 4, 0, 1, '2026-09-29 16:12:47', '2026-09-29 17:02:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846994305642497, 2104846994150453249, 'A', 'asdfasdfa', 1, 0, 1, '2026-09-29 16:13:24', '2026-09-29 16:14:15', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846994410500098, 2104846994150453249, 'B', 'asdfasdfa', 2, 0, 1, '2026-09-29 16:13:24', '2026-09-29 16:14:15', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846994477608961, 2104846994150453249, 'C', 'asdfasdfa', 3, 0, 1, '2026-09-29 16:13:24', '2026-09-29 16:14:15', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104846994532134913, 2104846994150453249, 'D', 'asdfasdfa', 4, 0, 1, '2026-09-29 16:13:24', '2026-09-29 16:14:15', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104847209616044034, 2104846994150453249, 'A', 'asdfasdfa', 1, 0, 0, '2026-09-29 16:14:16', '2026-09-29 16:14:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104847209687347201, 2104846994150453249, 'B', 'asdfasdfa', 2, 0, 0, '2026-09-29 16:14:16', '2026-09-29 16:14:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104847209741873153, 2104846994150453249, 'C', 'asdfasdfa', 3, 0, 0, '2026-09-29 16:14:16', '2026-09-29 16:14:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104847209850925057, 2104846994150453249, 'D', 'asdfasdfa', 4, 0, 0, '2026-09-29 16:14:16', '2026-09-29 16:14:16', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104847655822880769, 2104847655688663041, 'A', '<p>正确</p>', 1, 0, 1, '2026-09-29 16:16:02', '2026-09-29 17:01:45', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104847655885795329, 2104847655688663041, 'B', '<p>错误</p>', 2, 0, 1, '2026-09-29 16:16:02', '2026-09-29 17:01:45', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859161239150593, 2104847655688663041, 'A', '<p>正确</p>', 1, 0, 0, '2026-09-29 17:01:45', '2026-09-29 17:01:45', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859161293676546, 2104847655688663041, 'B', '<p>错误</p>', 2, 0, 0, '2026-09-29 17:01:45', '2026-09-29 17:01:45', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859291375820801, 2104846742441881602, 'A', '<p>asdfasdfa12312</p>', 1, 0, 1, '2026-09-29 17:02:16', '2026-09-29 17:02:21', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859291447123969, 2104846742441881602, 'B', '<p>asdfasdfa123</p>', 2, 0, 1, '2026-09-29 17:02:16', '2026-09-29 17:02:21', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859291505844225, 2104846742441881602, 'C', '<p>er234523423</p>', 3, 0, 1, '2026-09-29 17:02:16', '2026-09-29 17:02:21', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859291568758786, 2104846742441881602, 'D', '<p>asdfasdfa12312</p>', 4, 0, 1, '2026-09-29 17:02:16', '2026-09-29 17:02:21', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859315098804226, 2104846742441881602, 'A', '<p>asdfasdfa12312</p>', 1, 0, 0, '2026-09-29 17:02:22', '2026-09-29 17:02:22', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859315161718785, 2104846742441881602, 'B', '<p>asdfasdfa123</p>', 2, 0, 0, '2026-09-29 17:02:22', '2026-09-29 17:02:22', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859315228827650, 2104846742441881602, 'C', '<p>er234523423</p>', 3, 0, 0, '2026-09-29 17:02:22', '2026-09-29 17:02:22', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2104859315291742209, 2104846742441881602, 'D', '<p>asdfasdfa12312</p>', 4, 0, 0, '2026-09-29 17:02:22', '2026-09-29 17:02:22', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105128355562229762, 2105128355440594946, 'A', '<p>asd</p>', 1, 0, 0, '2026-09-30 10:51:26', '2026-09-30 10:51:26', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105128355633532929, 2105128355440594946, 'B', '<p>asd</p>', 2, 0, 0, '2026-09-30 10:51:26', '2026-09-30 10:51:26', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105128355717419010, 2105128355440594946, 'C', '<p>asd</p>', 3, 0, 0, '2026-09-30 10:51:26', '2026-09-30 10:51:26', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105128355797110785, 2105128355440594946, 'D', '<p>asd</p>', 4, 0, 0, '2026-09-30 10:51:26', '2026-09-30 10:51:26', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105150442817363970, 2105150442150469634, 'A', '<p>z</p>', 1, 0, 0, '2026-09-30 12:19:12', '2026-09-30 12:19:12', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105150442884472833, 2105150442150469634, 'B', '<p>zz</p>', 2, 0, 0, '2026-09-30 12:19:12', '2026-09-30 12:19:12', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105150442951581697, 2105150442150469634, 'C', '<p>z</p>', 3, 0, 0, '2026-09-30 12:19:12', '2026-09-30 12:19:12', '103', '1', '1');
INSERT INTO `question_option` (`id`, `question_id`, `option_key`, `option_content`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105150443081605122, 2105150442150469634, 'D', '<p>z</p>', 4, 0, 0, '2026-09-30 12:19:12', '2026-09-30 12:19:12', '103', '1', '1');
COMMIT;

-- ----------------------------
-- Table structure for wrong_question
-- ----------------------------
DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '系统用户ID（访客visitor不做错题集，错题集一般只给登录用户）',
  `question_id` bigint NOT NULL COMMENT '题目ID，关联question表',
  `source_type` varchar(20) NOT NULL COMMENT '来源：EXAM正式考试 / PAPER_PRACTICE试卷练习',
  `source_id` bigint NOT NULL COMMENT '来源ID：exam_id 或者 paper_id',
  `answer_item_id` bigint DEFAULT NULL COMMENT '首次答错对应的小题作答记录id（仅溯源）',
  `wrong_count` int NOT NULL DEFAULT '1' COMMENT '错误次数',
  `right_count` int NOT NULL DEFAULT '0' COMMENT '答对次数（复习做对累加）',
  `master_status` varchar(20) NOT NULL DEFAULT 'NOT_MASTER' COMMENT '掌握状态：NOT_MASTER未掌握 / MASTERED已掌握 / IGNORED忽略（用户移除错题）',
  `user_note` text COMMENT '用户笔记，富文本，可写错题心得',
  `last_wrong_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次答错时间',
  `last_review_time` datetime DEFAULT NULL COMMENT '最近一次复习时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '软删除，用户删除错题',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_question` (`user_id`,`question_id`) COMMENT '唯一约束：同一个用户同一题仅一条记录',
  KEY `idx_user` (`user_id`),
  KEY `idx_master_status` (`user_id`,`master_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户错题集主表';

-- ----------------------------
-- Records of wrong_question
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wrong_review_record
-- ----------------------------
DROP TABLE IF EXISTS `wrong_review_record`;
CREATE TABLE `wrong_review_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_wrong_id` bigint NOT NULL COMMENT '错题记录ID',
  `user_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `user_answer` text COMMENT '用户复习答案',
  `is_correct` tinyint(1) NOT NULL COMMENT '是否答对',
  `review_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_wrong` (`user_wrong_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题复习作答记录';

-- ----------------------------
-- Records of wrong_review_record
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
