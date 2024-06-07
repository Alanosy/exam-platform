/*
 Navicat Premium Data Transfer

 Source Server         : Aliyun
 Source Server Type    : MySQL
 Source Server Version : 50740 (5.7.40-log)
 Source Host           : 47.109.94.143:3306
 Source Schema         : db_exam

 Target Server Type    : MySQL
 Target Server Version : 50740 (5.7.40-log)
 File Encoding         : 65001

 Date: 07/06/2024 15:03:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_certificate
-- ----------------------------
DROP TABLE IF EXISTS `t_certificate`;
CREATE TABLE `t_certificate` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   证书',
  `certificate_name` varchar(255) NOT NULL COMMENT '证数名称',
  `image` varchar(255) DEFAULT NULL COMMENT '背景图片',
  `certification_nuit` varchar(50) NOT NULL COMMENT '认证单位',
  `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate
-- ----------------------------
BEGIN;
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (55, 'y', NULL, 'y', '2024-06-07 00:25:01', 1);
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (58, '综合项目实战', NULL, '成都工业职业技术学院', '2024-06-03 14:39:30', 0);
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (59, '222', NULL, '111', '2024-06-06 14:51:17', 0);
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (60, 'q', NULL, 'q', '2024-06-06 15:18:12', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_certificate_user
-- ----------------------------
DROP TABLE IF EXISTS `t_certificate_user`;
CREATE TABLE `t_certificate_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   证书与用户关系表',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `exam_id` int(11) DEFAULT NULL COMMENT '考试id',
  `code` varchar(255) DEFAULT NULL COMMENT '证书编号',
  `certificate_id` int(11) DEFAULT NULL COMMENT '证书id',
  `create_time` datetime DEFAULT NULL COMMENT '获奖时间      YYYY-MM-DD hh:mm:ss',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate_user
-- ----------------------------
BEGIN;
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (2, 1, 71, 'dsfasdfasdfas', 55, '2024-05-30 15:48:07');
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (3, 58, 77, 'ZSfWPxhR3Oz8rVQbnN', 55, '2024-06-03 09:37:06');
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (4, 58, 71, 'ubaxTTiUdSXqq6Kfld', 55, '2024-06-03 10:45:36');
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (5, 58, 79, '5zBeQgsbh59zyQdU8R', 55, '2024-06-03 14:19:21');
COMMIT;

-- ----------------------------
-- Table structure for t_exam
-- ----------------------------
DROP TABLE IF EXISTS `t_exam`;
CREATE TABLE `t_exam` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id    考试表',
  `title` varchar(255) NOT NULL COMMENT '考试名称',
  `exam_duration` int(11) NOT NULL COMMENT '考试时长',
  `passed_score` int(11) NOT NULL COMMENT '及格分',
  `gross_score` int(11) NOT NULL COMMENT '总分数',
  `max_count` int(11) DEFAULT NULL COMMENT '最大切屏次数',
  `user_id` int(11) DEFAULT NULL COMMENT '创建者id',
  `certificate_id` int(11) DEFAULT NULL COMMENT '证书id',
  `radio_count` int(11) DEFAULT NULL COMMENT '单选题数量',
  `radio_score` int(11) DEFAULT NULL COMMENT '单选题成绩     数据库存储*100，前端正常输入和展示/100',
  `multi_count` int(11) DEFAULT NULL COMMENT '多选题数量 ',
  `multi_score` int(11) DEFAULT NULL COMMENT '多选题成绩     数据库存储*100，前端正常输入和展示/100',
  `judge_count` int(11) DEFAULT NULL COMMENT '判断题数量 ',
  `judge_score` int(11) DEFAULT NULL COMMENT '判断题成绩     数据库存储*100，前端正常输入和展示/100',
  `saq_count` int(11) DEFAULT NULL COMMENT '简答题数量',
  `saq_score` int(11) DEFAULT NULL COMMENT '简答题成绩     数据库存储*100，前端正常输入和展示/100',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间     YYYY-MM-DD hh:mm:ss',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间     YYYY-MM-DD hh:mm:ss',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间     YYYY-MM-DD hh:mm:ss  ',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`,`passed_score`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam
-- ----------------------------
BEGIN;
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (71, '综合项目实战考试', 60, 6, 36, 5, 1, 55, 2, 3, 2, 3, 2, 2, 2, 10, '2024-05-12 16:00:00', '2024-05-30 16:00:00', '2024-05-29 17:41:38', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (74, '测试考试', 60, 10, 28, 5, 1, NULL, 2, 1, 2, 2, 2, 1, 2, 10, '2024-05-29 16:00:00', '2024-06-12 16:00:00', '2024-05-30 22:50:41', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (75, '测试简答题考试', 60, 5, 10, 5, 1, NULL, 0, 0, 0, 0, 0, 0, 2, 5, '2024-05-29 16:00:00', '2024-06-24 16:00:00', '2024-05-30 22:56:30', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (76, '测试考试', 60, 5, 8, 5, 1, 55, 2, 1, 2, 1, 2, 1, 2, 1, '2024-06-11 16:00:00', '2024-07-30 16:00:00', '2024-06-01 19:58:15', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (77, '测试小娜用户考试', 60, 1, 6, 5, 1, 55, 2, 1, 2, 1, 2, 1, 0, 0, '2024-06-03 16:00:00', '2024-07-30 16:00:00', '2024-06-03 09:34:28', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (78, '演示考试', 60, 2, 6, 5, 1, 55, 2, 1, 2, 1, 2, 1, 0, 0, '2024-06-04 16:00:00', '2024-07-30 16:00:00', '2024-06-03 09:48:10', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (80, 'sdfad', 60, 5, 8, 5, 1, 55, 2, 1, 2, 1, 2, 1, 2, 1, '2024-06-24 16:00:00', '2024-07-29 16:00:00', '2024-06-06 14:25:30', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_grade`;
CREATE TABLE `t_exam_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   考试与班级关系表',
  `exam_id` int(11) DEFAULT NULL COMMENT '考试id  唯一',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级id  唯一',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=144 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (119, 71, 79);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (120, 71, 80);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (121, 71, 81);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (124, 74, 79);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (125, 75, 79);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (126, 76, 89);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (127, 76, 88);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (128, 76, 87);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (129, 76, 81);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (130, 76, 80);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (131, 76, 79);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (132, 77, 79);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (133, 77, 80);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (134, 77, 81);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (135, 78, 79);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (136, 78, 80);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (137, 78, 81);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (141, 80, 81);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (142, 80, 80);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (143, 80, 79);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_qu_answer
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_qu_answer`;
CREATE TABLE `t_exam_qu_answer` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id  考试记录答案',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `exam_id` int(11) NOT NULL COMMENT '考试id',
  `question_id` int(11) NOT NULL COMMENT '试题id',
  `question_type` int(11) DEFAULT NULL COMMENT '题目类型',
  `answer_id` varchar(255) DEFAULT NULL COMMENT '答案id  用于客观题，多选题id使用“，”分隔',
  `answer_content` varchar(255) DEFAULT NULL COMMENT '答案内容    用于主观题',
  `checkout` int(11) DEFAULT NULL COMMENT '是否选中   0未选中  1选中',
  `is_sign` int(11) DEFAULT NULL COMMENT '是否标记   0未标记  1标记',
  `is_right` int(11) DEFAULT NULL COMMENT '是否正确   用于客观题，0错误 1正确',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`,`question_id`)
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_qu_answer
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (110, 1, 71, 593, 1, '2283', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (111, 1, 71, 594, 1, '2289', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (112, 1, 71, 596, 2, '2297', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (113, 1, 71, 595, 2, '2291', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (114, 1, 71, 598, 3, '2302', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (115, 1, 71, 599, 4, NULL, '123', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (116, 58, 71, 593, 1, '2282', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (117, 58, 71, 594, 1, '2287', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (118, 58, 71, 596, 2, '2295,2296', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (119, 58, 71, 595, 2, '2291,2292,2293', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (120, 58, 71, 598, 3, '2301', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (121, 58, 71, 599, 4, NULL, '111111', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (122, 58, 71, 600, 4, NULL, '2222', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (123, 58, 74, 594, 1, '2286', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (124, 58, 74, 593, 1, '2284', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (125, 58, 74, 596, 2, '2295,2296', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (126, 58, 74, 595, 2, '2292,2293', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (127, 58, 74, 598, 3, '2301', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (128, 58, 74, 600, 4, NULL, '111111111', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (129, 1, 75, 599, 4, NULL, '1', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (130, 1, 75, 600, 4, NULL, '12', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (131, 58, 77, 594, 1, '2286', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (132, 58, 77, 593, 1, '2282', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (133, 58, 77, 595, 2, '2290,2291,2292', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (134, 58, 77, 596, 2, '2295,2296', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (135, 58, 77, 598, 3, '2302', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (136, 58, 77, 597, 3, '2299', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (137, 58, 79, 594, 1, '2286', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (138, 58, 79, 593, 1, '2283', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (139, 58, 79, 595, 2, '2291', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (140, 58, 79, 596, 2, '2297', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (141, 58, 79, 598, 3, '2302', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (142, 58, 79, 599, 4, NULL, 'abcd', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (143, 58, 79, 600, 4, NULL, 'abcd', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (144, 1, 80, 603, 1, '2314', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (145, 1, 80, 602, 1, '2310', NULL, NULL, NULL, 1);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (146, 1, 80, 605, 2, '2324', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (147, 1, 80, 604, 2, '2318', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (148, 1, 80, 597, 3, '2300', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (149, 1, 80, 606, 3, '2327', NULL, NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (150, 1, 80, 600, 4, NULL, 'jjnjnj', NULL, NULL, 0);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`) VALUES (151, 1, 80, 609, 4, NULL, 'jhjh', NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_question
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_question`;
CREATE TABLE `t_exam_question` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id  考试题目表',
  `exam_id` int(11) NOT NULL COMMENT '考试id',
  `question_id` int(11) NOT NULL COMMENT '题目id',
  `score` int(11) NOT NULL COMMENT '分值',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `type` int(11) DEFAULT NULL COMMENT '试题类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=343 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_question
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (289, 71, 593, 1, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (290, 71, 594, 1, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (291, 71, 595, 1, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (292, 71, 596, 1, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (293, 71, 597, 1, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (294, 71, 598, 1, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (295, 71, 599, 1, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (296, 71, 600, 1, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (297, 74, 593, 1, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (298, 74, 594, 1, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (299, 74, 595, 2, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (300, 74, 596, 2, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (301, 74, 597, 1, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (302, 74, 598, 1, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (303, 74, 599, 10, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (304, 74, 600, 10, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (305, 75, 599, 5, 0, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (306, 75, 600, 5, 1, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (307, 76, 593, 1, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (308, 76, 594, 1, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (309, 76, 595, 1, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (310, 76, 596, 1, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (311, 76, 597, 1, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (312, 76, 598, 1, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (313, 76, 599, 1, 6, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (314, 76, 600, 1, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (315, 77, 593, 1, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (316, 77, 594, 1, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (317, 77, 595, 1, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (318, 77, 596, 1, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (319, 77, 597, 1, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (320, 77, 598, 1, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (321, 78, 593, 1, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (322, 78, 594, 1, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (323, 78, 595, 1, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (324, 78, 596, 1, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (325, 78, 597, 1, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (326, 78, 598, 1, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (335, 80, 602, 1, 1, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (336, 80, 603, 1, 0, 1);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (337, 80, 604, 1, 3, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (338, 80, 605, 1, 2, 2);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (339, 80, 597, 1, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (340, 80, 606, 1, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (341, 80, 609, 1, 7, 4);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (342, 80, 600, 1, 6, 4);
COMMIT;

-- ----------------------------
-- Table structure for t_exam_repo
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_repo`;
CREATE TABLE `t_exam_repo` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id  考试与题库',
  `exam_id` int(11) NOT NULL COMMENT '考试id  唯一',
  `repo_id` int(11) DEFAULT NULL COMMENT '题库id  唯一',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (59, 71, 37);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (62, 74, 37);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (63, 75, 37);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (64, 76, 37);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (65, 77, 37);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (66, 78, 37);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (68, 80, 37);
COMMIT;

-- ----------------------------
-- Table structure for t_exercise_record
-- ----------------------------
DROP TABLE IF EXISTS `t_exercise_record`;
CREATE TABLE `t_exercise_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `repo_id` int(11) NOT NULL COMMENT '题库id',
  `question_id` int(11) NOT NULL COMMENT '试题id',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `answer` varchar(255) DEFAULT NULL COMMENT '主观题答案',
  `question_type` int(11) NOT NULL COMMENT '题目类型',
  `options` varchar(255) DEFAULT NULL COMMENT '客观题答案集合  用于客观题,多选题id使用","分隔',
  `is_right` int(11) DEFAULT NULL COMMENT '客观题是否正确',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `repo_id` (`repo_id`,`question_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=117 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exercise_record
-- ----------------------------
BEGIN;
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (55, 29, 588, 1, 'uuuuuu', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (56, 29, 578, 1, '2244', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (57, 29, 584, 1, 'kk', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (62, 38, 593, 58, '2283', 1, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (63, 38, 601, 58, '2306', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (64, 37, 594, 58, '2291,2292,2293', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (65, 37, 595, 58, '2293', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (66, 37, 596, 58, '2298', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (67, 37, 597, 58, '2299', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (68, 37, 598, 58, '2302', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (69, 37, 600, 58, '是', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (70, 37, 599, 58, '安然三个v给', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (71, 37, 593, 78, '2282', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (72, 37, 594, 78, '2287', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (73, 37, 595, 78, '2290', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (74, 37, 596, 78, '2295,2296,2297,2298', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (75, 37, 597, 78, '2299', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (76, 37, 598, 78, '2302', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (77, 37, 599, 78, 'ijlksdf ', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (78, 37, 600, 78, '受到法律框架啊离开的v家', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (79, 37, 602, 78, '2310', 1, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (80, 37, 603, 78, '2314', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (81, 37, 604, 78, '2319', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (82, 37, 605, 78, '2323', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (83, 37, 606, 78, '2327', 3, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (84, 37, 607, 78, '2329', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (85, 37, 608, 78, '是老地方见', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (86, 37, 609, 78, '答案', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (87, 38, 610, 80, '2332', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (88, 37, 597, 84, '2299', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (89, 37, 598, 84, '2302', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (90, 37, 599, 84, '111111111', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (91, 37, 600, 84, '11111111', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (92, 37, 602, 84, '2310', 1, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (93, 37, 603, 84, '2315', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (94, 37, 604, 84, '2320', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (95, 37, 605, 84, '2325', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (96, 37, 607, 84, '2329', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (97, 37, 608, 84, '111', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (98, 37, 609, 84, '111', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (99, 37, 597, 88, '2299', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (100, 37, 598, 88, '2302', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (101, 37, 602, 89, '2311', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (102, 37, 602, 1, '2299', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (103, 37, 604, 1, '2319,2321', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (104, 38, 601, 1, '2306', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (105, 38, 610, 1, '2333', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (106, 37, 597, 1, '2299', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (107, 37, 598, 1, '2302', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (108, 37, 599, 1, 'dfsdf', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (109, 37, 600, 1, 'erf', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (110, 37, 607, 1, '2329', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (111, 37, 605, 1, '2324,2325', 2, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (112, 37, 603, 1, '2316', 1, NULL, 0);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (113, 37, 608, 1, '111', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (114, 37, 606, 1, '2326', 3, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (115, 37, 609, 58, 's反对方的号码，就。', 4, NULL, 1);
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (116, 37, 611, 1, '2340', 1, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_grade`;
CREATE TABLE `t_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   班级表',
  `grade_name` varchar(255) NOT NULL COMMENT '班级名称',
  `user_id` int(11) DEFAULT NULL COMMENT '创建人id',
  `code` varchar(255) NOT NULL COMMENT '班级口令',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `grade_name` (`grade_name`) USING BTREE,
  UNIQUE KEY `code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (79, '软件2202', 1, 'U9KP3BIPGOjuXRWKEh', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (80, '软件2203', 1, '5qFU9SLbQGkJoh4QBW', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (81, '软件2204', 1, 'V8F0wBDmi7tPfRKp5X', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (82, '测试班级', 2, 'oYXFEzvyXIqPpDm6YA', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (87, '1', 1, 'GXzJkd0Or9z7nkawve', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (88, '12', 1, 'aI8RBFR3x5Sl1mAgqP', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (89, '123123123', 1, 'HS15YydUcHZTZ6Dd8N', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (91, '测试111', 2, 'eKs85BQhfpvZcvcoOk', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (92, '11', 1, 'RZ5cJLS5FBrWbb3pdy', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (95, '敏', 1, 'ovLwPg863y4lpSIwqd', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (97, '英语', 1, 'w5rw2t6xRVHaaVvF22', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (98, '看', 1, '9TGmpDr5yuvtffMQ36', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (99, '简介', 1, 'AXHmaaeD1Oosvu43WB', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (100, '了', 1, 'SFeh6AA6oC7rl7KMS4', 0);
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (101, '否', 1, 'vJ3WefWYRRJfMi81ql', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_grade_exercise
-- ----------------------------
DROP TABLE IF EXISTS `t_grade_exercise`;
CREATE TABLE `t_grade_exercise` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id  刷题表',
  `repo_id` int(11) DEFAULT NULL COMMENT '题库id',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级id',
  `user_id` int(11) DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

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
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   人工评分表',
  `exam_qu_answer_id` int(11) DEFAULT NULL COMMENT '考试记录答案id',
  `user_id` int(11) DEFAULT NULL COMMENT '批改人id',
  `score` int(11) DEFAULT NULL COMMENT '得分',
  `create_time` datetime DEFAULT NULL COMMENT '批改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_manual_score
-- ----------------------------
BEGIN;
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (19, 115, 1, 1, '2024-05-30 14:16:32');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (20, 128, 1, 8, '2024-05-30 22:55:19');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (21, 129, 1, 4, '2024-06-03 10:41:47');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (22, 130, 1, 4, '2024-06-03 10:41:47');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (23, 121, 1, 8, '2024-06-03 10:45:36');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (24, 122, 1, 7, '2024-06-03 10:45:36');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (25, 142, 1, 1, '2024-06-03 14:19:21');
INSERT INTO `t_manual_score` (`id`, `exam_qu_answer_id`, `user_id`, `score`, `create_time`) VALUES (26, 143, 1, 1, '2024-06-03 14:19:21');
COMMIT;

-- ----------------------------
-- Table structure for t_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   公告表',
  `title` varchar(255) NOT NULL COMMENT '公告标题',
  `image` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `content` text NOT NULL COMMENT '公告内容',
  `user_id` int(11) DEFAULT NULL COMMENT '创建用户ID   唯一',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice
-- ----------------------------
BEGIN;
INSERT INTO `t_notice` (`id`, `title`, `image`, `content`, `user_id`, `create_time`, `is_deleted`) VALUES (64, 'rgvt', NULL, '<p>ft5v</p>', 1, '2024-06-03 13:37:29', 0);
INSERT INTO `t_notice` (`id`, `title`, `image`, `content`, `user_id`, `create_time`, `is_deleted`) VALUES (65, '11', NULL, '<p>1</p>', 1, '2024-06-06 14:50:59', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_notice_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_grade`;
CREATE TABLE `t_notice_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   公告班级关联表',
  `notice_id` int(11) DEFAULT NULL COMMENT '公告id',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (1, 65, 1);
COMMIT;

-- ----------------------------
-- Table structure for t_option
-- ----------------------------
DROP TABLE IF EXISTS `t_option`;
CREATE TABLE `t_option` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   选项答案表',
  `qu_id` int(11) NOT NULL COMMENT '试题id',
  `is_right` int(11) DEFAULT NULL COMMENT '是否正确',
  `image` varchar(255) DEFAULT NULL COMMENT '图片地址   0错误 1正确',
  `content` varchar(255) NOT NULL COMMENT '选项内容',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2342 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_option
-- ----------------------------
BEGIN;
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2299, 597, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2300, 597, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2301, 598, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2302, 598, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2303, 599, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2304, 600, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2305, 601, 1, NULL, '20', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2306, 601, 1, 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/70d2332e-3cec-40d1-883d-2fd594112301.png', '19', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2307, 601, 1, NULL, '18', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2308, 601, 1, NULL, '21', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2309, 602, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2310, 602, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2311, 602, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2312, 602, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2313, 603, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2314, 603, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2315, 603, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2316, 603, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2317, 604, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2318, 604, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2319, 604, 1, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2320, 604, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2321, 604, 0, NULL, '选项5', 4, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2322, 605, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2323, 605, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2324, 605, 0, NULL, '选项3', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2325, 605, 0, NULL, '选项3', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2326, 606, 1, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2327, 606, 0, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2328, 607, 0, NULL, '选项1', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2329, 607, 1, NULL, '选项2', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2330, 608, 1, NULL, '1简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2331, 609, 1, NULL, '2简答题答案，简答题答案，简答题答案', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2332, 610, 0, NULL, '', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2333, 610, 0, NULL, '', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2334, 610, 0, NULL, '', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2335, 610, 0, NULL, '', 3, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2336, 610, 0, NULL, '', 4, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2337, 610, 1, NULL, '', 5, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2338, 611, 1, NULL, 'A', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2339, 611, 0, NULL, '', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2340, 611, 0, NULL, '', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2341, 611, 0, NULL, '', 3, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_question
-- ----------------------------
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   试题表',
  `qu_type` varchar(255) NOT NULL COMMENT '试题类型',
  `image` varchar(255) DEFAULT NULL COMMENT '试题图片',
  `content` varchar(255) NOT NULL COMMENT '题干',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `analysis` varchar(255) DEFAULT NULL COMMENT '题目分析',
  `repo_id` int(11) NOT NULL COMMENT '题库id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=612 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_question
-- ----------------------------
BEGIN;
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (597, '3', NULL, '测试判断题1', '2024-05-29 17:39:39', '测试判断1解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (598, '3', NULL, '测试判断题2', '2024-05-29 17:39:39', '测试判断2解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (599, '4', NULL, '测试简答题1', '2024-05-29 17:39:39', '测试简答1解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (600, '4', NULL, '测试简答题2', '2024-05-29 17:39:39', '测试简答2解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (601, '1', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/6935f3b5-69ad-472b-988b-6243e6a027c9.png', 'How are you?', '2024-05-30 13:56:45', '19', 38, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (602, '1', NULL, '测试单选题1', '2024-06-03 10:39:21', '测试单选1解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (603, '1', NULL, '测试单选题2', '2024-06-03 10:39:22', '测试单选2解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (604, '2', NULL, '测试多选题1', '2024-06-03 10:39:22', '测试多选1解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (605, '2', NULL, '测试多选题2', '2024-06-03 10:39:22', '测试多选2解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (606, '3', NULL, '测试判断题1', '2024-06-03 10:39:22', '测试判断1解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (607, '3', NULL, '测试判断题2', '2024-06-03 10:39:22', '测试判断2解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (608, '4', NULL, '测试简答题1', '2024-06-03 10:39:22', '测试简答1解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (609, '4', NULL, '测试简答题2', '2024-06-03 10:39:22', '测试简答2解析', 37, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (610, '1', NULL, '1', '2024-06-03 14:49:38', NULL, 38, 1, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (611, '1', NULL, '111', '2024-06-06 14:49:29', '111', 37, 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_repo
-- ----------------------------
DROP TABLE IF EXISTS `t_repo`;
CREATE TABLE `t_repo` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   题库表',
  `user_id` int(11) NOT NULL COMMENT '创建人id',
  `title` varchar(255) NOT NULL COMMENT '题库标题',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (37, 1, '综合项目实站', '2024-05-29 17:38:19', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (38, 1, '英语四级题库', '2024-05-30 13:55:01', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (39, 2, '英语考试', '2024-06-03 10:05:33', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (40, 2, '发电方式', '2024-06-03 10:05:54', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (41, 2, '三部分', '2024-06-03 10:06:10', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (42, 2, '此模', '2024-06-03 10:06:29', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (44, 1, 'sdf', '2024-06-03 15:24:13', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (45, 1, 'sdfs', '2024-06-03 15:24:22', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (46, 1, '23', '2024-06-03 15:24:47', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (47, 1, 'sdf', '2024-06-03 15:38:56', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (48, 1, 'aaaaaa', '2024-06-03 15:49:49', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (49, 1, 'sdf', '2024-06-06 13:36:31', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (50, 1, 'dfs', '2024-06-06 13:37:32', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (51, 1, 'qq', '2024-06-06 15:12:51', 0);
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (52, 1, 'g', '2024-06-06 15:16:57', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID  角色表',
  `role_name` varchar(10) NOT NULL COMMENT '角色名称',
  `code` varchar(10) NOT NULL COMMENT '角色编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

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
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_name` varchar(255) NOT NULL COMMENT '用户名',
  `real_name` varchar(20) DEFAULT NULL COMMENT '真实姓名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) DEFAULT 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg' COMMENT '头像地址',
  `role_id` int(11) DEFAULT '1' COMMENT '角色id',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间   YYYY-MM-DD hh:mm:ss',
  `status` int(11) DEFAULT '1' COMMENT '状态  1正常0禁用',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_name` (`user_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
BEGIN;
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (1, 'admin', '管理员', '$2a$10$/ZdKFY15AWNLOeTqAp91a.uDa0JDioj1wVYGgpn.HKMYh9vq0Uh4S', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/27eb3a59-4d05-4bce-90ec-4a2457452886.png', 3, 78, '2024-05-23 16:06:07', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (2, 'zhangsan', '张三', '$2a$10$61ogZY7EXsMDWeVGQpDq3OBF1.phaUu7.xrwLyWFTOu8woE08zMIW', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, 8, '2024-05-06 15:35:23', 0, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (3, 'abc', 'adf', '$2a$10$.QCdZcBoezfOjeZZdGznrOb/287ublU0OCq4eRPPfZghuRChWSC7m', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 8, NULL, 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (4, '地方', '是为对方', '$2a$10$wZVpSnaaPJGz5xnmO7YwZ.5t6FdptApQFcYzRZb2pmiIyGU7/HJnu', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 9, '2024-05-13 11:19:41', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (5, '显得十分', '士大夫', '$2a$10$RF2qOZhK20nOhxT9y2CmauhM3ofsqZPOv7bvGf7FEVXtiKtH9NiY.', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 9, '2024-05-13 11:22:18', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (15, 'lisi', '李四', '$2a$10$OmgPV5i8rUmj5KWs5ILDyuz3JXXkMUqxoWaqD/iHQpI.s4hV47aYu', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 79, '2024-05-13 15:04:02', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (17, 'wangwu', '王五', '$2a$10$DmvS9HAYbwuaxiTbP3PaZ.34ccf.Sws/dgOUcDdSlWVfhsQugak7e', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, NULL, 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (18, '11356', '王五', '$2a$10$c9Lj1X40uO8T12zlBZPZyuEBqM8cOaoEhjQk.0pgsRmWPZj.4Xg0O', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-13 15:41:45', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (20, 'w156521211', '张三', '$2a$10$Ov4Q5ikaTX2BFDONV/Y6AO8dmHJt6/jE7mHPwNRSwGAlE3Nk4WFs2', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-18 00:43:58', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (21, '156415612211', '多选题', '$2a$10$6uCPinXKEqbO9Kl9Wtac4OcCrxj3OriF6kvQ9jciL.PlL89OtnLiC', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-18 00:43:58', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (22, 'thomsa11', '李四', '$2a$10$D1YYMMbFDEverfUP28tIR.ck11XV6gcZA9O0BuPnmRLN2k/imRXgi', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-18 00:43:58', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (23, 'aobaoma11', '王五', '$2a$10$ekzbC38jstUxp2KadoOXhOzcdGCLjJJVf1TM945Gbfvh66U2Kxu5.', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-18 00:43:58', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (33, 'abca', 'abca', '$2a$10$cJPlmEKg9fVRlXbWMM6U9eFrERxrrwLOlOaFOXAxffFhzE1RNb1lG', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-25 22:04:32', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (38, 'abcde', 'hhhhh', '$2a$10$CZ.SHHp2TT4npaG83QhC7esKmbFenBT01YRs7OOzsY/Rz2yltkyvG', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-26 11:38:04', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (39, '1111', '1111', '$2a$10$8xOCS4.ItI2lYb1RCD3IwuOqJzbkODI.pWz37Xd6TN2sSV1gTa03e', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-27 13:46:36', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (40, 'xiaoming', '小明', '$2a$10$PKQmCeCpT4BRmXId6RUff.lcROKNnqMVUvMzqehGKVgtnEJbZEH.a', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-27 13:48:06', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (41, 'xiaohong', '小红', '$2a$10$IMLe7Fq0SusMVtiqrLgcEOPGbLYvt4Eyo4iCmGyfYMIk9ocP5A7Ce', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 79, '2024-05-27 13:48:37', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (42, 'xiaoliu', '小刘', '$2a$10$.MIHk7EyNkw/Z8kGXcoeKuxs0byQh6M9RAmEZ775a4Khf9qmRud96', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 73, '2024-05-27 14:14:34', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (43, 'xiaoxue', '小学', '$2a$10$B4GYOm5aHpRvYbeAAq80BuiD0RdQeWRKLBDpQRUZnYr2RMbU8r/ne', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-27 14:25:23', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (44, 'xiaofang', '小方', '$2a$10$1j4I69VEOSLRJ0Dom9dXx.iOSkYRxr1KpxSTSbjFGkuD6whpg6J3C', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-27 14:27:08', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (45, 'xiao', '小方', '$2a$10$9yveHNV730mUhVGji..XFO70cTxL686sfrv3kJjq1wr.b/T9iQvHC', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-27 14:28:12', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (46, 'xia', '小', '$2a$10$62ODbE9f93j3jKcs3hmfCOH8vk1.6Sr4wnErFCkWf1tGcwK/jr55a', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-27 14:29:40', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (48, 'abcde3', 'adfads', '$2a$10$gS5sVThZ5qf56poaGz06j.j5AQv6SdpjQyVC5pb4a0iuWysLkhVX2', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-05-27 14:32:39', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (49, 'xiaojie', '小洁', '$2a$10$b.d8pZYpdAgpdrs59wfaGOnCGOgBgSB.mC/lTwx.oUJIcbtZ87nvu', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 72, '2024-05-27 14:56:30', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (50, 'xiaojia', '小佳', '$2a$10$bR4nZb2CnbtKlQmFqhoJCOtRwJlt4ZirzSF6tVTVOmtXzwSAReFFq', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 72, '2024-05-27 14:58:44', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (51, 'xiaoyue', '小悦', '$2a$10$ppCOuV6i0GdTIYiWb8FnJu3ukEBPAlj5YhUuD9quBP1IQnOq1nvcu', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 75, '2024-05-27 15:00:08', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (56, 'abcd', 'abcd', '$2a$10$WXsjNcvL/wKDH683MGtd/O1vq49h4oOZ3Bgv5EHsIwlxDntgAWsfK', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-30 15:17:32', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (57, 'abc1111', '1', '$2a$10$cTVoIei/3CS9jpeymB2mAueUUeq2SbibICznHfq1y7gwFZAuSCo9e', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-30 15:18:25', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (58, 'xiaona', '小娜', '$2a$10$A7FFGzQj2ZZBBJzwNLJwKO86FuC0Nu5nyKs6fYVRxyhJ05dSMViiy', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/1b560ce8-0dfd-40ab-8784-3693a351d426.png', 1, 79, '2024-05-30 15:18:34', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (59, 'sdfsdf', 'af', '$2a$10$3jGlYdRvOH0HGpF0Q22b9uAWm66o/45puUDaCwpkx3lctZJT/niga', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-05-30 15:21:20', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (76, 'weijin', '魏进', '$2a$10$3jGlYdRvOH0HGpF0Q22b9uAWm66o/45puUDaCwpkx3lctZJT/niga', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 3, NULL, NULL, 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (77, 'abcdefg', '11', '$2a$10$oqqAwgQ8qoJYJ1GhQdwumO4a8d0345YRkZAgQK6e1.s5I3OSbyFVG', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-03 10:43:39', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (78, 'test2024', 'test', '$2a$10$CRdk7TEk.Gt/ik65WaaLvOYjs3A.khrM5u8VYA2kafK6gaPbJhKBu', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-03 13:48:24', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (80, 'abcabc', 'abcabc', '$2a$10$FdIvgrketX1aXnbIS3UdKOBPmdx2gj8HfZiKb9ijS1bz4xA.D23/S', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-03 16:49:21', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (81, 'admin1', '111', '$2a$10$yzxHSQk8IN9l.6jKW5nerus.NgwAs7nOneTgxY7YIi8jHIuzMR7KC', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-04 09:12:08', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (82, 'hanqkent', 'hql', '$2a$10$jOq.krjS5YIsNtT6Cd8EqugNfjeCauN605Ujy6BWHww2q6w86SXg2', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-04 14:55:42', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (83, 'ff', 'ff', '$2a$10$Qlmn4NI2XNOM7xSHT7q1S.JulyV8vB77b4tCg.r1EXcIsDYetykma', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-05 01:32:06', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (84, 'xiaoguo', 'xiaoguo', '$2a$10$J6gjNa5uBC4MNPdX6j9jSegqJLhe7jJfNj23TYKDdftK0OoSLlGE6', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-05 19:57:39', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (85, '541360874@qq.com', '541360874@qq.com', '$2a$10$5IcD7lWnJTu0h9LjEXFz4OtpSv8VDZJW1Tm7wl8NeA7vuco3bgDCy', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-05 23:03:19', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (86, 'banpingcu', '胡自铖', '$2a$10$BuSwXg8/LWq.q4aoHMgKCORjnQQj2CQVyZubj5l.sYdl9uxaJmHZi', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-05 23:59:17', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (87, 'admin123', '胡自铖', '$2a$10$dOzNbpk17xzYJc7zneC/IuzLsI9aQHbGOx8Wsp3p.CcpQkR7xRtVS', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-06 10:06:34', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (88, 'root', '123', '$2a$10$e85V6iRdDdUGpjX3TTxWee713G2QcPHghURk98AZVhSase8Xha5ra', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-06 11:18:17', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (89, 'jason', 'phaedrus', '$2a$10$G65eqkKwxNka9ewhEDx/KelknUtEgoRYIRV2XOyWMPi2apzJ2YD6m', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-06 11:27:35', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (90, 'test9', 'test9', '$2a$10$/kRTdRgVNpgOvY9cU9BRHuSUYQjhvzhAaxrzpnvnZ/agW8fhPCD6.', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-06 11:32:14', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (91, 'zhangyancheng', 'zhangyancheng', '$2a$10$ioEuRVpE5ltn0WYAFa5wbuzz4uz1k3EeylM4812aq82qYLwxcVAGK', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-06 11:45:40', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (92, 'abcsdfa', 'dsfasdf', '$2a$10$sGgxm8s6vBGUk/ejRUDr/eV2zDa/DshYhoCkXUb6mo.EO/eN2YVIe', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2024-06-06 13:36:59', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (93, '阿斯蒂芬v发表', 'vdfbvbv', '$2a$10$F8hoArHcTe4pkBLy4O17G.C66Xz5HBdu4gbb7DOfdKVQJnwBH3olK', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-06 15:05:59', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (94, 'QQ', '小娜', '$2a$10$mivFe6H22iwBLcstoBMmleTou4hLE.d61TUf/IMyS4JKIKR5I.0he', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, NULL, '2024-06-06 15:22:54', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_user_book
-- ----------------------------
DROP TABLE IF EXISTS `t_user_book`;
CREATE TABLE `t_user_book` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   错题本',
  `exam_id` int(11) DEFAULT NULL COMMENT '考试id  唯一',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id    唯一',
  `qu_id` int(11) DEFAULT NULL COMMENT '题目id    唯一',
  `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间    YYYY-MM-DD hh:mm:ss',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=122 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_book
-- ----------------------------
BEGIN;
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (102, 71, 1, 594, '2024-05-29 17:46:37');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (106, 74, 58, 593, '2024-05-30 22:52:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (107, 74, 58, 595, '2024-05-30 22:52:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (108, 74, 58, 598, '2024-05-30 22:52:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (109, 74, 58, 600, '2024-05-30 22:52:54');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (110, 77, 58, 593, '2024-06-03 09:37:06');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (112, 79, 58, 596, '2024-06-03 10:45:29');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (113, 79, 58, 599, '2024-06-03 10:45:29');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (114, 79, 58, 600, '2024-06-03 10:45:29');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (116, 80, 1, 600, '2024-06-06 15:21:14');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (117, 80, 1, 603, '2024-06-06 15:21:14');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (120, 80, 1, 606, '2024-06-06 15:21:14');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (121, 80, 1, 609, '2024-06-06 15:21:14');
COMMIT;

-- ----------------------------
-- Table structure for t_user_daily_login_duration
-- ----------------------------
DROP TABLE IF EXISTS `t_user_daily_login_duration`;
CREATE TABLE `t_user_daily_login_duration` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键，自增长',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID，与用户表关联，确保数据一致性',
  `login_date` date DEFAULT NULL COMMENT '登录日期，记录用户在哪一天的登录时长',
  `total_seconds` int(11) DEFAULT NULL COMMENT '累积在线秒数，每日登录总时长，单位为秒',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_user_daily_login_duration
-- ----------------------------
BEGIN;
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (2, 3, '2024-05-28', 29);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (3, 3, '2024-05-29', 43194);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (19, 1, '2024-05-29', 30583);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (20, 2, '2024-05-29', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (21, 1, '2024-05-30', 22255);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (22, 2, '2024-05-30', 6140);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (23, 3, '2024-05-30', 7320);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (24, 58, '2024-05-30', 2688);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (25, 1, '2024-05-31', 54364);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (26, 58, '2024-05-31', 9466);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (27, 3, '2024-05-31', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (28, 2, '2024-05-31', 9386);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (29, 1, '2024-06-01', 5439);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (30, 58, '2024-06-01', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (31, 1, '2024-06-02', 599);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (32, 1, '2024-06-03', 47127);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (33, 58, '2024-06-03', 22725);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (34, 76, '2024-06-03', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (35, 2, '2024-06-03', 2799);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (36, 3, '2024-06-03', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (37, 80, '2024-06-03', 1198);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (38, 1, '2024-06-05', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (39, 84, '2024-06-05', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (40, 1, '2024-06-06', 31905);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (41, 58, '2024-06-06', 408);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (42, 2, '2024-06-06', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (43, 1, '2024-06-07', 27047);
COMMIT;

-- ----------------------------
-- Table structure for t_user_exams_score
-- ----------------------------
DROP TABLE IF EXISTS `t_user_exams_score`;
CREATE TABLE `t_user_exams_score` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id  用户考试成绩表',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id  唯一',
  `exam_id` int(11) DEFAULT NULL COMMENT '试卷id  唯一',
  `total_time` int(11) DEFAULT NULL COMMENT '总时长  YYYY-MM-DD hh:mm:ss',
  `user_time` int(11) DEFAULT NULL COMMENT '用户用时  YYYY-MM-DD hh:mm:ss',
  `user_score` int(11) unsigned DEFAULT '0' COMMENT '用户得分',
  `limit_time` datetime DEFAULT NULL COMMENT '交卷时间  YYYY-MM-DD hh:mm:ss',
  `count` int(11) DEFAULT '0' COMMENT '切屏次数',
  `state` int(11) DEFAULT NULL COMMENT '状态   0正在考试 1考试完成',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间  YYYY-MM-DD hh:mm:ss',
  `whether_mark` int(11) DEFAULT NULL COMMENT '是否阅卷-1无简答题、0未阅卷、1阅卷',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`)
) ENGINE=InnoDB AUTO_INCREMENT=148 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exams_score
-- ----------------------------
BEGIN;
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (102, 1, 71, 60, 20, 3, '2024-05-29 17:46:37', 0, 1, '2024-05-29 17:46:17', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (121, 58, 71, 60, 2700, 15, '2024-05-29 23:00:00', 0, 0, '2024-05-30 22:41:58', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (122, 58, 74, 60, 61, 11, '2024-05-30 22:52:54', 5, 1, '2024-05-30 22:51:52', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (128, 1, 75, 60, 77, 8, '2024-05-31 00:05:49', 0, 1, '2024-05-31 00:04:32', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (133, 58, 77, 60, 150, 5, '2024-06-03 09:37:06', 3, 1, '2024-06-03 09:34:36', -1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (134, 58, 79, 60, 44, 5, '2024-06-03 10:45:29', 0, 1, '2024-06-03 10:44:45', 1);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (143, 1, 80, 60, 1699, 1, '2024-06-06 15:21:14', 2, 1, '2024-06-06 14:52:55', 0);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (144, 1, 74, 60, NULL, 0, NULL, 2, 0, '2024-06-06 15:23:50', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (145, 1, 76, 60, NULL, 0, NULL, 1, 0, '2024-06-06 15:24:10', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (146, 1, 77, 60, NULL, 0, NULL, 4, 0, '2024-06-06 15:29:50', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (147, 1, 78, 60, NULL, 0, NULL, 1, 0, '2024-06-06 17:19:23', NULL);
COMMIT;

-- ----------------------------
-- Table structure for t_user_exercise_record
-- ----------------------------
DROP TABLE IF EXISTS `t_user_exercise_record`;
CREATE TABLE `t_user_exercise_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   用户刷题记录表',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `repo_id` int(11) DEFAULT NULL COMMENT '题库id',
  `total_count` int(11) DEFAULT NULL COMMENT '总题数',
  `exercise_count` int(11) DEFAULT NULL COMMENT '已刷题数',
  `create_time` datetime DEFAULT NULL COMMENT '刷题时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exercise_record
-- ----------------------------
BEGIN;
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (4, 1, 37, 0, 12, '2024-05-29 17:42:01');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (5, 58, 38, 0, 2, '2024-05-30 15:52:19');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (6, 58, 37, 0, 8, '2024-05-30 15:56:42');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (7, 78, 37, 0, 16, '2024-06-03 13:49:33');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (8, 80, 38, 2, 1, '2024-06-03 16:50:50');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (9, 84, 37, 0, 11, '2024-06-05 19:58:24');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (10, 88, 37, 0, 2, '2024-06-06 11:18:55');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (11, 89, 37, 12, 1, '2024-06-06 11:28:48');
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (12, 1, 38, 0, 2, '2024-06-06 14:17:02');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
