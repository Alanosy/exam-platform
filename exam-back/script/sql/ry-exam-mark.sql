-- ----------------------------
-- 阅卷模块表结构（考试主库 ry-exam）
--
-- 说明：ruoyi-exam-mark 服务的数据源指向考试主库（见 script/config/nacos/ruoyi-exam-mark.yml），
--       答卷明细在 ry-exam-answer 库，跨库一律走 Dubbo，不做直连。
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='阅卷任务表（一份答卷一条任务）';

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
  -- AI 阅卷预留：接 ruoyi-exam-ai 后由 MarkAiService 写入，教师确认后落到 score
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='阅卷明细表（一道主观题一条）';

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
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='阅卷操作日志';

SET FOREIGN_KEY_CHECKS = 1;
