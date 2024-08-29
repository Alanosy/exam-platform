/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80023 (8.0.23)
 Source Host           : localhost:3306
 Source Schema         : db_exam

 Target Server Type    : MySQL
 Target Server Version : 80023 (8.0.23)
 File Encoding         : 65001

 Date: 29/08/2024 13:54:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_certificate
-- ----------------------------
DROP TABLE IF EXISTS `t_certificate`;
CREATE TABLE `t_certificate` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID   证书',
  `certificate_name` varchar(255) NOT NULL COMMENT '证数名称',
  `image` varchar(255) DEFAULT NULL COMMENT '背景图片',
  `certification_nuit` varchar(50) NOT NULL COMMENT '认证单位',
  `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate
-- ----------------------------
BEGIN;
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (65, '13245', NULL, '32423', '2024-06-24 21:11:45', 0);
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (66, '1', NULL, '1', '2024-06-22 07:28:17', 0);
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (67, '111', NULL, '1111', '2024-06-22 14:34:27', 0);
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (68, '1111', NULL, '1111', '2024-06-24 15:16:52', 0);
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (69, 'mm', NULL, 'mm', '2024-06-24 21:50:28', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_certificate_user
-- ----------------------------
DROP TABLE IF EXISTS `t_certificate_user`;
CREATE TABLE `t_certificate_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID   证书与用户关系表',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `exam_id` int DEFAULT NULL COMMENT '考试id',
  `code` varchar(255) DEFAULT NULL COMMENT '证书编号',
  `certificate_id` int DEFAULT NULL COMMENT '证书id',
  `create_time` datetime DEFAULT NULL COMMENT '获奖时间      YYYY-MM-DD hh:mm:ss',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate_user
-- ----------------------------
BEGIN;
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (10, 118, 92, 'rkC5SOS5F7rfo5vVbu', 66, '2024-06-24 16:23:14');
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (11, 118, 95, '0K8bcurD6iBZmbA0oB', 65, '2024-06-24 19:31:08');
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (12, 118, 93, 'p1aeWYm2dTYPKsznWP', 68, '2024-07-16 15:52:51');
COMMIT;

-- ----------------------------
-- Table structure for t_exam
-- ----------------------------
DROP TABLE IF EXISTS `t_exam`;
CREATE TABLE `t_exam` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id    考试表',
  `title` varchar(255) NOT NULL COMMENT '考试名称',
  `exam_duration` int NOT NULL COMMENT '考试时长',
  `passed_score` int NOT NULL COMMENT '及格分',
  `gross_score` int NOT NULL COMMENT '总分数',
  `max_count` int DEFAULT NULL COMMENT '最大切屏次数',
  `user_id` int DEFAULT NULL COMMENT '创建者id',
  `certificate_id` int DEFAULT NULL COMMENT '证书id',
  `radio_count` int DEFAULT NULL COMMENT '单选题数量',
  `radio_score` int DEFAULT NULL COMMENT '单选题成绩     数据库存储*100，前端正常输入和展示/100',
  `multi_count` int DEFAULT NULL COMMENT '多选题数量 ',
  `multi_score` int DEFAULT NULL COMMENT '多选题成绩     数据库存储*100，前端正常输入和展示/100',
  `judge_count` int DEFAULT NULL COMMENT '判断题数量 ',
  `judge_score` int DEFAULT NULL COMMENT '判断题成绩     数据库存储*100，前端正常输入和展示/100',
  `saq_count` int DEFAULT NULL COMMENT '简答题数量',
  `saq_score` int DEFAULT NULL COMMENT '简答题成绩     数据库存储*100，前端正常输入和展示/100',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间     YYYY-MM-DD hh:mm:ss',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间     YYYY-MM-DD hh:mm:ss',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间     YYYY-MM-DD hh:mm:ss  ',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`,`passed_score`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam
