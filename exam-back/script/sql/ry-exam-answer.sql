/*
 Navicat Premium Data Transfer

 Source Server         : dcLocalhost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : ry-exam-answer

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 02/10/2026 17:30:30
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
) ENGINE=InnoDB AUTO_INCREMENT=2105695881810391043 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试逐题作答表';

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
) ENGINE=InnoDB AUTO_INCREMENT=2105693453522284546 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试答卷记录表';

SET FOREIGN_KEY_CHECKS = 1;
