/*
 Navicat Premium Data Transfer

 Source Server         : DockerMysql5.7
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : localhost:3306
 Source Schema         : db_exam

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 27/04/2025 21:24:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_category
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
  `parent_id` int(11) DEFAULT '0' COMMENT '父分类ID，0表示一级分类',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_category
-- ----------------------------
BEGIN;
INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `create_time`, `is_deleted`) VALUES (1, '语文', 0, 1, '2025-04-24 14:33:30', 0);
INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `create_time`, `is_deleted`) VALUES (2, '数学', 0, 2, '2025-04-24 14:33:30', 0);
INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `create_time`, `is_deleted`) VALUES (3, '英语', 0, 3, '2025-04-24 14:33:30', 0);
INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `create_time`, `is_deleted`) VALUES (4, '模拟试题', 1, 1, '2025-04-24 14:33:30', 0);
INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `create_time`, `is_deleted`) VALUES (5, '历年真题', 1, 2, '2025-04-24 14:33:30', 0);
INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `create_time`, `is_deleted`) VALUES (6, '计算机', 0, 0, '2025-04-24 22:34:25', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_certificate
-- ----------------------------
DROP TABLE IF EXISTS `t_certificate`;
CREATE TABLE `t_certificate` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   证书',
  `certificate_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '证数名称',
  `image` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '背景图片',
  `certification_nuit` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '认证单位',
  `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_certificate_user
-- ----------------------------
DROP TABLE IF EXISTS `t_certificate_user`;
CREATE TABLE `t_certificate_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   证书与用户关系表',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `exam_id` int(11) DEFAULT NULL COMMENT '考试id',
  `code` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '证书编号',
  `certificate_id` int(11) DEFAULT NULL COMMENT '证书id',
  `create_time` datetime DEFAULT NULL COMMENT '获奖时间      YYYY-MM-DD hh:mm:ss',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate_user
-- ----------------------------
BEGIN;
INSERT INTO `t_certificate_user` (`id`, `user_id`, `exam_id`, `code`, `certificate_id`, `create_time`) VALUES (2, 164, 120, 'ktlrQPhxIAyGSQKJhB', NULL, '2025-04-26 14:35:36');
COMMIT;

-- ----------------------------
-- Table structure for t_discussion
-- ----------------------------
DROP TABLE IF EXISTS `t_discussion`;
CREATE TABLE `t_discussion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '发布人id',
  `grade_id` int(11) NOT NULL COMMENT '接收班级id',
  `content` text NOT NULL COMMENT '内容',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `create_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_discussion
-- ----------------------------
BEGIN;
INSERT INTO `t_discussion` (`id`, `user_id`, `grade_id`, `content`, `title`, `create_time`) VALUES (1, 163, 124, '测试发布讨论 -内容1', '测试发布讨论 -标题1', '2025-04-04 17:55:10');
COMMIT;

-- ----------------------------
-- Table structure for t_exam
-- ----------------------------
DROP TABLE IF EXISTS `t_exam`;
CREATE TABLE `t_exam` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id    考试表',
  `title` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '考试名称',
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
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam
-- ----------------------------
BEGIN;
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (119, 'test3', 1, 1, 21, 1, 163, NULL, 0, 0, 0, 0, 3, 7, 0, 0, '2025-04-20 16:00:00', '2025-05-14 16:00:00', '2025-04-26 14:07:42', 0);
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (120, 'test1', 100, 1, 20, 100, 163, NULL, 0, 0, 0, 0, 10, 2, 0, 0, '2025-04-09 16:00:00', '2025-05-27 16:00:00', '2025-04-26 14:34:59', 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=207 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (202, 116, 124);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (203, 117, 124);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (204, 118, 124);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (205, 119, 124);
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (206, 120, 124);
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
  `answer_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '答案id  用于客观题，多选题id使用“，”分隔',
  `answer_content` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '答案内容    用于主观题',
  `checkout` int(11) DEFAULT NULL COMMENT '是否选中   0未选中  1选中',
  `is_sign` int(11) DEFAULT NULL COMMENT '是否标记   0未标记  1标记',
  `is_right` int(11) DEFAULT NULL COMMENT '是否正确   用于客观题，0错误 1正确',
  `ai_score` int(11) DEFAULT NULL COMMENT 'ai评分',
  `ai_reason` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'ai评分原因',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`,`question_id`)
) ENGINE=InnoDB AUTO_INCREMENT=294 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_qu_answer
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (284, 164, 120, 718, 3, '2642', NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (285, 164, 120, 719, 3, '2645', NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (286, 164, 120, 720, 3, '2647', NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (287, 164, 120, 721, 3, '2648', NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (288, 164, 120, 722, 3, '2651', NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (289, 164, 120, 723, 3, '2652', NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (290, 164, 120, 724, 3, '2655', NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (291, 164, 120, 725, 3, '2656', NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (292, 164, 120, 726, 3, '2659', NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES (293, 164, 120, 727, 3, '2660', NULL, NULL, NULL, 1, NULL, NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=540 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_question
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (521, 118, 718, 7, 0, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (522, 118, 718, 7, 0, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (523, 118, 719, 7, 1, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (524, 118, 720, 7, 2, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (525, 118, 718, 7, 0, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (526, 118, 719, 7, 1, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (527, 119, 718, 7, 0, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (528, 119, 719, 7, 1, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (529, 119, 720, 7, 2, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (530, 120, 718, 2, 0, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (531, 120, 719, 2, 1, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (532, 120, 720, 2, 2, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (533, 120, 721, 2, 3, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (534, 120, 722, 2, 4, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (535, 120, 723, 2, 5, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (536, 120, 724, 2, 6, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (537, 120, 725, 2, 7, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (538, 120, 726, 2, 8, 3);
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (539, 120, 727, 2, 9, 3);
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
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (104, 116, NULL);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (105, 117, NULL);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (106, 118, NULL);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (107, 119, NULL);
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (108, 120, NULL);
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
  `answer` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '主观题答案',
  `question_type` int(11) NOT NULL COMMENT '题目类型',
  `options` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '客观题答案集合  用于客观题,多选题id使用","分隔',
  `is_right` int(11) DEFAULT NULL COMMENT '客观题是否正确',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `repo_id` (`repo_id`,`question_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=165 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exercise_record
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_grade`;
CREATE TABLE `t_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   班级表',
  `grade_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '班级名称',
  `user_id` int(11) DEFAULT NULL COMMENT '创建人id',
  `code` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '班级口令',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `grade_name` (`grade_name`) USING BTREE,
  UNIQUE KEY `code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (124, '测试班级', 1, 'OQPr7cXFdLxUPq0ZpJ', 0);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_grade_exercise
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_like
-- ----------------------------
DROP TABLE IF EXISTS `t_like`;
CREATE TABLE `t_like` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `discussion_id` int(11) DEFAULT NULL,
  `reply_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `discussion_id` (`discussion_id`,`reply_id`,`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of t_like
-- ----------------------------
BEGIN;
INSERT INTO `t_like` (`id`, `discussion_id`, `reply_id`, `user_id`, `create_time`) VALUES (11, 1, 12, 163, '2025-04-18 17:51:08');
COMMIT;

-- ----------------------------
-- Table structure for t_log
-- ----------------------------
DROP TABLE IF EXISTS `t_log`;
CREATE TABLE `t_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `place` varchar(255) NOT NULL COMMENT '登录地点',
  `behavior` varchar(255) NOT NULL COMMENT '操作行为',
  `device` varchar(255) NOT NULL COMMENT '登录设备',
  `user_id` int(11) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1481613429 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of t_log
-- ----------------------------
BEGIN;
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-04 15:57:38');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (2, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-04 15:59:49');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (3, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-04 16:05:40');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (4, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-04 16:05:47');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613319, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-04 16:07:07');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613320, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-04 16:07:17');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613321, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-04 16:11:55');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613322, '0|0|0|内网IP|内网IP', '登录失败', 'Macintosh', 1, '2025-04-04 16:13:10');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613323, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-04 16:13:17');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613324, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-04 16:15:47');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613325, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-04 16:15:54');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613326, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-04 16:16:03');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613327, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-04 16:16:10');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613328, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-04 17:59:08');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613329, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-04 17:59:14');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613330, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-04 21:40:02');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613331, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-04 22:21:57');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613332, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-04 22:22:09');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613333, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-04 22:22:47');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613334, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-04 22:22:54');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613335, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-04 23:06:05');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613336, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-04 23:06:20');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613337, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-04 23:06:34');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613338, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-04 23:06:42');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613339, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-04 23:07:43');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613340, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-05 00:03:04');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613341, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-05 00:03:45');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613342, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-05 00:03:53');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613343, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-05 09:23:35');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613344, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-05 09:31:58');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613345, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-05 09:32:05');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613346, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-05 09:44:49');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613347, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-05 09:45:10');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613348, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-05 09:45:52');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613349, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-05 09:46:04');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613350, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-06 15:06:00');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613351, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-06 15:06:52');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613352, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-06 15:07:06');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613353, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-06 15:07:18');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613354, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-06 15:07:28');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613355, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-06 16:48:05');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613356, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-06 16:48:17');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613357, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-06 16:55:03');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613358, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-06 16:55:11');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613359, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-06 16:58:59');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613360, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-06 16:59:09');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613361, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-06 17:51:39');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613362, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-06 17:51:48');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613363, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-06 18:08:42');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613364, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-06 18:09:24');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613365, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-06 18:09:34');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613366, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-06 20:00:43');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613367, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-06 20:09:56');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613368, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-06 20:10:05');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613369, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-06 20:10:24');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613370, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-06 20:10:32');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613371, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-06 20:11:38');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613372, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-06 20:11:46');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613373, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-06 20:13:00');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613374, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-06 20:13:08');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613375, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-06 20:14:14');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613376, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-06 20:14:21');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613377, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-06 20:41:00');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613378, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-06 20:41:08');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613379, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-06 20:41:53');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613380, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-06 20:41:59');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613381, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-06 20:42:08');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613382, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-06 20:42:14');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613383, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-06 20:44:50');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613384, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-06 20:44:57');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613385, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-17 22:23:14');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613386, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-18 17:48:01');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613387, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-18 17:54:41');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613388, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-18 17:54:49');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613389, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-18 17:55:24');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613390, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-18 17:55:31');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613391, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-25 21:14:16');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613392, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-25 21:14:24');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613393, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-25 21:15:24');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613394, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-25 21:15:34');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613395, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-25 21:16:08');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613396, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-25 21:16:22');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613397, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-25 21:16:41');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613398, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-26 10:14:28');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613399, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-26 13:40:09');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613400, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-26 13:40:23');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613401, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-26 13:50:06');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613402, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-26 13:50:14');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613403, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-26 13:50:46');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613404, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-26 13:51:04');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613405, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-26 13:51:54');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613406, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-26 13:52:07');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613407, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-26 13:53:42');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613408, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-26 13:53:49');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613409, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-26 14:07:45');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613410, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-26 14:08:03');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613411, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-26 14:34:18');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613412, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-26 14:34:32');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613413, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-26 14:35:02');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613414, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 164, '2025-04-26 14:35:10');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613415, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 164, '2025-04-26 14:35:39');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613416, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-26 14:35:49');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613417, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 163, '2025-04-26 15:00:35');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613418, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-26 15:00:40');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613419, '0|0|0|内网IP|内网IP', '设备登出', 'Macintosh', 1, '2025-04-26 15:03:28');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613420, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-26 15:03:34');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613421, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 1, '2025-04-27 00:05:49');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613422, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-27 00:26:22');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613423, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-27 00:26:50');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613424, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-27 01:32:07');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613425, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-27 08:18:53');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613426, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-27 19:49:03');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613427, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-27 19:51:25');
INSERT INTO `t_log` (`id`, `place`, `behavior`, `device`, `user_id`, `create_time`) VALUES (1481613428, '0|0|0|内网IP|内网IP', '设备登录', 'Macintosh', 163, '2025-04-27 20:03:42');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_manual_score
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   公告表',
  `title` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '公告标题',
  `image` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '图片地址',
  `content` text COLLATE utf8mb4_bin NOT NULL COMMENT '公告内容',
  `user_id` int(11) DEFAULT NULL COMMENT '创建用户ID   唯一',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  `is_public` int(11) DEFAULT NULL COMMENT '是否公开所有用户，老师为所有班级，管理员为所有用户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_notice_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_grade`;
CREATE TABLE `t_notice_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   公告班级关联表',
  `notice_id` int(11) DEFAULT NULL COMMENT '公告id',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级',
  `is_deleted` int(11) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=199 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice_grade
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_option
-- ----------------------------
DROP TABLE IF EXISTS `t_option`;
CREATE TABLE `t_option` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   选项答案表',
  `qu_id` int(11) NOT NULL COMMENT '试题id',
  `is_right` int(11) DEFAULT NULL COMMENT '是否正确',
  `image` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '图片地址   0错误 1正确',
  `content` text COLLATE utf8mb4_bin NOT NULL COMMENT '选项内容',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2666 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_option
-- ----------------------------
BEGIN;
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2642, 718, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2643, 718, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2644, 719, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2645, 719, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2646, 720, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2647, 720, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2648, 721, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2649, 721, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2650, 722, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2651, 722, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2652, 723, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2653, 723, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2654, 724, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2655, 724, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2656, 725, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2657, 725, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2658, 726, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2659, 726, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2660, 727, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2661, 727, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2662, 728, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2663, 728, 0, NULL, '错误', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2664, 729, 1, NULL, '正确', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2665, 729, 0, NULL, '错误', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_question
-- ----------------------------
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   试题表',
  `qu_type` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '试题类型',
  `image` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '试题图片',
  `content` text COLLATE utf8mb4_bin NOT NULL COMMENT '题干',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `analysis` text COLLATE utf8mb4_bin COMMENT '题目分析',
  `repo_id` int(11) DEFAULT NULL COMMENT '题库id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=730 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_question
-- ----------------------------
BEGIN;
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (718, '3', NULL, '112312', '2025-04-26 10:26:18', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (719, '3', NULL, '123123', '2025-04-26 10:26:26', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (720, '3', NULL, '123123', '2025-04-26 10:26:35', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (721, '3', NULL, '1231', '2025-04-26 10:26:42', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (722, '3', NULL, '123123', '2025-04-26 10:26:50', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (723, '3', NULL, '13123', '2025-04-26 11:03:51', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (724, '3', NULL, '123123', '2025-04-26 11:04:00', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (725, '3', NULL, '12312312', '2025-04-26 11:04:07', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (726, '3', NULL, '1231231', '2025-04-26 11:04:14', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (727, '3', NULL, '123123', '2025-04-26 11:04:21', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (728, '3', NULL, '123123', '2025-04-26 11:04:28', NULL, 99, 163, 0);
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (729, '3', NULL, '123123', '2025-04-26 11:04:36', NULL, 99, 163, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_reply
-- ----------------------------
DROP TABLE IF EXISTS `t_reply`;
CREATE TABLE `t_reply` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `discussion_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `parent_id` int(11) DEFAULT NULL COMMENT '父评论id',
  `content` varchar(255) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_reply
-- ----------------------------
BEGIN;
INSERT INTO `t_reply` (`id`, `discussion_id`, `user_id`, `parent_id`, `content`, `create_time`) VALUES (8, 1, 163, -1, '111212121', '2025-04-04 21:28:06');
INSERT INTO `t_reply` (`id`, `discussion_id`, `user_id`, `parent_id`, `content`, `create_time`) VALUES (9, 1, 163, -1, '委屈二分身乏术', '2025-04-04 21:29:30');
INSERT INTO `t_reply` (`id`, `discussion_id`, `user_id`, `parent_id`, `content`, `create_time`) VALUES (10, 1, 163, -1, '111', '2025-04-04 21:57:00');
INSERT INTO `t_reply` (`id`, `discussion_id`, `user_id`, `parent_id`, `content`, `create_time`) VALUES (12, 1, 164, -1, '2223213', '2025-04-04 22:13:05');
INSERT INTO `t_reply` (`id`, `discussion_id`, `user_id`, `parent_id`, `content`, `create_time`) VALUES (18, 1, 163, 12, '测试评论', '2025-04-05 09:46:47');
COMMIT;

-- ----------------------------
-- Table structure for t_repo
-- ----------------------------
DROP TABLE IF EXISTS `t_repo`;
CREATE TABLE `t_repo` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   题库表',
  `user_id` int(11) NOT NULL COMMENT '创建人id',
  `title` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '题库标题',
  `category_id` int(11) DEFAULT NULL COMMENT '分类ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  `is_exercise` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `category_id`, `create_time`, `is_deleted`, `is_exercise`) VALUES (99, 163, '测试题库', 6, '2025-04-24 22:34:33', 0, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID  角色表',
  `role_name` varchar(10) COLLATE utf8mb4_bin NOT NULL COMMENT '角色名称',
  `code` varchar(10) COLLATE utf8mb4_bin NOT NULL COMMENT '角色编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  `user_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
  `real_name` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '真实姓名',
  `password` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) COLLATE utf8mb4_bin DEFAULT 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg' COMMENT '头像地址',
  `role_id` int(11) DEFAULT '1' COMMENT '角色id',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间   YYYY-MM-DD hh:mm:ss',
  `status` int(11) DEFAULT '1' COMMENT '状态  1正常0禁用',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_name` (`user_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=165 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
BEGIN;
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (1, 'admin', '管理员', '$2a$10$/ZdKFY15AWNLOeTqAp91a.uDa0JDioj1wVYGgpn.HKMYh9vq0Uh4S', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/27eb3a59-4d05-4bce-90ec-4a2457452886.png', 3, 106, '2024-05-23 16:06:07', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (163, 'teacher', '教师测试账号', '$2a$10$odROzQ2vFaHRomD9UG4uWO65twiqI33Y1RktzqtxWNLaVf5.luivy', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2025-03-22 11:38:18', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (164, 'student', '学生测试账号', '$2a$10$vcnsGkVJdeH0tdKiLa1.d.qbPD/.5B5Ah3qYzeN6rem5P/U8jGiMS', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 124, '2025-03-22 11:38:36', 1, 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=218 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_book
-- ----------------------------
BEGIN;
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (213, 120, 164, 719, '2025-04-26 14:35:36');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (214, 120, 164, 720, '2025-04-26 14:35:36');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (215, 120, 164, 722, '2025-04-26 14:35:36');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (216, 120, 164, 724, '2025-04-26 14:35:36');
INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES (217, 120, 164, 726, '2025-04-26 14:35:36');
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
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_user_daily_login_duration
-- ----------------------------
BEGIN;
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (126, 164, '2025-03-22', 0);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (127, 164, '2025-04-24', 564020);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (128, 164, '2025-04-25', 28830);
INSERT INTO `t_user_daily_login_duration` (`id`, `user_id`, `login_date`, `total_seconds`) VALUES (129, 164, '2025-04-26', 63127);
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
  `user_score` int(10) unsigned DEFAULT '0' COMMENT '用户得分',
  `limit_time` datetime DEFAULT NULL COMMENT '交卷时间  YYYY-MM-DD hh:mm:ss',
  `count` int(11) DEFAULT '0' COMMENT '切屏次数',
  `state` int(11) DEFAULT NULL COMMENT '状态   0正在考试 1考试完成',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间  YYYY-MM-DD hh:mm:ss',
  `whether_mark` int(11) DEFAULT NULL COMMENT '是否阅卷-1无简答题、0未阅卷、1阅卷',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`)
) ENGINE=InnoDB AUTO_INCREMENT=206 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exams_score
-- ----------------------------
BEGIN;
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (204, 164, 119, 1, NULL, 0, NULL, 0, 0, '2025-04-26 14:08:10', NULL);
INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES (205, 164, 120, 100, 19, 10, '2025-04-26 14:35:36', 0, 1, '2025-04-26 14:35:17', -1);
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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exercise_record
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_user_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_user_grade`;
CREATE TABLE `t_user_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `u_id` int(11) DEFAULT NULL COMMENT '用户id',
  `g_id` int(11) DEFAULT NULL COMMENT '教师id',
  `is_deleted` int(11) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `u_id` (`u_id`,`g_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_user_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_user_grade` (`id`, `u_id`, `g_id`, `is_deleted`) VALUES (10, 163, 124, 0);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