-- ----------------------------
BEGIN;
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (86, 'TEST', 12, 100, 150, 2, 1, NULL, 3, 30, 3, 10, 2, 10, 2, 5, '2024-06-18 16:00:00', '2024-06-19 16:00:00', '2024-06-19 10:59:18', 1);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (87, '考试收到v发表过', 20, 40, 60, 2, 1, 65, 6, 10, 0, 0, 0, 0, 0, 0, '2024-06-22 16:00:00', '2024-06-23 16:00:00', '2024-06-23 15:38:03', 1);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (88, '考试ksks', 20, 40, 60, 10, 1, 65, 6, 10, 0, 4, 0, 0, 0, 0, '2024-06-22 16:00:00', '2024-06-24 16:00:00', '2024-06-23 21:15:56', 1);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (89, '考试考试考试', 15, 40, 60, 20, 1, 65, 6, 10, 0, 4, 0, 0, 0, 0, '2024-06-22 16:00:00', '2024-06-24 16:00:00', '2024-06-23 21:41:38', 1);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (90, '考试考试', 15, 3, 35, 5, 1, NULL, 5, 5, 0, 0, 0, 0, 1, 10, '2024-06-23 16:00:00', '2024-06-25 16:00:00', '2024-06-24 15:58:19', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (91, '测试抗生素 人', 20, 1, 5, 3, 1, 66, 1, 5, 0, 0, 0, 0, 1, 0, '2024-06-22 16:00:00', '2024-07-30 16:00:00', '2024-06-24 16:00:24', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (92, '语文测试', 20, 3, 20, 5, 1, 66, 2, 5, 0, 0, 0, 0, 1, 10, '2024-06-16 16:00:00', '2024-07-25 16:00:00', '2024-06-24 16:18:52', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (93, '数学测试', 30, 3, 10, 6, 1, 68, 0, 0, 0, 0, 0, 0, 1, 10, '2024-06-16 16:00:00', '2024-07-30 16:00:00', '2024-06-24 16:20:28', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (94, '考试试卷', 30, 20, 55, 10, 1, NULL, 5, 5, 0, 0, 0, 0, 3, 10, '2024-06-17 16:00:00', '2024-07-30 16:00:00', '2024-06-24 16:35:09', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (95, '4如果他保护你', 60, 3, 35, 5, 1, 65, 5, 3, 0, 0, 0, 0, 2, 10, '2024-06-23 16:00:00', '2024-06-27 16:00:00', '2024-06-24 18:54:17', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (96, '工行建行', 60, 5, 25, 3, 1, 65, 5, 5, 0, 0, 0, 0, 0, 0, '2024-06-23 16:00:00', '2024-06-30 16:00:00', '2024-06-24 19:16:27', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (97, '数学考试', 30, 20, 43, 0, 1, 66, 4, 2, 1, 5, 0, 0, 3, 10, '2024-06-03 16:00:00', '2024-07-23 16:00:00', '2024-06-24 21:49:33', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (98, 'sdssvsv', 120, 60, 110, 1, 1, 65, 8, 5, 5, 5, 4, 5, 5, 5, '2024-06-26 18:00:00', '2024-06-26 19:00:00', '2024-06-26 00:37:39', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (99, '天气考试', 5, 5, 10, 1, 1, 65, 0, 0, 0, 0, 0, 0, 1, 10, '2024-06-27 16:00:00', '2024-06-27 18:00:00', '2024-06-27 14:52:10', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (100, '天气考试', 5, 5, 10, 2, 1, 65, 0, 0, 0, 0, 0, 0, 1, 10, '2024-06-27 07:01:00', '2024-06-27 16:00:00', '2024-06-27 14:57:58', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (101, '天气执行考试', 20, 5, 10, 2, 1, 65, 0, 0, 0, 0, 0, 0, 1, 10, '2024-06-27 07:10:00', '2024-06-27 15:10:00', '2024-06-27 15:04:02', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (102, '测试考试', 15, 6, 10, 2, 132, 65, 0, 0, 0, 0, 0, 0, 1, 10, '2024-06-27 09:00:00', '2024-06-28 15:00:00', '2024-06-27 16:27:34', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (103, '风格和v你', 544, 100, 100, 3, 132, 65, 0, 0, 0, 0, 0, 0, 1, 100, '2024-06-20 16:00:00', '2024-07-30 16:00:00', '2024-06-28 16:04:15', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (104, '1大苏打撒旦', 60, 0, 80, 1, 1, 69, 2, 10, 2, 10, 2, 10, 2, 10, '2024-07-05 23:00:00', '2024-07-10 00:00:00', '2024-07-06 07:26:10', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (105, '我企鹅我去', 30, 4, 15, 1, 1, NULL, 2, 3, 2, 2, 2, 1, 3, 1, '2024-07-15 16:00:00', '2024-08-27 16:00:00', '2024-07-12 16:50:04', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (106, '20240716', 2, 10, 23, 1, 1, 65, 2, 2, 2, 2, 3, 3, 3, 2, '2024-07-15 16:00:00', '2024-08-28 16:00:00', '2024-07-16 15:54:47', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_grade`;
CREATE TABLE `t_exam_grade` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id   考试与班级关系表',
  `exam_id` int DEFAULT NULL COMMENT '考试id  唯一',
  `grade_id` int DEFAULT NULL COMMENT '班级id  唯一',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=191 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (156, 86, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (157, 87, 108);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (158, 87, 109);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (159, 87, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (160, 88, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (161, 89, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (162, 90, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (163, 91, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (164, 92, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (165, 93, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (166, 94, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (167, 95, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (168, 96, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (169, 97, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (170, 98, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (171, 99, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (172, 100, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (173, 100, 108);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (174, 100, 109);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (175, 100, 110);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (176, 101, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (177, 101, 108);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (178, 101, 109);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (179, 101, 110);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (180, 102, 112);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (181, 103, 112);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (182, 104, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (183, 105, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (184, 106, 111);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (185, 106, 110);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (186, 106, 109);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (187, 106, 108);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (188, 106, 107);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (189, 106, 114);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (190, 106, 113);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_qu_answer
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_qu_answer`;
CREATE TABLE `t_exam_qu_answer` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id  考试记录答案',
  `user_id` int NOT NULL COMMENT '用户id',
  `exam_id` int NOT NULL COMMENT '考试id',
  `question_id` int NOT NULL COMMENT '试题id',
  `question_type` int DEFAULT NULL COMMENT '题目类型',
  `answer_id` varchar(255) DEFAULT NULL COMMENT '答案id  用于客观题，多选题id使用“，”分隔',
  `answer_content` varchar(255) DEFAULT NULL COMMENT '答案内容    用于主观题',
  `checkout` int DEFAULT NULL COMMENT '是否选中   0未选中  1选中',
  `is_sign` int DEFAULT NULL COMMENT '是否标记   0未标记  1标记',
  `is_right` int DEFAULT NULL COMMENT '是否正确   用于客观题，0错误 1正确',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`,`question_id`)
) ENGINE=InnoDB AUTO_INCREMENT=276 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_qu_answer
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (219, 117, 86, 620, 1, '2367', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (220, 117, 88, 635, 1, '2419', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (221, 117, 88, 631, 1, '2403', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (222, 117, 88, 630, 1, '2399', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (223, 117, 88, 633, 1, '2413', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (224, 117, 88, 629, 1, '2397', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (225, 117, 88, 632, 1, '2408', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (226, 118, 87, 629, 1, '2395', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (227, 118, 87, 630, 1, '2401', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (228, 118, 87, 633, 1, '2412', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (229, 118, 87, 632, 1, '2407', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (230, 118, 87, 635, 1, '2419', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (231, 118, 87, 631, 1, '2404', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (232, 118, 88, 635, 1, '2420', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (233, 118, 88, 631, 1, '2404', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (234, 118, 88, 630, 1, '2400', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (235, 118, 88, 633, 1, '2411', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (236, 118, 88, 629, 1, '2395', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (237, 118, 88, 632, 1, '2406', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (238, 118, 90, 630, 1, '2401', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (239, 118, 90, 629, 1, '2395', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (240, 118, 90, 631, 1, '2403', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (241, 118, 90, 632, 1, '2408', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (242, 118, 90, 633, 1, '2411', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (243, 118, 90, 660, 4, NULL, '123', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (244, 118, 91, 631, 1, '2403', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (245, 118, 91, 660, 4, NULL, '12', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (246, 118, 92, 631, 1, '2403', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (247, 118, 92, 632, 1, '2408', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (248, 118, 92, 660, 4, NULL, '123', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (249, 118, 93, 661, 4, NULL, '3', NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (250, 118, 94, 630, 1, '2399', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (251, 118, 94, 633, 1, '2411', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (252, 118, 94, 632, 1, '2408', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (253, 118, 94, 631, 1, '2404', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (254, 118, 94, 629, 1, '2397', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (255, 118, 94, 660, 4, NULL, '12', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (256, 118, 94, 662, 4, NULL, '212', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (257, 118, 94, 663, 4, NULL, '222', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (258, 118, 95, 630, 1, '2399', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (259, 118, 95, 632, 1, '2407', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (260, 118, 95, 633, 1, '2411', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (261, 118, 95, 629, 1, '2395', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (262, 118, 95, 631, 1, '2405', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (263, 118, 95, 662, 4, NULL, '1', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (264, 118, 95, 660, 4, NULL, '1', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (265, 118, 97, 632, 1, '2406', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (266, 118, 97, 631, 1, '2403', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (267, 118, 97, 630, 1, '2400', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (268, 118, 97, 633, 1, '2412', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (269, 118, 97, 629, 2, '2397,2396', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (270, 118, 97, 662, 4, NULL, 'sf', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (271, 118, 97, 663, 4, NULL, 'er', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (272, 118, 97, 660, 4, NULL, 'were', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (273, 118, 98, 689, 1, '2572', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (274, 131, 102, 698, 4, NULL, '测试', NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (275, 131, 103, 698, 4, NULL, '测试', NULL, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_question
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_question`;
CREATE TABLE `t_exam_question` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id  考试题目表',
  `exam_id` int NOT NULL COMMENT '考试id',
  `question_id` int NOT NULL COMMENT '题目id',
  `score` int NOT NULL COMMENT '分值',
  `sort` int DEFAULT NULL COMMENT '排序',
  `type` int DEFAULT NULL COMMENT '试题类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=499 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_question
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (376, 85, 617, 1, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (377, 86, 618, 30, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (378, 86, 620, 30, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (379, 86, 621, 30, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (380, 86, 619, 10, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (381, 86, 622, 10, 4, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (382, 86, 623, 10, 5, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (383, 86, 624, 10, 6, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (384, 86, 625, 10, 7, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (385, 86, 626, 5, 9, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (386, 86, 627, 5, 8, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (387, 87, 629, 10, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (388, 87, 630, 10, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (389, 87, 631, 10, 5, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (390, 87, 632, 10, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (391, 87, 633, 10, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (392, 87, 635, 10, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (393, 88, 629, 10, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (394, 88, 630, 10, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (395, 88, 631, 10, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (396, 88, 632, 10, 5, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (397, 88, 633, 10, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (398, 88, 635, 10, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (399, 89, 629, 10, 5, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (400, 89, 630, 10, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (401, 89, 631, 10, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (402, 89, 632, 10, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (403, 89, 633, 10, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (404, 89, 635, 10, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (405, 90, 629, 5, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (406, 90, 630, 5, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (407, 90, 631, 5, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (408, 90, 632, 5, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (409, 90, 633, 5, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (410, 90, 660, 10, 5, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (411, 91, 631, 5, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (412, 91, 660, 0, 1, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (413, 92, 631, 5, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (414, 92, 632, 5, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (415, 92, 660, 10, 2, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (416, 93, 661, 10, 0, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (417, 94, 629, 5, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (418, 94, 630, 5, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (419, 94, 631, 5, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (420, 94, 632, 5, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (421, 94, 633, 5, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (422, 94, 660, 10, 5, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (423, 94, 662, 10, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (424, 94, 663, 10, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (425, 95, 629, 3, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (426, 95, 630, 3, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (427, 95, 631, 3, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (428, 95, 632, 3, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (429, 95, 633, 3, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (430, 95, 660, 10, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (431, 95, 662, 10, 5, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (432, 96, 629, 5, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (433, 96, 630, 5, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (434, 96, 631, 5, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (435, 96, 632, 5, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (436, 96, 633, 5, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (437, 97, 630, 2, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (438, 97, 631, 2, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (439, 97, 632, 2, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (440, 97, 633, 2, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (441, 97, 629, 5, 4, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (442, 97, 660, 10, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (443, 97, 662, 10, 5, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (444, 97, 663, 10, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (445, 98, 689, 5, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (446, 98, 690, 5, 5, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (447, 98, 630, 5, 4, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (448, 98, 631, 5, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (449, 98, 632, 5, 3, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (450, 98, 633, 5, 2, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (451, 98, 681, 5, 6, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (452, 98, 682, 5, 7, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (453, 98, 691, 5, 10, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (454, 98, 692, 5, 9, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (455, 98, 629, 5, 12, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (456, 98, 683, 5, 8, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (457, 98, 684, 5, 11, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (458, 98, 693, 5, 13, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (459, 98, 694, 5, 15, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (460, 98, 685, 5, 14, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (461, 98, 686, 5, 16, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (462, 98, 688, 5, 21, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (463, 98, 660, 5, 17, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (464, 98, 695, 5, 18, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (465, 98, 663, 5, 19, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (466, 98, 696, 5, 20, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (467, 99, 697, 10, 0, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (468, 100, 697, 10, 0, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (469, 101, 697, 10, 0, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (470, 102, 698, 10, 0, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (471, 103, 698, 100, 0, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (472, 104, 672, 10, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (473, 104, 673, 10, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (474, 104, 674, 10, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (475, 104, 675, 10, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (476, 104, 676, 10, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (477, 104, 677, 10, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (478, 104, 661, 10, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (479, 104, 678, 10, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (480, 105, 672, 3, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (481, 105, 673, 3, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (482, 105, 674, 2, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (483, 105, 675, 2, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (484, 105, 676, 1, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (485, 105, 677, 1, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (486, 105, 661, 1, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (487, 105, 678, 1, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (488, 105, 679, 1, 8, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (489, 106, 672, 2, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (490, 106, 673, 2, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (491, 106, 674, 2, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (492, 106, 675, 2, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (493, 106, 676, 3, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (494, 106, 677, 3, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (495, 106, 701, 3, 6, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (496, 106, 661, 2, 8, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (497, 106, 678, 2, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (498, 106, 679, 2, 9, 4);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_repo
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_repo`;
CREATE TABLE `t_exam_repo` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id  考试与题库',
  `exam_id` int NOT NULL COMMENT '考试id  唯一',
  `repo_id` int DEFAULT NULL COMMENT '题库id  唯一',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (74, 86, 63);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (75, 87, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (76, 88, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (77, 89, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (78, 90, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (79, 91, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (80, 92, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (81, 93, 63);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (82, 94, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (83, 95, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (84, 96, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (85, 97, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (86, 98, 64);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (87, 99, 69);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (88, 100, 69);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (89, 101, 69);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (90, 102, 70);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (91, 103, 70);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (92, 104, 63);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (93, 105, 63);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (94, 106, 63);
COMMIT;

-- ----------------------------
-- Table structure for t_exercise_record
-- ----------------------------
DROP TABLE IF EXISTS `t_exercise_record`;
CREATE TABLE `t_exercise_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `repo_id` int NOT NULL COMMENT '题库id',
  `question_id` int NOT NULL COMMENT '试题id',
  `user_id` int NOT NULL COMMENT '用户id',
  `answer` varchar(255) DEFAULT NULL COMMENT '主观题答案',
  `question_type` int NOT NULL COMMENT '题目类型',
  `options` varchar(255) DEFAULT NULL COMMENT '客观题答案集合  用于客观题,多选题id使用","分隔',
  `is_right` int DEFAULT NULL COMMENT '客观题是否正确',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `repo_id` (`repo_id`,`question_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=160 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exercise_record
-- ----------------------------
BEGIN;
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (133, 64, 629, 1, '2395', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (134, 64, 630, 1, '2399', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (135, 64, 631, 1, '2403', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (136, 64, 632, 1, '2408', 1, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (137, 65, 634, 1, '2414', 1, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (138, 64, 629, 118, '2394', 1, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (139, 64, 630, 118, '2399', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (140, 64, 631, 118, '2403', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (141, 64, 632, 118, '2407', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (142, 64, 633, 118, '2410', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (143, 63, 661, 1, '3', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (144, 63, 661, 118, 'ds', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (145, 63, 672, 118, '2527', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (146, 63, 672, 1, '2525', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (147, 63, 674, 1, '2535,2533,2534,2536,2537', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (148, 63, 675, 1, '2541,2540', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (149, 63, 677, 1, '2544', 3, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (150, 63, 678, 1, '234234', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (151, 63, 676, 1, '2542', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (152, 63, 679, 1, '1', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (153, 63, 673, 1, '2529', 1, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (154, 69, 697, 130, 'dddddddd', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (155, 69, 697, 1, '一半', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (156, 69, 697, 134, '123', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (157, 70, 698, 134, '123', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (158, 64, 633, 1, '2412', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (159, 63, 701, 1, '2607', 3, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_grade`;
CREATE TABLE `t_grade` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id   班级表',
  `grade_name` varchar(255) NOT NULL COMMENT '班级名称',
  `user_id` int DEFAULT NULL COMMENT '创建人id',
  `code` varchar(255) NOT NULL COMMENT '班级口令',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `grade_name` (`grade_name`) USING BTREE,
  UNIQUE KEY `code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=117 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (107, '软件2202班', 1, 'BKCQlyU3W8nMSRetHf', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (108, '软件2201班', 1, 'DqV9XAekoBfKZUnJH6', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (109, '物联网2201班', 1, 'gw9E740dRrZcHaCTCJ', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (110, '大数据2201班', 1, 'MBfIMGCCmXEFxSe48F', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (111, '111111', 1, 'uMQ1l3TGpit0V8D6gH', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (112, '班级1', 132, 'VXtkuTT96LGK3f1yoP', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (113, '12', 1, 'nI4l6GYXUfAVvaN18F', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (114, 's', 1, 'GuuZlJNcsc2YrckBgL', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (115, '1', 1, 'bObdTlssHDiBeAIVs5', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (116, '123123', 1, '5Qod2Ca4Ilkh1GIfqE', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_grade_exercise
-- ----------------------------
DROP TABLE IF EXISTS `t_grade_exercise`;
CREATE TABLE `t_grade_exercise` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id  刷题表',
  `repo_id` int DEFAULT NULL COMMENT '题库id',
  `grade_id` int DEFAULT NULL COMMENT '班级id',
  `user_id` int DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_grade_exercise
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_manual_score
-- ----------------------------
DROP TABLE IF EXISTS `t_manual_score`;
CREATE TABLE `t_manual_score` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id   人工评分表',
  `exam_qu_answer_id` int DEFAULT NULL COMMENT '考试记录答案id',
  `user_id` int DEFAULT NULL COMMENT '批改人id',
  `score` int DEFAULT NULL COMMENT '得分',
  `create_time` datetime DEFAULT NULL COMMENT '批改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_manual_score
-- ----------------------------
BEGIN;
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (39, 243, 1, 9, '2024-06-24 16:14:36');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (40, 248, 1, 8, '2024-06-24 16:23:14');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (41, 264, 1, 1, '2024-06-24 19:31:08');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (42, 263, 1, 1, '2024-06-24 19:31:08');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (43, 249, 1, 6, '2024-07-16 15:52:51');
COMMIT;

-- ----------------------------
-- Table structure for t_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID   公告表',
  `title` varchar(255) NOT NULL COMMENT '公告标题',
  `image` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `content` text NOT NULL COMMENT '公告内容',
  `user_id` int DEFAULT NULL COMMENT '创建用户ID   唯一',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice
-- ----------------------------
BEGIN;
INSERT INTO `t_notice` (`id`, `title`, `image`, `content`, `user_id`, `create_time`, `is_deleted`) VALUES (89, '11', NULL, '<p>1111</p>', 1, '2024-06-23 21:25:42', 1);
INSERT INTO `t_notice` (`id`, `title`, `image`, `content`, `user_id`, `create_time`, `is_deleted`) VALUES (90, '不管对方', NULL, '<ul><li class=\"ql-align-center\">我地方VS下能发挥刚才v</li></ul>', 1, '2024-06-26 16:08:41', 0);
INSERT INTO `t_notice` (`id`, `title`, `image`, `content`, `user_id`, `create_time`, `is_deleted`) VALUES (91, '呃呃呃', NULL, '<p><span class=\"ql-size-small\">狒狒一号</span></p>', 1, '2024-07-10 09:35:38', 0);
INSERT INTO `t_notice` (`id`, `title`, `image`, `content`, `user_id`, `create_time`, `is_deleted`) VALUES (92, '测试', NULL, '<p>2</p>', 1, '2024-07-12 17:22:39', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_notice_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_grade`;
CREATE TABLE `t_notice_grade` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID   公告班级关联表',
  `notice_id` int DEFAULT NULL COMMENT '公告id',
  `grade_id` int DEFAULT NULL COMMENT '班级',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (96, 89, 107);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (97, 89, 108);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (98, 89, 109);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (99, 90, 107);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (100, 90, 108);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (101, 90, 109);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (102, 90, 110);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (103, 91, 107);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (104, 91, 108);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (105, 91, 109);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (106, 91, 110);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (107, 91, 111);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (108, 91, 113);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (109, 91, 114);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (110, 92, 107);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (111, 92, 108);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (112, 92, 109);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (113, 92, 110);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (114, 92, 111);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (115, 92, 113);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (116, 92, 114);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (117, 92, 115);
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (118, 92, 116);
COMMIT;

-- ----------------------------
-- Table structure for t_option
-- ----------------------------
DROP TABLE IF EXISTS `t_option`;
CREATE TABLE `t_option` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id   选项答案表',
  `qu_id` int NOT NULL COMMENT '试题id',
  `is_right` int DEFAULT NULL COMMENT '是否正确',
  `image` varchar(255) DEFAULT NULL COMMENT '图片地址   0错误 1正确',
  `content` varchar(255) NOT NULL COMMENT '选项内容',
  `sort` int DEFAULT NULL COMMENT '排序',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2608 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_option
-- ----------------------------
BEGIN;
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2359, 618, 0, NULL, '1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2360, 618, 1, NULL, '2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2361, 618, 0, NULL, '3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2362, 618, 0, NULL, '4', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2363, 619, 0, NULL, '1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2364, 619, 1, NULL, '2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2365, 619, 1, NULL, '3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2366, 619, 1, NULL, '4', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2367, 620, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2368, 620, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2369, 620, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2370, 620, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2371, 621, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2372, 621, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2373, 621, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2374, 621, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2375, 622, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2376, 622, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2377, 622, 1, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2378, 622, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2379, 622, 0, NULL, '选项5', 4, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2380, 623, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2381, 623, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2382, 623, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2383, 623, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2384, 624, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2385, 624, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2386, 625, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2387, 625, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2388, 626, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2389, 627, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2390, 628, 0, NULL, 'A.拒绝变更', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2391, 628, 0, NULL, 'B.进行变更', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2392, 628, 0, NULL, 'C.不理会', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2393, 628, 1, NULL, 'D.告诉客户变更带来的影响', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2394, 629, 1, NULL, '子项目不能再往下划分成更小的子项目', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2395, 629, 1, NULL, '划分子项目的目的是为了便于管理', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2396, 629, 0, NULL, '项目生命期的一个阶段是子项目', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2397, 629, 0, NULL, '子项目的划分便于发包给其他单位', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2398, 630, 0, NULL, '拥有主要顾客或项目发起人', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2399, 630, 0, NULL, '独特性', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2400, 630, 0, NULL, '通过渐进性协助实施的', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2401, 630, 1, NULL, '具有很小的确定性', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2402, 631, 1, NULL, '工具', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2403, 631, 0, NULL, '资源', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2404, 631, 0, NULL, '目标', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2405, 631, 0, NULL, '环境', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2406, 632, 0, NULL, '项目群', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2407, 632, 0, NULL, '组合', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2408, 632, 1, NULL, '项目', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2409, 632, 0, NULL, '过程', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2410, 633, 0, NULL, '引入新技术和建议过程更改时作费效分析', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2411, 633, 1, NULL, ' 软件工程过程小组(SEPG)', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2412, 633, 0, NULL, '前摄性识别过程的强项和弱项', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2413, 633, 0, NULL, '识别出那些采用最好软件工程实践的技术创新并推广到整个组织即组织经验教训的重用', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2414, 634, 1, NULL, '集成测试和调试', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2415, 634, 0, NULL, '集成开发和单元调试', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2416, 634, 0, NULL, '开发和调试', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2417, 634, 0, NULL, '调试和测试', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2418, 635, 1, NULL, '甲方和乙方的利益', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2419, 635, 0, NULL, '范围，时间，成本,质量', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2420, 635, 0, NULL, '明确的和未明确表达的需求', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2421, 635, 0, NULL, '有不同需求和期望的项目干系人', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2422, 636, 1, NULL, '正确', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2423, 636, 0, NULL, '错误', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2424, 637, 1, NULL, '', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2425, 637, 0, NULL, '', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2426, 637, 0, NULL, '', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2427, 637, 0, NULL, '', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2428, 638, 1, NULL, '', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2429, 638, 0, NULL, '', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2430, 638, 0, NULL, '', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2431, 638, 0, NULL, '', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2432, 639, 0, NULL, '', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2433, 639, 1, NULL, '', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2434, 639, 0, NULL, '', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2435, 639, 0, NULL, '', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2436, 640, 1, NULL, '', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2437, 640, 0, NULL, '', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2438, 640, 0, NULL, '', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2439, 640, 0, NULL, '', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2440, 641, 1, NULL, '', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2441, 641, 0, NULL, '', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2442, 641, 0, NULL, '', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2443, 641, 0, NULL, '', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2444, 642, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2445, 642, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2446, 642, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2447, 642, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2448, 643, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2449, 643, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2450, 643, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2451, 643, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2452, 644, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2453, 644, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2454, 644, 1, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2455, 644, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2456, 644, 0, NULL, '选项5', 4, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2457, 645, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2458, 645, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2459, 645, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2460, 645, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2461, 646, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2462, 646, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2463, 647, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2464, 647, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2465, 648, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2466, 649, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2467, 650, 1, NULL, '第三方c', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2468, 650, 0, NULL, 'c生成cc', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2469, 650, 0, NULL, '现场v', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2470, 650, 0, NULL, 'v成都', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2471, 651, 1, NULL, 'we\'f\'r', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2472, 651, 0, NULL, '', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2473, 651, 0, NULL, '', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2474, 651, 0, NULL, '', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2475, 652, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2476, 652, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2477, 652, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2478, 652, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2479, 653, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2480, 653, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2481, 653, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2482, 653, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2483, 654, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2484, 654, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2485, 654, 1, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2486, 654, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2487, 654, 0, NULL, '选项5', 4, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2488, 655, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2489, 655, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2490, 655, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2491, 655, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2492, 656, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2493, 656, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2494, 657, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2495, 657, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2496, 658, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2497, 659, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2498, 660, 1, NULL, '213', NULL, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2499, 661, 1, NULL, '3', NULL, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2500, 662, 1, NULL, '444', NULL, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2501, 663, 1, NULL, '666', NULL, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2502, 664, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2503, 664, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2504, 664, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2505, 664, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2506, 665, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2507, 665, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2508, 665, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2509, 665, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2510, 666, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2511, 666, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2512, 666, 1, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2513, 666, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2514, 666, 0, NULL, '选项5', 4, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2515, 667, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2516, 667, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2517, 667, 0, NULL, '选项3', 2, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2518, 667, 0, NULL, '选项3', 3, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2519, 668, 1, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2520, 668, 0, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2521, 669, 0, NULL, '选项1', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2522, 669, 1, NULL, '选项2', 1, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2523, 670, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2524, 671, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 1);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2525, 672, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2526, 672, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2527, 672, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2528, 672, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2529, 673, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2530, 673, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2531, 673, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2532, 673, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2533, 674, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2534, 674, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2535, 674, 1, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2536, 674, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2537, 674, 0, NULL, '选项5', 4, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2538, 675, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2539, 675, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2540, 675, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2541, 675, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2542, 676, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2543, 676, 0, NULL, '', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2544, 677, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2545, 677, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2546, 678, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2547, 679, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2548, 680, 1, NULL, 'asd', NULL, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2549, 681, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2550, 681, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2551, 681, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2552, 681, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2553, 682, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2554, 682, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2555, 682, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2556, 682, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2557, 683, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2558, 683, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2559, 683, 1, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2560, 683, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2561, 683, 0, NULL, '选项5', 4, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2562, 684, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2563, 684, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2564, 684, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2565, 684, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2566, 685, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2567, 685, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2568, 686, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2569, 686, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2570, 687, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2571, 688, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2572, 689, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2573, 689, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2574, 689, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2575, 689, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2576, 690, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2577, 690, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2578, 690, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2579, 690, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2580, 691, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2581, 691, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2582, 691, 1, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2583, 691, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2584, 691, 0, NULL, '选项5', 4, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2585, 692, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2586, 692, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2587, 692, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2588, 692, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2589, 693, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2590, 693, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2591, 694, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2592, 694, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2593, 695, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2594, 696, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2595, 697, 1, NULL, '正正，负负', NULL, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2596, 698, 1, NULL, '测试', NULL, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2597, 699, 0, NULL, '1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2598, 699, 1, NULL, '2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2599, 699, 0, NULL, '3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2600, 699, 0, NULL, '4', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2601, 700, 0, NULL, '', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2602, 700, 0, NULL, '', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2603, 700, 0, NULL, '', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2604, 700, 1, NULL, '', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2605, 700, 0, NULL, '', 4, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2606, 701, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2607, 701, 0, NULL, '错误我去3123 ', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_question
-- ----------------------------
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id   试题表',
  `qu_type` varchar(255) NOT NULL COMMENT '试题类型',
  `image` varchar(255) DEFAULT NULL COMMENT '试题图片',
  `content` varchar(255) NOT NULL COMMENT '题干',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `analysis` varchar(255) DEFAULT NULL COMMENT '题目分析',
  `repo_id` int NOT NULL COMMENT '题库id',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=702 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_question
-- ----------------------------
BEGIN;
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (618, '1', NULL, '1+1=', '2024-06-18 22:30:50', NULL, 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (619, '2', NULL, '111111', '2024-06-18 22:31:13', NULL, 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (620, '1', NULL, '测试单选题1', '2024-06-19 10:53:22', '测试单选1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (621, '1', NULL, '测试单选题2', '2024-06-19 10:53:22', '测试单选2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (622, '2', NULL, '测试多选题1', '2024-06-19 10:53:22', '测试多选1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (623, '2', NULL, '测试多选题2', '2024-06-19 10:53:22', '测试多选2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (624, '3', NULL, '测试判断题1', '2024-06-19 10:53:22', '测试判断1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (625, '3', NULL, '测试判断题2', '2024-06-19 10:53:22', '测试判断2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (626, '4', NULL, '测试简答题1', '2024-06-19 10:53:22', '测试简答1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (627, '4', NULL, '测试简答题2', '2024-06-19 10:53:22', '测试简答2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (628, '1', NULL, '项目接近结束的时候，如果客户希望对项目范围进行大的变更，项目经理应该如何做？()', '2024-06-19 15:01:23', NULL, 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (629, '2', '', '下列都是子项目的正确说法,除了( )', '2024-06-19 15:28:02', '2', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (630, '1', NULL, '下列( )不属于项目管理的特征。', '2024-06-19 15:30:58', NULL, 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (631, '1', NULL, '下列不属于项目管理基本要素的是( )', '2024-06-19 15:31:52', NULL, 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (632, '1', NULL, '( )是为了创造一个唯一的产品或提供一个唯一的服务而进行的临时性的努力。', '2024-06-19 15:39:33', NULL, 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (633, '1', NULL, '下列哪项不属于优化级的行为特征( )', '2024-06-19 15:41:39', NULL, 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (634, '1', NULL, '软件构造”指的是通过编码、验证、单元测试、（    ）的组合，详细地创建可工作的、有意义的软件。', '2024-06-19 15:42:52', NULL, 65, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (635, '1', NULL, '项目管理需', '2024-06-19 15:43:06', NULL, 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (636, '3', NULL, '1', '2024-06-23 21:18:04', '2', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (637, '1', NULL, '1', '2024-06-23 21:20:58', '2', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (638, '1', NULL, '1', '2024-06-23 21:21:43', '2', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (639, '1', NULL, '8', '2024-06-23 21:32:28', '9', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (640, '1', NULL, '5', '2024-06-23 21:36:07', '4', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (641, '1', NULL, '8', '2024-06-23 21:36:44', '5', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (642, '1', NULL, '测试单选题1', '2024-06-23 21:42:35', '测试单选1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (643, '1', NULL, '测试单选题2', '2024-06-23 21:42:35', '测试单选2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (644, '2', NULL, '测试多选题1', '2024-06-23 21:42:35', '测试多选1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (645, '2', NULL, '测试多选题2', '2024-06-23 21:42:35', '测试多选2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (646, '3', NULL, '测试判断题1', '2024-06-23 21:42:35', '测试判断1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (647, '3', NULL, '测试判断题2', '2024-06-23 21:42:35', '测试判断2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (648, '4', NULL, '测试简答题1', '2024-06-23 21:42:35', '测试简答1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (649, '4', NULL, '测试简答题2', '2024-06-23 21:42:35', '测试简答2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (650, '1', NULL, '阿斯蒂芬v', '2024-06-23 22:06:56', '斯蒂芬干哈', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (651, '1', NULL, '额外的夫人钢铁行业吧', '2024-06-24 14:11:57', '我的饭如果', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (652, '1', NULL, '测试单选题1', '2024-06-24 15:44:26', '测试单选1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (653, '1', NULL, '测试单选题2', '2024-06-24 15:44:26', '测试单选2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (654, '2', NULL, '测试多选题1', '2024-06-24 15:44:26', '测试多选1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (655, '2', NULL, '测试多选题2', '2024-06-24 15:44:26', '测试多选2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (656, '3', NULL, '测试判断题1', '2024-06-24 15:44:26', '测试判断1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (657, '3', NULL, '测试判断题2', '2024-06-24 15:44:26', '测试判断2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (658, '4', NULL, '测试简答题1', '2024-06-24 15:44:26', '测试简答1解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (659, '4', NULL, '测试简答题2', '2024-06-24 15:44:26', '测试简答2解析', 64, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (660, '4', NULL, '123', '2024-06-24 15:53:44', '12', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (661, '4', NULL, '1+2=？', '2024-06-24 16:19:47', '3', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (662, '4', NULL, '4566', '2024-06-24 16:33:49', '45446', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (663, '4', NULL, '555', '2024-06-24 16:34:07', '44', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (664, '1', NULL, '测试单选题1', '2024-06-24 20:09:54', '测试单选1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (665, '1', NULL, '测试单选题2', '2024-06-24 20:09:54', '测试单选2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (666, '2', NULL, '测试多选题1', '2024-06-24 20:09:54', '测试多选1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (667, '2', NULL, '测试多选题2', '2024-06-24 20:09:54', '测试多选2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (668, '3', NULL, '测试判断题1', '2024-06-24 20:09:54', '测试判断1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (669, '3', NULL, '测试判断题2', '2024-06-24 20:09:54', '测试判断2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (670, '4', NULL, '测试简答题1', '2024-06-24 20:09:54', '测试简答1解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (671, '4', NULL, '测试简答题2', '2024-06-24 20:09:54', '测试简答2解析', 63, 1, 1);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (672, '1', NULL, '测试单选题1', '2024-06-24 21:44:45', '测试单选1解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (673, '1', NULL, '测试单选题2', '2024-06-24 21:44:45', '测试单选2解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (674, '2', NULL, '测试多选题1', '2024-06-24 21:44:45', '测试多选1解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (675, '2', NULL, '测试多选题2', '2024-06-24 21:44:45', '测试多选2解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (676, '3', NULL, '测试判断题1', '2024-06-24 21:44:45', '测试判断1解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (677, '3', NULL, '测试判断题2', '2024-06-24 21:44:45', '测试判断2解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (678, '4', NULL, '测试简答题1', '2024-06-24 21:44:45', '测试简答1解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (679, '4', NULL, '测试简答题2', '2024-06-24 21:44:45', '测试简答2解析', 63, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (680, '4', NULL, 'sad', '2024-06-24 21:50:06', 'sad', 65, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (681, '1', NULL, '测试单选题1', '2024-06-24 21:50:18', '测试单选1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (682, '1', NULL, '测试单选题2', '2024-06-24 21:50:18', '测试单选2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (683, '2', NULL, '测试多选题1', '2024-06-24 21:50:18', '测试多选1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (684, '2', NULL, '测试多选题2', '2024-06-24 21:50:18', '测试多选2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (685, '3', NULL, '测试判断题1', '2024-06-24 21:50:18', '测试判断1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (686, '3', NULL, '测试判断题2', '2024-06-24 21:50:18', '测试判断2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (687, '4', NULL, '测试简答题1', '2024-06-24 21:50:18', '测试简答1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (688, '4', NULL, '测试简答题2', '2024-06-24 21:50:18', '测试简答2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (689, '1', NULL, '测试单选题1', '2024-06-24 21:57:59', '测试单选1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (690, '1', NULL, '测试单选题2', '2024-06-24 21:57:59', '测试单选2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (691, '2', NULL, '测试多选题1', '2024-06-24 21:57:59', '测试多选1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (692, '2', NULL, '测试多选题2', '2024-06-24 21:57:59', '测试多选2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (693, '3', NULL, '测试判断题1', '2024-06-24 21:57:59', '测试判断1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (694, '3', NULL, '测试判断题2', '2024-06-24 21:57:59', '测试判断2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (695, '4', NULL, '测试简答题1', '2024-06-24 21:57:59', '测试简答1解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (696, '4', NULL, '测试简答题2', '2024-06-24 21:57:59', '测试简答2解析', 64, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (697, '4', NULL, '天气与心情关系', '2024-06-27 14:48:19', '正正，负负', 69, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (698, '4', NULL, 'test是什么', '2024-06-27 16:25:50', '测试', 70, 132, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (699, '1', NULL, '软件工程', '2024-06-28 16:01:13', NULL, 70, 132, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (700, '1', NULL, 'kfjdt', '2024-07-09 16:24:54', NULL, 65, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (701, '3', NULL, '请问', '2024-07-16 10:59:17', NULL, 63, 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_repo
-- ----------------------------
DROP TABLE IF EXISTS `t_repo`;
CREATE TABLE `t_repo` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id   题库表',
  `user_id` int NOT NULL COMMENT '创建人id',
  `title` varchar(255) NOT NULL COMMENT '题库标题',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (63, 1, 'TYG', '2024-06-18 22:30:15', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (64, 1, '软件项目管理', '2024-06-19 15:26:43', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (65, 1, '软件构造', '2024-06-19 15:40:30', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (66, 1, '撒东方大厦', '2024-06-20 10:13:32', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (67, 1, '111', '2024-06-21 14:26:45', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (68, 1, 'mm', '2024-06-24 21:49:52', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (69, 1, '主观题库', '2024-06-27 14:44:58', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (70, 132, 'test题库', '2024-06-27 16:25:25', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (71, 1, '123', '2024-07-05 21:18:19', 1);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (72, 1, '111', '2024-07-06 07:14:30', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (73, 1, 'rrew', '2024-07-11 17:37:20', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID  角色表',
  `role_name` varchar(10) NOT NULL COMMENT '角色名称',
  `code` varchar(10) NOT NULL COMMENT '角色编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_role
-- ----------------------------
BEGIN;
INSERT INTO `t_role` (`id`, `role_name`, `code`) VALUES (1, '学生', 'student');
INSERT INTO `t_role` (`id`, `role_name`, `code`) VALUES (2, '教师', 'teacher');
INSERT INTO `t_role` (`id`, `role_name`, `code`) VALUES (3, '管理员', 'admin');
COMMIT;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_name` varchar(255) NOT NULL COMMENT '用户名',
  `real_name` varchar(20) DEFAULT NULL COMMENT '真实姓名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) DEFAULT 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg' COMMENT '头像地址',
  `role_id` int DEFAULT '1' COMMENT '角色id',
  `grade_id` int DEFAULT NULL COMMENT '班级id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间   YYYY-MM-DD hh:mm:ss',
  `status` int DEFAULT '1' COMMENT '状态  1正常0禁用',
  `is_deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_name` (`user_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
BEGIN;
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (1, 'admin', '管理员', '$2a$10$/ZdKFY15AWNLOeTqAp91a.uDa0JDioj1wVYGgpn.HKMYh9vq0Uh4S', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/27eb3a59-4d05-4bce-90ec-4a2457452886.png', 3, 106, '2024-05-23 16:06:07', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (113, '18888888888', '18888888888', '$2a$10$YFyZsE0u5SPwU.6tNnWU0OTFiWHHqDnrx8/0ZRUahnf4BorSu.0hO', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-19 10:58:12', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (114, 'shengming@im-a.top', 'shengming@im-a.top', '$2a$10$V1t.ZRM9jTyhKPerEmnYkuAc0VtQv.qhk4Af7.462FuQM7GMUavWa', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-19 11:04:50', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (115, '111111', '111111', '$2a$10$i/snkDzT9N/B/LljJcaL4uvOFSTlYenS60Pi38fFKbJy.FvyKcPAS', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-19 21:06:52', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (116, '123', '147', '$2a$10$/nQTwR1SY5uji7B.BLpRK.2vfagXtrhwmRYKngVcHLuM0JhLHyKXG', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-23 15:14:41', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (117, 'nana', 'nana', '$2a$10$o3BU/Qbq3ZvdYMQj5teUN..6T4wHk38cFn5mStyEQCOAI18f3cbki', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 107, '2024-06-23 15:46:09', 1, 1);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (118, 'li', 'li', '$2a$10$kD23VtsrLyA9lEQMniv2Iu5Jt.4enRLUvthpIP96wO/fvyFHt4zKC', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 107, '2024-06-24 00:29:44', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (119, 'teacher', '张三', '$2a$10$llTzoTN3NpH.C9dXXkv9m.FK3IyYpAJEEWUyzkkcVCIayUKRCT8QG', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-06-24 01:42:01', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (121, '321123', '321123', '$2a$10$9sQ8olUnajU/HPcHbHkKsexhtI05osVYqIBdRKq36tWMYDeAAGLK6', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-24 15:33:01', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (122, '1', '1', '$2a$10$7j5lOirb.HrqMJ.fAuVbjes6.Dlu2luGz4jfmmqrnMUq3RznsqgtK', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-24 20:42:26', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (123, '2', '2', '$2a$10$3LNY5WYZ07bjgAGeUZ67L.cQ1ehEzP9h8zpu/j829oGZMeiDblfMK', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-24 20:44:33', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (124, '22', '22', '$2a$10$zP59JqQO6gXKa18r1HCzuufRRo8fSRnebPWEXLdzLmbBZI4dxyGf6', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-06-24 20:49:33', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (125, '55', '555', '$2a$10$pVA.6m1yfpMYkYAv5iIpb.ec/RtoDPPR3eCWpaDHqaGqR/xWqT5uK', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-06-24 21:10:02', 1, 1);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (126, '44', '14', '$2a$10$3YO3cAOfVfMq71uvGJrSNeRBB8gIj96x2P/pQsk4DiSLrdumAuJTa', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-06-24 21:43:33', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (127, '45', '54', '$2a$10$rY89zzhDaXqB5zBqHpIZieGBwUIjTZ6W3OaeOHrNOQsNPh9M6.Veu', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-06-24 21:48:22', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (128, '21201619', '123', '$2a$10$CQ5BK3PXwl8fMuwwoOI5K.kpBPVOxNGwT55gzhOnOxzFE1XAX8YOa', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-26 14:48:34', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (129, '123456', '123456', '$2a$10$fbxB0tK5joqXokz7I0rVwe/POazVG.z2LENFJtfYnBkO7E9mfSkpi', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-27 10:19:16', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (130, 'test', 'test', '$2a$10$0hJNKP1GF8q0yHWAX.7i9uPol9tztu7IZOgNN12tXv3FZeMREs3zq', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-27 14:59:31', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (131, 'dev', 'dev', '$2a$10$Rs/AkiZ.GK34ZDa9EafxkOx7QffFhgerLqll3qgXnd2OyHcZ4vN1C', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 112, '2024-06-27 15:01:12', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (132, 'devs', 'devs', '$2a$10$NfMgx4.I7K7c8Q1HggG/s.dWxb05cMX81zVCMWrqVDF2ZLwcX/KL6', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-06-27 15:08:43', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (134, 'test1', 'test1', '$2a$10$dt.ron22PdtWw0DSpA6ry.oHsb2HfJV6LAwg69RytlviogATAN62W', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-27 16:29:20', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (135, 'zhangsan', '张三', '$2a$10$UL2BnPK3yh7.xGi5D.XNkexECEM.UzgZCeZT/LJvdY7fydNol3GnW', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-30 18:15:30', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (136, 'buxingyu19@gmail.com', '123', '$2a$10$zl3JNWeEEUVji5j4fUpR6Oq7VzYSLyzUupZlB3oT5s8iks7IIHS.y', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-07-03 16:17:08', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (137, 'qqqqqq', 'qqqqqq', '$2a$10$7df21P8fwvv9dZGqh19XbOHk/C47yJM249I7nTpNOA9S0lAhvjhby', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-07-04 11:23:52', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (138, '万辉', '万辉', '$2a$10$RKxX30G26XESbfxsVgm1zuL2ITmndPv4t/XodcRoU0vP2VUOcJRmG', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-07-05 15:56:50', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (140, '3213', '4fds', '$2a$10$QLlopmC/8LeO2UwqhdET.Osq/qgeMdIFl/TfukUZQ7ExUWQ0h9AAS', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-07-07 14:25:46', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (143, '123ee', '123ee', '$2a$10$.wLk1qw3lXC8UBeCeeX4EeoXtCSTWnYm/xNgbxyKIcF0Qfb3H4pjW', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-07-12 09:45:14', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (144, 'yanglaoshi', 'yanglaoshi', '$2a$10$Kh6HIp/FBKBlGyBbj6bagOfJ/xEV752wo59wMNfzHRkt30aQth.gS', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-07-12 16:45:03', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (145, '123123', '123123', '$2a$10$UrSnd/GLt0cvFW8MU9o0fexQglFZ8eNcRKWODfbIQUbc0bQhZTUGi', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-07-12 16:46:34', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (146, '阿是范德萨发', '111', '$2a$10$Jny8PjSPGci1ODXdvev7teQaah8fcw6OqQsH..a2tbPuNosEejCAq', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-07-16 17:15:19', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_user_book
-- ----------------------------
DROP TABLE IF EXISTS `t_user_book`;
CREATE TABLE `t_user_book` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID   错题本',
  `exam_id` int DEFAULT NULL COMMENT '考试id  唯一',
  `user_id` int DEFAULT NULL COMMENT '用户id    唯一',
  `qu_id` int DEFAULT NULL COMMENT '题目id    唯一',
  `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间    YYYY-MM-DD hh:mm:ss',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_book
-- ----------------------------
BEGIN;
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (171, 88, 117, 635, '2024-06-23 21:39:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (172, 88, 117, 631, '2024-06-23 21:39:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (173, 88, 117, 630, '2024-06-23 21:39:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (174, 88, 117, 633, '2024-06-23 21:39:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (175, 88, 117, 629, '2024-06-23 21:39:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (176, 88, 118, 629, '2024-06-24 01:48:02');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (177, 88, 118, 630, '2024-06-24 01:48:02');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (178, 88, 118, 631, '2024-06-24 01:48:02');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (179, 88, 118, 632, '2024-06-24 01:48:02');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (180, 88, 118, 635, '2024-06-24 01:48:02');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (181, 90, 118, 629, '2024-06-24 15:59:37');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (182, 90, 118, 631, '2024-06-24 15:59:37');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (183, 90, 118, 660, '2024-06-24 15:59:37');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (184, 91, 118, 631, '2024-06-24 16:02:20');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (185, 91, 118, 660, '2024-06-24 16:02:20');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (186, 92, 118, 631, '2024-06-24 16:21:10');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (187, 92, 118, 660, '2024-06-24 16:21:10');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (188, 94, 118, 629, '2024-06-24 16:35:52');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (189, 94, 118, 630, '2024-06-24 16:35:52');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (190, 94, 118, 631, '2024-06-24 16:35:52');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (191, 94, 118, 660, '2024-06-24 16:35:52');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (192, 94, 118, 662, '2024-06-24 16:35:52');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (193, 94, 118, 663, '2024-06-24 16:35:52');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (194, 95, 118, 629, '2024-06-24 18:55:11');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (195, 95, 118, 630, '2024-06-24 18:55:11');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (196, 95, 118, 631, '2024-06-24 18:55:11');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (197, 95, 118, 632, '2024-06-24 18:55:11');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (198, 95, 118, 660, '2024-06-24 18:55:11');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (199, 95, 118, 662, '2024-06-24 18:55:11');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (200, 97, 118, 629, '2024-06-24 21:52:26');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (201, 97, 118, 630, '2024-06-24 21:52:26');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (202, 97, 118, 631, '2024-06-24 21:52:26');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (203, 97, 118, 632, '2024-06-24 21:52:26');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (204, 97, 118, 633, '2024-06-24 21:52:26');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (205, 97, 118, 660, '2024-06-24 21:52:26');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (206, 97, 118, 662, '2024-06-24 21:52:26');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (207, 97, 118, 663, '2024-06-24 21:52:26');
COMMIT;

-- ----------------------------
-- Table structure for t_user_daily_login_duration
-- ----------------------------
DROP TABLE IF EXISTS `t_user_daily_login_duration`;
CREATE TABLE `t_user_daily_login_duration` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键，自增长',
  `user_id` int DEFAULT NULL COMMENT '用户ID，与用户表关联，确保数据一致性',
  `login_date` date DEFAULT NULL COMMENT '登录日期，记录用户在哪一天的登录时长',
  `total_seconds` int DEFAULT NULL COMMENT '累积在线秒数，每日登录总时长，单位为秒',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_user_daily_login_duration
-- ----------------------------
BEGIN;
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (62, 1, '2024-06-18', 901);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (63, 1, '2024-06-19', 20828);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (64, 114, '2024-06-19', 299);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (65, 115, '2024-06-19', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (66, 1, '2024-06-20', 48836);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (67, 1, '2024-06-21', 38709);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (68, 1, '2024-06-22', 48529);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (69, 1, '2024-06-23', 30165);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (70, 117, '2024-06-23', 1706);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (71, 1, '2024-06-24', 84559);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (72, 118, '2024-06-24', 32417);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (73, 119, '2024-06-24', 841);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (74, 1, '2024-06-25', 44580);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (75, 1, '2024-06-26', 24287);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (76, 118, '2024-06-26', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (77, 128, '2024-06-26', 300);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (78, 1, '2024-06-27', 31998);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (79, 131, '2024-06-27', 4600);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (80, 132, '2024-06-27', 260);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (81, 1, '2024-06-28', 14133);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (82, 134, '2024-06-28', 443);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (83, 132, '2024-06-28', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (84, 131, '2024-06-28', 93);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (85, 1, '2024-06-29', 18158);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (86, 1, '2024-06-30', 21624);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (87, 135, '2024-06-30', 5710);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (88, 132, '2024-06-30', 3688);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (89, 1, '2024-07-01', 38160);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (90, 1, '2024-07-02', 18597);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (91, 135, '2024-07-02', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (92, 118, '2024-07-02', 1799);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (93, 1, '2024-07-03', 42023);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (94, 1, '2024-07-04', 12286);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (95, 137, '2024-07-04', 685);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (96, 1, '2024-07-05', 31303);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (97, 1, '2024-07-06', 17360);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (98, 1, '2024-07-07', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (99, 1, '2024-07-08', 5995);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (100, 1, '2024-07-09', 7198);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (101, 1, '2024-07-10', 19348);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (102, 1, '2024-07-11', 29945);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (103, 1, '2024-07-12', 27563);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (104, 1, '2024-07-13', 7179);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (105, 1, '2024-07-15', 21524);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (106, 1, '2024-07-16', 38440);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (107, 146, '2024-07-16', 3296);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (108, 1, '2024-07-17', 29785);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (109, 1, '2024-07-18', 42773);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (110, 1, '2024-07-19', 27543);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (111, 1, '2024-07-20', 16736);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (112, 1, '2024-07-21', 25024);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (113, 1, '2024-07-22', 2365417);
COMMIT;

-- ----------------------------
-- Table structure for t_user_exams_score
-- ----------------------------
DROP TABLE IF EXISTS `t_user_exams_score`;
CREATE TABLE `t_user_exams_score` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id  用户考试成绩表',
  `user_id` int DEFAULT NULL COMMENT '用户id  唯一',
  `exam_id` int DEFAULT NULL COMMENT '试卷id  唯一',
  `total_time` int DEFAULT NULL COMMENT '总时长  YYYY-MM-DD hh:mm:ss',
  `user_time` int DEFAULT NULL COMMENT '用户用时  YYYY-MM-DD hh:mm:ss',
  `user_score` int unsigned DEFAULT '0' COMMENT '用户得分',
  `limit_time` datetime DEFAULT NULL COMMENT '交卷时间  YYYY-MM-DD hh:mm:ss',
  `count` int DEFAULT '0' COMMENT '切屏次数',
  `state` int DEFAULT NULL COMMENT '状态   0正在考试 1考试完成',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间  YYYY-MM-DD hh:mm:ss',
  `whether_mark` int DEFAULT NULL COMMENT '是否阅卷-1无简答题、0未阅卷、1阅卷',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`)
) ENGINE=InnoDB AUTO_INCREMENT=194 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exams_score
-- ----------------------------
BEGIN;
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (175, 117, 87, 20, NULL, 0, NULL, 2, 0, '2024-06-23 15:50:22', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (176, 117, 86, 12, NULL, 0, NULL, 2, 0, '2024-06-23 21:05:33', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (177, 117, 88, 20, 1422, 10, '2024-06-23 21:39:54', 9, 1, '2024-06-23 21:16:12', -1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (178, 117, 89, 15, NULL, 0, NULL, 3, 0, '2024-06-23 21:41:49', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (179, 118, 86, 12, NULL, 0, NULL, 2, 0, '2024-06-24 00:30:49', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (180, 118, 87, 20, NULL, 0, NULL, 2, 0, '2024-06-24 01:38:57', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (181, 118, 88, 20, 15, 10, '2024-06-24 01:48:02', 1, 1, '2024-06-24 01:47:46', -1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (182, 118, 89, 15, NULL, 0, NULL, 1, 0, '2024-06-24 15:50:23', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (183, 118, 90, 15, 44, 24, '2024-06-24 15:59:37', 2, 1, '2024-06-24 15:58:53', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (184, 118, 91, 20, 78, 0, '2024-06-24 16:02:20', 0, 1, '2024-06-24 16:01:01', 0);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (185, 118, 92, 20, 24, 13, '2024-06-24 16:21:10', 0, 1, '2024-06-24 16:20:46', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (186, 118, 93, 30, 33, 6, '2024-06-24 16:21:49', 0, 1, '2024-06-24 16:21:16', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (187, 118, 94, 30, 23, 10, '2024-06-24 16:35:52', 0, 1, '2024-06-24 16:35:29', 0);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (188, 118, 95, 60, 20, 5, '2024-06-24 18:55:11', 0, 1, '2024-06-24 18:54:50', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (189, 118, 96, 60, 102, 0, '2024-06-24 19:18:29', 3, 1, '2024-06-24 19:16:47', -1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (190, 118, 97, 30, 21, 0, '2024-06-24 21:52:26', 0, 1, '2024-06-24 21:52:05', 0);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (191, 118, 98, 120, NULL, 0, NULL, 1, 0, '2024-06-26 00:38:28', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (192, 131, 102, 15, NULL, 0, NULL, 0, 0, '2024-06-28 16:06:55', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (193, 131, 103, 544, 13, 0, '2024-06-28 16:08:16', 0, 1, '2024-06-28 16:08:03', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_user_exercise_record
-- ----------------------------
DROP TABLE IF EXISTS `t_user_exercise_record`;
CREATE TABLE `t_user_exercise_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id   用户刷题记录表',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `repo_id` int DEFAULT NULL COMMENT '题库id',
  `total_count` int DEFAULT NULL COMMENT '总题数',
  `exercise_count` int DEFAULT NULL COMMENT '已刷题数',
  `create_time` datetime DEFAULT NULL COMMENT '刷题时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exercise_record
-- ----------------------------
BEGIN;
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (17, 1, 63, 0, 14, '2024-06-19 10:56:03');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (18, 114, 63, 0, 5, '2024-06-19 11:05:32');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (19, 1, 64, 0, 5, '2024-06-19 15:28:09');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (20, 1, 65, 1, 1, '2024-06-19 15:43:17');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (21, 118, 64, 0, 5, '2024-06-24 01:10:08');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (22, 118, 63, 0, 2, '2024-06-24 21:46:48');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (23, 130, 69, 1, 1, '2024-06-27 15:00:24');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (24, 1, 69, 1, 1, '2024-06-27 23:24:00');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (25, 134, 69, 1, 1, '2024-06-28 15:56:21');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (26, 134, 70, 1, 1, '2024-06-28 15:57:42');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
