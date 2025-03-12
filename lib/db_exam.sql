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

 Date: 12/03/2025 21:45:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate
-- ----------------------------
BEGIN;
INSERT INTO `t_certificate` (`id`, `certificate_name`, `image`, `certification_nuit`, `create_time`, `is_deleted`) VALUES (70, '优秀毕业学生', NULL, '夕阳红小学', '2025-03-12 21:36:21', 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_certificate_user
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam
-- ----------------------------
BEGIN;
INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES (107, '语文常识题考试', 100, 10, 10, 20, 148, 70, 1, 10, 0, 0, 0, 0, 0, 0, '2025-02-28 16:00:00', '2025-04-29 16:00:00', '2025-03-12 21:42:01', 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=192 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES (191, 107, 118);
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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`,`question_id`)
) ENGINE=InnoDB AUTO_INCREMENT=276 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_qu_answer
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB AUTO_INCREMENT=500 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_question
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES (499, 107, 702, 10, 0, 1);
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
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exam_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES (95, 107, 74);
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
) ENGINE=InnoDB AUTO_INCREMENT=161 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_exercise_record
-- ----------------------------
BEGIN;
INSERT INTO `t_exercise_record` (`id`, `repo_id`, `question_id`, `user_id`, `answer`, `question_type`, `options`, `is_right`) VALUES (160, 74, 702, 149, '2609', 1, NULL, 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES (118, '夕阳小学1班', 148, 'GkrFkcD5uLcZupI5Q3', 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice
-- ----------------------------
BEGIN;
INSERT INTO `t_notice` (`id`, `title`, `image`, `content`, `user_id`, `create_time`, `is_deleted`) VALUES (93, '3月12日上午9点--语文考试', NULL, '<p>考试内容为常识题</p>', 148, '2025-03-12 21:41:14', 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_notice_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_notice_grade` (`id`, `notice_id`, `grade_id`) VALUES (119, 93, 118);
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
  `content` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '选项内容',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2612 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_option
-- ----------------------------
BEGIN;
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2608, 702, 0, NULL, '儿子', 0, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2609, 702, 0, NULL, '孙子', 1, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2610, 702, 1, NULL, '爷爷', 2, 0);
INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES (2611, 702, 0, NULL, '奶奶', 3, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_question
-- ----------------------------
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   试题表',
  `qu_type` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '试题类型',
  `image` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '试题图片',
  `content` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '题干',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `analysis` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '题目分析',
  `repo_id` int(11) DEFAULT NULL COMMENT '题库id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=703 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_question
-- ----------------------------
BEGIN;
INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `user_id`, `is_deleted`) VALUES (702, '1', NULL, '爸爸的爸爸叫什么？', '2025-03-12 21:40:07', NULL, 74, 148, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_repo
-- ----------------------------
DROP TABLE IF EXISTS `t_repo`;
CREATE TABLE `t_repo` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   题库表',
  `user_id` int(11) NOT NULL COMMENT '创建人id',
  `title` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '题库标题',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_repo
-- ----------------------------
BEGIN;
INSERT INTO `t_repo` (`id`, `user_id`, `title`, `create_time`, `is_deleted`) VALUES (74, 148, '语文题库', '2025-03-12 21:39:37', 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=150 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
BEGIN;
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (1, 'admin', '管理员', '$2a$10$/ZdKFY15AWNLOeTqAp91a.uDa0JDioj1wVYGgpn.HKMYh9vq0Uh4S', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/27eb3a59-4d05-4bce-90ec-4a2457452886.png', 3, 106, '2024-05-23 16:06:07', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (148, 'teacher', '教师账号', '$2a$10$.57mfw2AwrkTgJzPembI9eqtwIXEhXEG6G8rdBwWR7upQVcfxEdxe', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 2, NULL, '2025-03-12 21:27:37', 1, 0);
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES (149, 'student', '学生账号', '$2a$10$RE1t2v5a5BdpWKWMTPnRCud.GYIdOmYkZweEoGZqLUOI1SRB7MG4i', 'https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg', 1, 118, '2025-03-12 21:30:25', 1, 0);
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
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_book
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_user_daily_login_duration
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB AUTO_INCREMENT=195 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exams_score
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_user_exercise_record
-- ----------------------------
BEGIN;
INSERT INTO `t_user_exercise_record` (`id`, `user_id`, `repo_id`, `total_count`, `exercise_count`, `create_time`) VALUES (27, 149, 74, 1, 1, '2025-03-12 21:43:06');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
