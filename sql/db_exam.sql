/*
 在线考试系统 - 完整数据库脚本（结构 + 基础种子数据）

 库名: db_exam
 字符集: utf8mb4 / utf8mb4_bin
 兼容: MySQL 5.7+ / 8.0+

 更新说明（相对旧版）:
 1. t_question 增加 sort / level / audio
 2. t_repo 增加 category_id / is_exercise
 3. t_user_grade 支持学生/教师多班级关联
 4. t_grade_exercise 增加 uk_repo_grade
 5. 补齐高频查询性能索引（考试、成绩、作答、班级、错题本等）
 6. 删除考试改为物理级联清理关联数据（见后端 ExamServiceImpl）

 默认账号（密码均为 123456）:
   admin / teacher / student

 Date: 2026-09-24 15:47:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `db_exam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
USE `db_exam`;


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_cu_user` (`user_id`) USING BTREE,
  KEY `idx_cu_exam` (`exam_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_discussion
-- ----------------------------
DROP TABLE IF EXISTS `t_discussion`;
CREATE TABLE `t_discussion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '发布人id',
  `grade_id` int(11) NOT NULL COMMENT '接收班级id',
  `content` text COLLATE utf8mb4_bin NOT NULL COMMENT '内容',
  `title` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `create_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_disc_grade` (`grade_id`) USING BTREE,
  KEY `idx_disc_user` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  `fill_count` int(11) DEFAULT 0 COMMENT '填空题数量',
  `fill_score` int(11) DEFAULT 0 COMMENT '填空题单题默认分 数据库存储*100，前端正常输入和展示/100',
  `fill_need_mark` tinyint(4) NOT NULL DEFAULT 0 COMMENT '填空题是否二次人工阅卷：0否 1是',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间     YYYY-MM-DD hh:mm:ss',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间     YYYY-MM-DD hh:mm:ss',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间     YYYY-MM-DD hh:mm:ss  ',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`,`passed_score`) USING BTREE,
  KEY `idx_exam_user_del` (`user_id`,`is_deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_exam_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_grade`;
CREATE TABLE `t_exam_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   考试与班级关系表',
  `exam_id` int(11) DEFAULT NULL COMMENT '考试id',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_eg_exam_id` (`exam_id`) USING BTREE,
  KEY `idx_eg_grade_id` (`grade_id`) USING BTREE,
  KEY `idx_eg_exam_grade` (`exam_id`,`grade_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  `answer_content` varchar(2000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '答案内容 主观题/填空题(多空用|||分隔)',
  `checkout` int(11) DEFAULT NULL COMMENT '是否选中   0未选中  1选中',
  `is_sign` int(11) DEFAULT NULL COMMENT '是否标记   0未标记  1标记',
  `is_right` int(11) DEFAULT NULL COMMENT '是否正确   用于客观题，0错误 1正确',
  `ai_score` int(11) DEFAULT NULL COMMENT 'ai评分',
  `ai_reason` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'ai评分原因',
  `earned_score` int(11) DEFAULT NULL COMMENT '填空题自动实得分（按空给分）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`,`question_id`) USING BTREE,
  KEY `idx_eqa_exam_qid` (`exam_id`,`question_id`) USING BTREE,
  KEY `idx_eqa_exam_user_type` (`exam_id`,`user_id`,`question_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_eq_exam_type` (`exam_id`,`type`) USING BTREE,
  KEY `idx_eq_exam_qid` (`exam_id`,`question_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_exam_repo
-- ----------------------------
DROP TABLE IF EXISTS `t_exam_repo`;
CREATE TABLE `t_exam_repo` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id  考试与题库',
  `exam_id` int(11) NOT NULL COMMENT '考试id',
  `repo_id` int(11) DEFAULT NULL COMMENT '题库id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_er_exam_id` (`exam_id`) USING BTREE,
  KEY `idx_er_repo_id` (`repo_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  UNIQUE KEY `repo_id` (`repo_id`,`question_id`,`user_id`) USING BTREE,
  KEY `idx_er_user_repo` (`user_id`,`repo_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_grade`;
CREATE TABLE `t_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id   班级表',
  `grade_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '班级名称',
  `user_id` int(11) DEFAULT NULL COMMENT '创建人id',
  `code` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '班级口令',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '显示排序，越小越靠前',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `grade_name` (`grade_name`) USING BTREE,
  UNIQUE KEY `code` (`code`) USING BTREE,
  KEY `idx_grade_user_id` (`user_id`) USING BTREE,
  KEY `idx_grade_sort` (`sort`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_repo_grade` (`repo_id`,`grade_id`) USING BTREE,
  KEY `idx_grade_id` (`grade_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  UNIQUE KEY `discussion_id` (`discussion_id`,`reply_id`,`user_id`) USING BTREE,
  KEY `idx_like_reply` (`reply_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_log
-- ----------------------------
DROP TABLE IF EXISTS `t_log`;
CREATE TABLE `t_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `place` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '登录地点',
  `behavior` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '操作行为',
  `device` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '登录设备',
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ms_answer` (`exam_qu_answer_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   公告表',
  `title` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '公告标题',
  `image` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '图片地址',
  `content` text COLLATE utf8mb4_bin NOT NULL COMMENT '公告内容',
  `user_id` int(11) DEFAULT NULL COMMENT '创建用户ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  `is_public` int(11) DEFAULT NULL COMMENT '是否公开所有用户，老师为所有班级，管理员为所有用户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_notice_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_grade`;
CREATE TABLE `t_notice_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   公告班级关联表',
  `notice_id` int(11) DEFAULT NULL COMMENT '公告id',
  `grade_id` int(11) DEFAULT NULL COMMENT '班级',
  `is_deleted` int(11) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ng_grade` (`grade_id`) USING BTREE,
  KEY `idx_ng_notice` (`notice_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_option_qu_id` (`qu_id`) USING BTREE,
  KEY `idx_option_qu_right` (`qu_id`,`is_right`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '题库内排序，越小越靠前',
  `level` int(11) NOT NULL DEFAULT '3' COMMENT '难度1-5，星级',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  `audio` text COLLATE utf8mb4_bin COMMENT '试题音频，JSON数组',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_qu_repo_del` (`repo_id`,`is_deleted`) USING BTREE,
  KEY `idx_qu_repo_type_del` (`repo_id`,`qu_type`,`is_deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_reply
-- ----------------------------
DROP TABLE IF EXISTS `t_reply`;
CREATE TABLE `t_reply` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `discussion_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `parent_id` int(11) DEFAULT NULL COMMENT '父评论id',
  `content` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_reply_disc_parent` (`discussion_id`,`parent_id`) USING BTREE,
  KEY `idx_reply_parent` (`parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  `is_exercise` int(11) NOT NULL DEFAULT '0' COMMENT '是否开启刷题 0否 1是',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '显示排序，越小越靠前',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_repo_user_del` (`user_id`,`is_deleted`) USING BTREE,
  KEY `idx_repo_category` (`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID  角色表',
  `role_name` varchar(10) COLLATE utf8mb4_bin NOT NULL COMMENT '角色名称',
  `code` varchar(10) COLLATE utf8mb4_bin NOT NULL COMMENT '角色编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
  `real_name` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '真实姓名',
  `password` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '头像地址，空表示使用角色默认头像',
  `role_id` int(11) DEFAULT '1' COMMENT '角色id',
  `grade_id` int(11) DEFAULT NULL COMMENT '主班级id（兼容字段，多班级以 t_user_grade 为准）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间   YYYY-MM-DD hh:mm:ss',
  `status` int(11) DEFAULT '1' COMMENT '状态  1正常0禁用',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_name` (`user_name`) USING BTREE,
  KEY `idx_user_grade_del` (`grade_id`,`is_deleted`) USING BTREE,
  KEY `idx_user_role` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_user_book
-- ----------------------------
DROP TABLE IF EXISTS `t_user_book`;
CREATE TABLE `t_user_book` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID   错题本',
  `exam_id` int(11) DEFAULT NULL COMMENT '考试id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `qu_id` int(11) DEFAULT NULL COMMENT '题目id',
  `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间    YYYY-MM-DD hh:mm:ss',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ub_user_exam` (`user_id`,`exam_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_user_daily_login_duration
-- ----------------------------
DROP TABLE IF EXISTS `t_user_daily_login_duration`;
CREATE TABLE `t_user_daily_login_duration` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键，自增长',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID，与用户表关联，确保数据一致性',
  `login_date` date DEFAULT NULL COMMENT '登录日期，记录用户在哪一天的登录时长',
  `total_seconds` int(11) DEFAULT NULL COMMENT '累积在线秒数，每日登录总时长，单位为秒',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_udld_user_date` (`user_id`,`login_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_user_exams_score
-- ----------------------------
DROP TABLE IF EXISTS `t_user_exams_score`;
CREATE TABLE `t_user_exams_score` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id  用户考试成绩表',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `exam_id` int(11) DEFAULT NULL COMMENT '试卷id',
  `total_time` int(11) DEFAULT NULL COMMENT '考试总时长（分钟）',
  `user_time` int(11) DEFAULT NULL COMMENT '用户用时（秒）',
  `user_score` int(10) unsigned DEFAULT '0' COMMENT '用户得分',
  `limit_time` datetime DEFAULT NULL COMMENT '交卷时间  YYYY-MM-DD hh:mm:ss',
  `count` int(11) DEFAULT '0' COMMENT '切屏次数',
  `state` int(11) DEFAULT NULL COMMENT '状态   0正在考试 1考试完成',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间  YYYY-MM-DD hh:mm:ss',
  `whether_mark` int(11) DEFAULT NULL COMMENT '是否阅卷 -1无简答题、0未阅卷、1已阅卷',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`) USING BTREE,
  KEY `idx_ues_exam_mark` (`exam_id`,`whether_mark`) USING BTREE,
  KEY `idx_ues_exam_state` (`exam_id`,`state`) USING BTREE,
  KEY `idx_ues_user_state` (`user_id`,`state`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_uer_user_repo` (`user_id`,`repo_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_user_grade
-- ----------------------------
DROP TABLE IF EXISTS `t_user_grade`;
CREATE TABLE `t_user_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `u_id` int(11) DEFAULT NULL COMMENT '用户id（学生或教师）',
  `g_id` int(11) DEFAULT NULL COMMENT '班级id',
  `is_deleted` int(11) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `u_id` (`u_id`,`g_id`) USING BTREE,
  KEY `idx_ug_g_id` (`g_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_role
-- ----------------------------
BEGIN;
INSERT INTO `t_role` (`id`, `role_name`, `code`) VALUES (1, '学生', 'student');
INSERT INTO `t_role` (`id`, `role_name`, `code`) VALUES (2, '教师', 'teacher');
INSERT INTO `t_role` (`id`, `role_name`, `code`) VALUES (3, '管理员', 'admin');
COMMIT;

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
-- Records of t_grade
-- ----------------------------
BEGIN;
INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `sort`, `is_deleted`) VALUES (1, '演示班级', 1, 'DemoClassToken001', 1, 0);
COMMIT;

-- ----------------------------
-- Records of t_user
-- 密码均为 BCrypt(123456)
-- ----------------------------
BEGIN;
INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES
(1, 'admin', '管理员', '$2a$10$/ZdKFY15AWNLOeTqAp91a.uDa0JDioj1wVYGgpn.HKMYh9vq0Uh4S', '', 3, NULL, '2024-05-23 16:06:07', 1, 0),
(2, 'teacher', '教师测试账号', '$2a$10$odROzQ2vFaHRomD9UG4uWO65twiqI33Y1RktzqtxWNLaVf5.luivy', '', 2, NULL, '2025-03-22 11:38:18', 1, 0),
(3, 'student', '学生测试账号', '$2a$10$vcnsGkVJdeH0tdKiLa1.d.qbPD/.5B5Ah3qYzeN6rem5P/U8jGiMS', '', 1, 1, '2025-03-22 11:38:36', 1, 0);
COMMIT;

-- ----------------------------
-- Records of t_user_grade（教师/学生多班级关联）
-- ----------------------------
BEGIN;
INSERT INTO `t_user_grade` (`id`, `u_id`, `g_id`, `is_deleted`) VALUES (1, 2, 1, 0);
INSERT INTO `t_user_grade` (`id`, `u_id`, `g_id`, `is_deleted`) VALUES (2, 3, 1, 0);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
