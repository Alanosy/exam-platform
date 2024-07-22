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

 Date: 22/07/2024 11:45:24
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

SET FOREIGN_KEY_CHECKS = 1;
