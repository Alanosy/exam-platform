/*
 Navicat Premium Data Transfer

 Source Server         : dcLocalhost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : ry-exam

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 04/10/2026 15:25:46
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
  `start_time` datetime DEFAULT NULL COMMENT '考试开始时间，练习考试可为空表示长期有效',
  `end_time` datetime DEFAULT NULL COMMENT '考试结束时间，练习考试可为空表示长期有效',
  `duration` int DEFAULT '0' COMMENT '本场考试限时(分钟)，0沿用试卷time_limit',
  `allow_late` tinyint NOT NULL DEFAULT '0' COMMENT '是否允许迟到入场 0否 1是',
  `late_minute` int DEFAULT '0' COMMENT '允许迟到多少分钟，超过无法进入',
  `allow_retry` tinyint NOT NULL DEFAULT '0' COMMENT '是否允许重考 0否 1是',
  `max_retry_count` int DEFAULT '1' COMMENT '单个考生最大重考次数',
  `show_answer_mode` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'none' COMMENT '答案展示 none不展示 / after_submit交卷后 / after_exam考试结束',
  `partial_score` char(1) NOT NULL DEFAULT '0' COMMENT '客观题部分得分开关 0必须全对 1启用部分得分',
  `partial_score_rate` int NOT NULL DEFAULT '100' COMMENT '部分正确的得分比例(%) 100按命中比例 50一律半数',
  `anti_cheat_config` json DEFAULT NULL COMMENT '防作弊配置：切屏次数、禁止复制粘贴、摄像头抓拍、全屏限制等',
  `cert_id` bigint DEFAULT NULL COMMENT '及格证书模板ID，为空表示本场考试不颁发证书',
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
) ENGINE=InnoDB AUTO_INCREMENT=2105692623289184258 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试主表';

-- ----------------------------
-- Table structure for exam_certificate
-- ----------------------------
DROP TABLE IF EXISTS `exam_certificate`;
CREATE TABLE `exam_certificate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cert_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '证书名称（管理用，如「前端工程师认证证书」）',
  `cert_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证书编码（业务唯一标识，用于证书编号前缀与外部系统对接）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '证书大标题（证书正面居中大字，如「结业证书」）',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证书副标题（标题下方小字，如英文标题）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '证书正文模板，支持占位符 {nickName} {realName} {account} {examName} {score} {totalScore} {passScore} {certNo} {issueDate} {expireDate}',
  `issuer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '发证机构 / 签发人（证书右下角落款）',
  `seal_oss_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '印章图片 ossId（存 OSS，取地址时再换）',
  `bg_oss_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证书背景图 ossId，为空则用 bg_color 纯色',
  `bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '#fdfaf3' COMMENT '证书背景色（无背景图时生效）',
  `orientation` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '版式 0横版 1竖版',
  `valid_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '有效期类型 0永久有效 1按天计算',
  `valid_days` int NOT NULL DEFAULT '0' COMMENT '有效期天数（valid_type=1 时生效，从颁发日起算）',
  `issue_count` int NOT NULL DEFAULT '0' COMMENT '已颁发数量（冗余计数，列表页直接展示）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态 0启用 1停用（停用后不再自动颁发）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志 0存在 1删除',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_cert_code` (`cert_code`,`del_flag`) USING BTREE,
  KEY `idx_cert_name` (`cert_name`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105862469964505091 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='证书模板';

-- ----------------------------
-- Table structure for exam_certificate_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_certificate_record`;
CREATE TABLE `exam_certificate_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cert_id` bigint DEFAULT NULL COMMENT '证书模板ID（模板被删仍保留这里，只为溯源）',
  `cert_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '证书编号（全局唯一，对外核验用）',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `exam_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '考试名称快照',
  `record_id` bigint DEFAULT NULL COMMENT '答卷记录ID（答题库 exam_record 主键，跨库只存ID）',
  `user_id` bigint DEFAULT NULL COMMENT '考生用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '考生账号（登录名）快照',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '考生姓名快照',
  `attempt_no` int DEFAULT '1' COMMENT '第几次参加（快照）',
  `score` decimal(10,2) DEFAULT '0.00' COMMENT '考生得分（快照）',
  `pass_score` decimal(10,2) DEFAULT '0.00' COMMENT '及格分（快照）',
  `total_score` decimal(10,2) DEFAULT '0.00' COMMENT '试卷总分（快照）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证书大标题（颁发时从模板快照）',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证书副标题（快照）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '证书正文（占位符已替换成真实内容，快照）',
  `issuer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '发证机构（快照）',
  `seal_oss_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '印章图片 ossId（快照）',
  `bg_oss_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '背景图 ossId（快照）',
  `bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '#fdfaf3' COMMENT '背景色（快照）',
  `orientation` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '版式 0横版 1竖版（快照）',
  `issue_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '颁发方式 0及格自动颁发 1手动补发',
  `issue_time` datetime DEFAULT NULL COMMENT '颁发时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间（永久有效为 NULL）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态 0有效 1已吊销',
  `revoke_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '吊销原因（作弊、考试作废等）',
  `revoke_time` datetime DEFAULT NULL COMMENT '吊销时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志 0存在 1删除',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_cert_no` (`cert_no`) USING BTREE,
  UNIQUE KEY `uk_exam_record` (`exam_id`,`record_id`,`del_flag`) USING BTREE,
  KEY `idx_user` (`user_id`) USING BTREE,
  KEY `idx_exam_status` (`exam_id`,`status`) USING BTREE,
  KEY `idx_cert` (`cert_id`) USING BTREE,
  KEY `idx_issue_time` (`issue_time`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='证书颁发记录';

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
) ENGINE=InnoDB AUTO_INCREMENT=2105640241431781378 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考试邀请记录表';

-- ----------------------------
-- Table structure for exam_knowledge_point
-- ----------------------------
DROP TABLE IF EXISTS `exam_knowledge_point`;
CREATE TABLE `exam_knowledge_point` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父级ID，0 表示章节（根节点）',
  `name` varchar(100) NOT NULL COMMENT '知识点名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_parent` (`parent_id`),
  KEY `idx_knowledge_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点（两级：章节 / 知识点）';

-- ----------------------------
-- Table structure for exam_mark_item
-- ----------------------------
DROP TABLE IF EXISTS `exam_mark_item`;
CREATE TABLE `exam_mark_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '阅卷任务ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID，冗余便于按考试统计',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `record_id` bigint NOT NULL COMMENT '答卷记录ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `question_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '题型，冗余便于统计',
  `sort` int NOT NULL DEFAULT '0' COMMENT '题号顺序，与答卷一致',
  `full_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '本题满分（取自试卷配置，缺则取题目分值）',
  `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '考生作答快照，阅卷时不再回查答题库',
  `score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最终得分',
  `correct` tinyint NOT NULL DEFAULT '0' COMMENT '0未判 1正确 2错误',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT 'pending待阅 / marked已阅',
  `mark_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '定稿方式 manual人工 / ai智能预评后确认 / auto自动判分',
  `mark_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '阅卷评语',
  `marker` bigint DEFAULT NULL COMMENT '阅卷人ID',
  `marker_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '阅卷人姓名',
  `mark_time` datetime DEFAULT NULL COMMENT '阅卷时间',
  `ai_score` decimal(10,2) DEFAULT NULL COMMENT 'AI 建议分',
  `ai_reason` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'AI 评分理由',
  `ai_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'none' COMMENT 'none未调用 / running评估中 / success成功 / fail失败',
  `ai_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'AI 模型标识',
  `ai_time` datetime DEFAULT NULL COMMENT 'AI 评估时间',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_task_question` (`task_id`,`question_id`,`del_flag`) USING BTREE,
  KEY `idx_record` (`record_id`,`del_flag`) USING BTREE,
  KEY `idx_exam_status` (`exam_id`,`status`,`del_flag`) USING BTREE,
  KEY `idx_question` (`question_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105705356273373186 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='阅卷明细表（一道主观题一条）';

-- ----------------------------
-- Table structure for exam_mark_log
-- ----------------------------
DROP TABLE IF EXISTS `exam_mark_log`;
CREATE TABLE `exam_mark_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '阅卷任务ID',
  `item_id` bigint DEFAULT NULL COMMENT '阅卷明细ID，任务级操作为空',
  `record_id` bigint NOT NULL COMMENT '答卷记录ID',
  `question_id` bigint DEFAULT NULL COMMENT '试题ID，任务级操作为空',
  `action` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'create建任务 / score打分 / rescore改分 / ai预评 / finish完成阅卷',
  `old_score` decimal(10,2) DEFAULT NULL COMMENT '改动前得分',
  `new_score` decimal(10,2) DEFAULT NULL COMMENT '改动后得分',
  `mark_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '打标方式 manual / ai / auto',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `operator` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作人姓名',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task` (`task_id`) USING BTREE,
  KEY `idx_record` (`record_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2106316405053784066 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='阅卷操作日志';

-- ----------------------------
-- Table structure for exam_mark_task
-- ----------------------------
DROP TABLE IF EXISTS `exam_mark_task`;
CREATE TABLE `exam_mark_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `record_id` bigint NOT NULL COMMENT '答卷记录ID（答题库 exam_record 主键，跨库只存ID）',
  `user_id` bigint DEFAULT NULL COMMENT '考生用户ID',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '考生账号（登录名）',
  `attempt_no` int NOT NULL DEFAULT '1' COMMENT '第几次参加',
  `question_count` int NOT NULL DEFAULT '0' COMMENT '主观题总题数',
  `marked_count` int NOT NULL DEFAULT '0' COMMENT '已阅题数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT 'pending待阅 / marking阅卷中 / finished已阅完',
  `objective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '客观题得分（开卷快照，阅卷时不改）',
  `subjective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '主观题得分（阅卷累加）',
  `total_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总分',
  `pass_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '及格分（开卷快照）',
  `passed` tinyint NOT NULL DEFAULT '0' COMMENT '是否及格 0否 1是',
  `submit_time` datetime DEFAULT NULL COMMENT '考生交卷时间快照',
  `marker` bigint DEFAULT NULL COMMENT '最近一次阅卷人ID',
  `marker_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '最近一次阅卷人姓名',
  `mark_time` datetime DEFAULT NULL COMMENT '最近一次阅卷时间',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_record` (`record_id`,`del_flag`) USING BTREE,
  KEY `idx_exam_status` (`exam_id`,`status`,`del_flag`) USING BTREE,
  KEY `idx_paper_id` (`paper_id`) USING BTREE,
  KEY `idx_marker` (`marker`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105705355212214275 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='阅卷任务表（一份答卷一条任务）';

-- ----------------------------
-- Table structure for exam_proctor_event
-- ----------------------------
DROP TABLE IF EXISTS `exam_proctor_event`;
CREATE TABLE `exam_proctor_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '监考会话ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `record_id` bigint DEFAULT NULL COMMENT '答卷记录ID',
  `user_id` bigint DEFAULT NULL COMMENT '考生用户ID',
  `event_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '事件类型 switch_screen / blur / copy / paste / cut / contextmenu / exit_fullscreen / camera / camera_deny / devtool / multitab / force_submit',
  `event_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '事件名称（中文，列表直接展示）',
  `level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'info' COMMENT '级别 info提示 / warn可疑 / danger严重',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '事件摘要，如被粘贴内容的前 200 字',
  `extra` text COMMENT '扩展信息（键位、屏幕尺寸、抓拍 ossId 等）',
  `event_time` datetime DEFAULT NULL COMMENT '事件发生的客户端时间',
  `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  PRIMARY KEY (`id`),
  KEY `idx_session_time` (`session_id`,`event_time`),
  KEY `idx_exam_time` (`exam_id`,`event_time`),
  KEY `idx_exam_type` (`exam_id`,`event_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2105944652989288450 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='防作弊事件流水';

-- ----------------------------
-- Table structure for exam_proctor_session
-- ----------------------------
DROP TABLE IF EXISTS `exam_proctor_session`;
CREATE TABLE `exam_proctor_session` (
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
  `exam_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_record` (`exam_id`,`record_id`,`del_flag`) COMMENT '一份答卷只有一条监考会话',
  KEY `idx_exam_status` (`exam_id`,`status`),
  KEY `idx_exam_risk` (`exam_id`,`risk_level`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2105944581212164098 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监考会话（一场考试 × 一份答卷）';

-- ----------------------------
-- Table structure for exam_proctor_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `exam_proctor_snapshot`;
CREATE TABLE `exam_proctor_snapshot` (
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
  KEY `idx_session_time` (`session_id`,`capture_time`),
  KEY `idx_exam_time` (`exam_id`,`capture_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2105706418845671426 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='摄像头抓拍';

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
-- Table structure for exam_question_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `exam_question_knowledge`;
CREATE TABLE `exam_question_knowledge` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `knowledge_id` bigint NOT NULL COMMENT '知识点ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_dept` varchar(64) DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_knowledge` (`question_id`,`knowledge_id`),
  KEY `idx_qk_knowledge` (`knowledge_id`),
  KEY `idx_qk_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='试题-知识点关联';

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
  `time_limit` int DEFAULT '0' COMMENT '考试时长(分钟)，0代表不限时',
  `visibility` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'private' COMMENT '可见性 private私有 / public公开',
  `share_password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公开分享密码，公开模式生效，空则无密码',
  `share_expire_time` datetime DEFAULT NULL COMMENT '分享链接过期时间，NULL永久有效',
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
) ENGINE=InnoDB AUTO_INCREMENT=2105487608142360579 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试卷主表';

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
) ENGINE=InnoDB AUTO_INCREMENT=2105487610499559426 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试卷-试题中间表';

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
) ENGINE=InnoDB AUTO_INCREMENT=2106633095260086275 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试题主表';

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
) ENGINE=InnoDB AUTO_INCREMENT=2105486630382866435 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题库表';

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
) ENGINE=InnoDB AUTO_INCREMENT=2106633097092997122 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='试题选项表';

-- ----------------------------
-- Table structure for stat_calc_task
-- ----------------------------
DROP TABLE IF EXISTS `stat_calc_task`;
CREATE TABLE `stat_calc_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `trigger_type` varchar(20) NOT NULL DEFAULT 'auto' COMMENT 'auto交卷 mark阅卷 job定时 manual手动',
  `calc_type` varchar(20) NOT NULL DEFAULT 'full' COMMENT 'full全量 incr增量',
  `status` varchar(20) NOT NULL DEFAULT 'running' COMMENT 'running成功中 success成功 fail失败',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `cost_ms` bigint DEFAULT NULL COMMENT '耗时毫秒',
  `error_msg` varchar(1000) DEFAULT '' COMMENT '错误信息',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统计计算任务';

-- ----------------------------
-- Table structure for stat_exam_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `stat_exam_knowledge`;
CREATE TABLE `stat_exam_knowledge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `knowledge_id` bigint NOT NULL COMMENT '知识点ID(一期=题库分类ID)',
  `knowledge_name` varchar(200) DEFAULT '' COMMENT '知识点名称',
  `knowledge_path` varchar(500) DEFAULT '' COMMENT '知识点全路径',
  `question_count` int NOT NULL DEFAULT '0' COMMENT '题目数',
  `full_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '知识点总分',
  `avg_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '平均得分',
  `score_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '得分率%',
  `wrong_count` int NOT NULL DEFAULT '0' COMMENT '错误人次',
  `wrong_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '错误率%',
  `mastery` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '掌握度%',
  `weak_level` varchar(20) DEFAULT '' COMMENT 'good良好 normal一般 weak薄弱',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_knowledge` (`exam_id`,`knowledge_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试知识点统计';

-- ----------------------------
-- Table structure for stat_exam_question
-- ----------------------------
DROP TABLE IF EXISTS `stat_exam_question`;
CREATE TABLE `stat_exam_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `question_type` varchar(30) DEFAULT '' COMMENT '题型',
  `question_category` varchar(20) DEFAULT '' COMMENT 'objective客观 subjective主观',
  `difficulty` varchar(20) DEFAULT '' COMMENT '难度',
  `sort` int NOT NULL DEFAULT '0' COMMENT '题号',
  `full_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '本题满分',
  `answer_count` int NOT NULL DEFAULT '0' COMMENT '作答人数',
  `blank_count` int NOT NULL DEFAULT '0' COMMENT '未作答人数',
  `correct_count` int NOT NULL DEFAULT '0' COMMENT '完全答对人数',
  `correct_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '正确率%',
  `partial_count` int NOT NULL DEFAULT '0' COMMENT '部分正确人数(部分得分模式)',
  `wrong_count` int NOT NULL DEFAULT '0' COMMENT '答错人数',
  `avg_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '平均得分',
  `score_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '得分率%',
  `max_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最高得分',
  `min_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最低得分',
  `zero_count` int NOT NULL DEFAULT '0' COMMENT '零分人数',
  `full_count` int NOT NULL DEFAULT '0' COMMENT '满分人数',
  `difficulty_index` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '难度系数',
  `discrimination` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '区分度',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_question` (`exam_id`,`question_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试试题统计';

-- ----------------------------
-- Table structure for stat_exam_question_option
-- ----------------------------
DROP TABLE IF EXISTS `stat_exam_question_option`;
CREATE TABLE `stat_exam_question_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `option_key` varchar(10) NOT NULL DEFAULT '' COMMENT '选项标识 A/B/C/D',
  `option_content` varchar(500) DEFAULT '' COMMENT '选项内容',
  `select_count` int NOT NULL DEFAULT '0' COMMENT '选中人次',
  `select_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '选中率%',
  `is_correct` char(1) NOT NULL DEFAULT '0' COMMENT '是否正确选项 0否 1是',
  `trap` tinyint NOT NULL DEFAULT '0' COMMENT '易错项 0否 1是(非正确项但选中率超三成)',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_question_option` (`exam_id`,`question_id`,`option_key`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试试题选项分布';

-- ----------------------------
-- Table structure for stat_exam_score_segment
-- ----------------------------
DROP TABLE IF EXISTS `stat_exam_score_segment`;
CREATE TABLE `stat_exam_score_segment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `segment_label` varchar(30) NOT NULL DEFAULT '' COMMENT '分段名 如 60-69',
  `segment_min` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '下限(含)',
  `segment_max` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '上限(不含,末段含)',
  `person_count` int NOT NULL DEFAULT '0' COMMENT '人数',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105891828813148163 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试分数段分布';

-- ----------------------------
-- Table structure for stat_exam_summary
-- ----------------------------
DROP TABLE IF EXISTS `stat_exam_summary`;
CREATE TABLE `stat_exam_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `paper_id` bigint DEFAULT NULL COMMENT '试卷ID快照',
  `exam_name` varchar(200) DEFAULT '' COMMENT '考试名称快照',
  `invited_count` int NOT NULL DEFAULT '0' COMMENT '应考人数',
  `submitted_count` int NOT NULL DEFAULT '0' COMMENT '已交卷数',
  `counted_count` int NOT NULL DEFAULT '0' COMMENT '已入统数',
  `pending_mark_count` int NOT NULL DEFAULT '0' COMMENT '待阅卷数',
  `excluded_count` int NOT NULL DEFAULT '0' COMMENT '作废数',
  `attendance_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '参考率%',
  `full_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '试卷总分',
  `pass_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '及格分',
  `max_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最高分',
  `min_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最低分',
  `avg_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '平均分',
  `median_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '中位数',
  `std_dev` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '标准差',
  `pass_count` int NOT NULL DEFAULT '0' COMMENT '及格人数',
  `pass_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '及格率%',
  `excellent_count` int NOT NULL DEFAULT '0' COMMENT '优秀人数',
  `excellent_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '优秀率%',
  `avg_used_seconds` int NOT NULL DEFAULT '0' COMMENT '平均用时(秒)',
  `avg_objective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '客观题平均分',
  `avg_subjective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '主观题平均分',
  `difficulty` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '难度系数',
  `discrimination` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '区分度',
  `has_subjective` char(1) NOT NULL DEFAULT '0' COMMENT '是否含主观题 0否 1是',
  `partial_score` char(1) NOT NULL DEFAULT '0' COMMENT '客观题部分得分开关快照 0必须全对 1部分得分',
  `partial_score_rate` int NOT NULL DEFAULT '100' COMMENT '部分正确得分比例快照',
  `calc_status` varchar(20) NOT NULL DEFAULT 'success' COMMENT 'computing计算中 success成功 fail失败',
  `calc_version` int NOT NULL DEFAULT '0' COMMENT '计算版本号',
  `calc_time` datetime DEFAULT NULL COMMENT '最后计算时间',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 2已删',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam` (`exam_id`,`del_flag`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2105891827814903810 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试统计汇总';

-- ----------------------------
-- Table structure for stat_exam_user
-- ----------------------------
DROP TABLE IF EXISTS `stat_exam_user`;
CREATE TABLE `stat_exam_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `user_id` bigint NOT NULL COMMENT '考生用户ID',
  `record_id` bigint DEFAULT NULL COMMENT '答卷记录ID',
  `attempt_no` int NOT NULL DEFAULT '1' COMMENT '第几次参加',
  `account` varchar(100) DEFAULT '' COMMENT '考生账号',
  `user_name` varchar(50) DEFAULT '' COMMENT '考生姓名快照',
  `dept_name` varchar(100) DEFAULT '' COMMENT '部门名快照',
  `total_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总分',
  `objective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '客观题得分',
  `subjective_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '主观题得分',
  `correct_count` int NOT NULL DEFAULT '0' COMMENT '答对题数',
  `wrong_count` int NOT NULL DEFAULT '0' COMMENT '答错题数',
  `blank_count` int NOT NULL DEFAULT '0' COMMENT '未答题数',
  `passed` tinyint NOT NULL DEFAULT '0' COMMENT '是否及格 0否 1是',
  `rank_no` int NOT NULL DEFAULT '0' COMMENT '排名',
  `used_seconds` int NOT NULL DEFAULT '0' COMMENT '用时(秒)',
  `submit_time` datetime DEFAULT NULL COMMENT '交卷时间',
  `stat_status` varchar(20) NOT NULL DEFAULT 'COUNTED' COMMENT 'COUNTED已入统 PENDING_MARK待阅 EXCLUDED作废',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_user_attempt` (`exam_id`,`user_id`,`attempt_no`,`del_flag`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试考生统计';

-- ----------------------------
-- Table structure for stat_export_task
-- ----------------------------
DROP TABLE IF EXISTS `stat_export_task`;
CREATE TABLE `stat_export_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint DEFAULT NULL COMMENT '考试ID',
  `export_type` varchar(20) NOT NULL DEFAULT '' COMMENT 'overview汇总 score成绩单 question题目 knowledge知识点 detail明细',
  `param_json` varchar(2000) DEFAULT '' COMMENT '筛选条件快照',
  `status` varchar(20) NOT NULL DEFAULT 'waiting' COMMENT 'waiting/running/success/fail',
  `file_oss_id` varchar(200) DEFAULT '' COMMENT '文件OSS标识',
  `file_name` varchar(200) DEFAULT '' COMMENT '文件名',
  `row_count` int NOT NULL DEFAULT '0' COMMENT '导出行数',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `error_msg` varchar(1000) DEFAULT '' COMMENT '错误信息',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_dept` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统计导出任务';

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
) ENGINE=InnoDB AUTO_INCREMENT=2105488043762761731 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户错题集主表';

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
) ENGINE=InnoDB AUTO_INCREMENT=2105952220801331202 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题复习作答记录';

SET FOREIGN_KEY_CHECKS = 1;
