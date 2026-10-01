/*
 Navicat Premium Data Transfer

 Source Server         : 8.137.151.232
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : 8.137.151.232:3306
 Source Schema         : ry-exam-answer

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 30/09/2026 20:30:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exam_answer
-- ----------------------------
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '答卷记录ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `question_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '题型，冗余便于统计',
  `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '考生作答JSON：客观题{choices:[A]}或{blanks:[..]}，主观题{text:..}',
  `score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '本题得分',
  `correct` tinyint NOT NULL DEFAULT '0' COMMENT '0未判 1正确 2错误',
  `sort` int NOT NULL DEFAULT '0' COMMENT '题号顺序',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_record_question` (`record_id`,`question_id`,`del_flag`) USING BTREE,
  KEY `idx_record_id` (`record_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105193536568872962 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试逐题作答表';

-- ----------------------------
-- Records of exam_answer
-- ----------------------------
BEGIN;
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105177158101188609, 2105174659168419841, 2104846742441881602, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 1, '000000', 0, '2026-09-30 14:05:21', '2026-09-30 14:16:24', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105177168796663810, 2105174659168419841, 2104846994150453249, NULL, '{\"choices\":[\"B\"]}', 0.00, 2, 2, '000000', 0, '2026-09-30 14:05:24', '2026-09-30 14:16:24', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105177180905619458, 2105174659168419841, 2104612232406966273, NULL, '{\"choices\":[\"B\"]}', 5.00, 1, 3, '000000', 0, '2026-09-30 14:05:27', '2026-09-30 14:16:24', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105177294005026817, 2105174659168419841, 2104847655688663041, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 4, '000000', 0, '2026-09-30 14:05:54', '2026-09-30 14:16:24', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105182812949913602, 2105180092260098050, 2104846742441881602, NULL, '{\"choices\":[\"B\"]}', 0.00, 2, 1, '000000', 0, '2026-09-30 14:27:50', '2026-09-30 14:28:00', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105182817899192321, 2105180092260098050, 2104846994150453249, NULL, '{\"choices\":[\"C\"]}', 0.00, 2, 2, '000000', 0, '2026-09-30 14:27:51', '2026-09-30 14:28:00', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105182825872564226, 2105180092260098050, 2104612232406966273, NULL, '{\"choices\":[\"C\"]}', 0.00, 2, 3, '000000', 0, '2026-09-30 14:27:53', '2026-09-30 14:28:00', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105182833468448770, 2105180092260098050, 2104847655688663041, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 4, '000000', 0, '2026-09-30 14:27:55', '2026-09-30 14:28:00', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105192985093394434, 2105192964121874433, 2104846742441881602, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 1, '000000', 0, '2026-09-30 15:08:15', '2026-09-30 15:08:23', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105192993532334082, 2105192964121874433, 2104846994150453249, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 2, '000000', 0, '2026-09-30 15:08:17', '2026-09-30 15:08:23', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105193003175038978, 2105192964121874433, 2104612232406966273, NULL, '{\"choices\":[\"A\"]}', 0.00, 2, 3, '000000', 0, '2026-09-30 15:08:19', '2026-09-30 15:08:23', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105193007730053121, 2105192964121874433, 2104847655688663041, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 4, '000000', 0, '2026-09-30 15:08:20', '2026-09-30 15:08:23', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105193518122323970, 2105193504088182786, 2104846742441881602, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 1, '000000', 0, '2026-09-30 15:10:22', '2026-09-30 15:10:28', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105193523960795138, 2105193504088182786, 2104846994150453249, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 2, '000000', 0, '2026-09-30 15:10:23', '2026-09-30 15:10:28', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105193529006542850, 2105193504088182786, 2104612232406966273, NULL, '{\"choices\":[\"A\"]}', 0.00, 2, 3, '000000', 0, '2026-09-30 15:10:25', '2026-09-30 15:10:28', '103', '1', '1');
INSERT INTO `exam_answer` (`id`, `record_id`, `question_id`, `question_type`, `answer_content`, `score`, `correct`, `sort`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105193536568872961, 2105193504088182786, 2104847655688663041, NULL, '{\"choices\":[\"A\"]}', 5.00, 1, 4, '000000', 0, '2026-09-30 15:10:26', '2026-09-30 15:10:28', '103', '1', '1');
COMMIT;

-- ----------------------------
-- Table structure for exam_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `paper_id` bigint NOT NULL COMMENT '试卷ID（开考时快照，试卷后续改动不影响已开考的答卷）',
  `user_id` bigint DEFAULT NULL COMMENT '考生用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考生账号（登录名）',
  `attempt_no` int NOT NULL DEFAULT '1' COMMENT '第几次参加，从1开始',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'answering' COMMENT 'answering答题中 / submitted已交卷 / expired超时作废',
  `start_time` datetime DEFAULT NULL COMMENT '开考时间',
  `submit_time` datetime DEFAULT NULL COMMENT '交卷时间',
  `used_seconds` int NOT NULL DEFAULT '0' COMMENT '用时（秒）',
  `duration_minutes` bigint NOT NULL DEFAULT '0' COMMENT '本场限时（分钟），0表示不限时',
  `question_count` int NOT NULL DEFAULT '0' COMMENT '题目总数',
  `answered_count` int NOT NULL DEFAULT '0' COMMENT '已作答题目数',
  `objective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '客观题得分（自动判分）',
  `subjective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '主观题得分（人工阅卷后回填）',
  `total_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总分',
  `pass_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '及格分（开考时取自试卷）',
  `passed` tinyint NOT NULL DEFAULT '0' COMMENT '是否及格 0否 1是',
  `auto_submit` tinyint NOT NULL DEFAULT '0' COMMENT '是否超时系统自动交卷 0否 1是',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_account_attempt` (`exam_id`,`account`,`attempt_no`,`del_flag`) USING BTREE,
  KEY `idx_account` (`account`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105193504088182787 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试答卷记录表';

-- ----------------------------
-- Records of exam_record
-- ----------------------------
BEGIN;
INSERT INTO `exam_record` (`id`, `exam_id`, `paper_id`, `user_id`, `account`, `attempt_no`, `status`, `start_time`, `submit_time`, `used_seconds`, `duration_minutes`, `question_count`, `answered_count`, `objective_score`, `subjective_score`, `total_score`, `pass_score`, `passed`, `auto_submit`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105174659168419841, 2105174045227184129, 2104943974758174722, 1, 'admin', 1, 'submitted', '2026-09-30 13:55:24', '2026-09-30 14:16:25', 1260, 888, 4, 4, 15.00, 0.00, 15.00, 4.00, 1, 0, '000000', 0, '2026-09-30 13:55:26', '2026-09-30 14:16:25', '103', '1', '1');
INSERT INTO `exam_record` (`id`, `exam_id`, `paper_id`, `user_id`, `account`, `attempt_no`, `status`, `start_time`, `submit_time`, `used_seconds`, `duration_minutes`, `question_count`, `answered_count`, `objective_score`, `subjective_score`, `total_score`, `pass_score`, `passed`, `auto_submit`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105180092260098050, 2105174045227184129, 2104943974758174722, 1, 'admin', 2, 'submitted', '2026-09-30 14:17:01', '2026-09-30 14:28:00', 659, 888, 4, 4, 5.00, 0.00, 5.00, 4.00, 1, 0, '000000', 0, '2026-09-30 14:17:01', '2026-09-30 14:28:00', '103', '1', '1');
INSERT INTO `exam_record` (`id`, `exam_id`, `paper_id`, `user_id`, `account`, `attempt_no`, `status`, `start_time`, `submit_time`, `used_seconds`, `duration_minutes`, `question_count`, `answered_count`, `objective_score`, `subjective_score`, `total_score`, `pass_score`, `passed`, `auto_submit`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105184710427877378, 2105174045227184129, 2104943974758174722, 1, 'admin', 3, 'submitted', '2026-09-30 14:35:22', '2026-09-30 14:35:26', 4, 888, 4, 0, 0.00, 0.00, 0.00, 4.00, 0, 0, '000000', 0, '2026-09-30 14:35:22', '2026-09-30 14:35:26', '103', '1', '1');
INSERT INTO `exam_record` (`id`, `exam_id`, `paper_id`, `user_id`, `account`, `attempt_no`, `status`, `start_time`, `submit_time`, `used_seconds`, `duration_minutes`, `question_count`, `answered_count`, `objective_score`, `subjective_score`, `total_score`, `pass_score`, `passed`, `auto_submit`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105192964121874433, 2105174045227184129, 2104943974758174722, 1, 'admin', 4, 'submitted', '2026-09-30 15:08:10', '2026-09-30 15:08:23', 12, 888, 4, 4, 15.00, 0.00, 15.00, 4.00, 1, 0, '000000', 0, '2026-09-30 15:08:10', '2026-09-30 15:08:23', '103', '1', '1');
INSERT INTO `exam_record` (`id`, `exam_id`, `paper_id`, `user_id`, `account`, `attempt_no`, `status`, `start_time`, `submit_time`, `used_seconds`, `duration_minutes`, `question_count`, `answered_count`, `objective_score`, `subjective_score`, `total_score`, `pass_score`, `passed`, `auto_submit`, `tenant_id`, `del_flag`, `create_time`, `update_time`, `create_dept`, `create_by`, `update_by`) VALUES (2105193504088182786, 2105193340510367745, 2104943974758174722, 1, 'admin', 1, 'submitted', '2026-09-30 15:10:18', '2026-09-30 15:10:28', 9, 1, 4, 4, 15.00, 0.00, 15.00, 4.00, 1, 0, '000000', 0, '2026-09-30 15:10:19', '2026-09-30 15:10:28', '103', '1', '1');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
