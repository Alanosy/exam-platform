-- 考试级「部分得分」配置 + 统计模块建表
-- 幂等脚本：可重复执行
-- 库：ry-exam

-- ----------------------------
-- 1. exam 增加客观题部分得分配置
-- ----------------------------
-- partial_score：客观题（多选漏选 / 填空只对部分空）是否给部分分
--   0 = 必须完全答对才给分（默认，与历史行为一致）
--   1 = 启用部分得分
-- partial_score_rate：部分正确时的得分比例（%），
--   100 = 按命中比例给分（对一半给一半）
--   50  = 只要不是全对，一律给该题满分的 50%
-- 说明：多选题只要选中了任何一个错误选项，一律 0 分（防止「全选蒙满分」）。
SET @exist := (SELECT COUNT(1) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam' AND COLUMN_NAME = 'partial_score');
SET @sql := IF(@exist = 0,
    "ALTER TABLE `exam` ADD COLUMN `partial_score` char(1) NOT NULL DEFAULT '0' COMMENT '客观题部分得分开关 0必须全对 1启用部分得分' AFTER `show_answer_mode`",
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(1) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam' AND COLUMN_NAME = 'partial_score_rate');
SET @sql := IF(@exist = 0,
    "ALTER TABLE `exam` ADD COLUMN `partial_score_rate` int NOT NULL DEFAULT 100 COMMENT '部分正确的得分比例(%) 100按命中比例 50一律半数' AFTER `partial_score`",
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 2. 考试整体汇总
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_exam_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `paper_id` bigint DEFAULT NULL COMMENT '试卷ID快照',
  `exam_name` varchar(200) DEFAULT '' COMMENT '考试名称快照',
  `invited_count` int NOT NULL DEFAULT 0 COMMENT '应考人数',
  `submitted_count` int NOT NULL DEFAULT 0 COMMENT '已交卷数',
  `counted_count` int NOT NULL DEFAULT 0 COMMENT '已入统数',
  `pending_mark_count` int NOT NULL DEFAULT 0 COMMENT '待阅卷数',
  `excluded_count` int NOT NULL DEFAULT 0 COMMENT '作废数',
  `attendance_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '参考率%',
  `full_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '试卷总分',
  `pass_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '及格分',
  `max_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '最高分',
  `min_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低分',
  `avg_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '平均分',
  `median_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '中位数',
  `std_dev` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '标准差',
  `pass_count` int NOT NULL DEFAULT 0 COMMENT '及格人数',
  `pass_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '及格率%',
  `excellent_count` int NOT NULL DEFAULT 0 COMMENT '优秀人数',
  `excellent_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '优秀率%',
  `avg_used_seconds` int NOT NULL DEFAULT 0 COMMENT '平均用时(秒)',
  `avg_objective_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '客观题平均分',
  `avg_subjective_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '主观题平均分',
  `difficulty` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '难度系数',
  `discrimination` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '区分度',
  `has_subjective` char(1) NOT NULL DEFAULT '0' COMMENT '是否含主观题 0否 1是',
  `partial_score` char(1) NOT NULL DEFAULT '0' COMMENT '客观题部分得分开关快照 0必须全对 1部分得分',
  `partial_score_rate` int NOT NULL DEFAULT 100 COMMENT '部分正确得分比例快照',
  `calc_status` varchar(20) NOT NULL DEFAULT 'success' COMMENT 'computing计算中 success成功 fail失败',
  `calc_version` int NOT NULL DEFAULT 0 COMMENT '计算版本号',
  `calc_time` datetime DEFAULT NULL COMMENT '最后计算时间',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 2已删',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam` (`exam_id`, `del_flag`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试统计汇总';

-- ----------------------------
-- 3. 分数段分布
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_exam_score_segment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `segment_label` varchar(30) NOT NULL DEFAULT '' COMMENT '分段名 如 60-69',
  `segment_min` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '下限(含)',
  `segment_max` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '上限(不含,末段含)',
  `person_count` int NOT NULL DEFAULT 0 COMMENT '人数',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`, `del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试分数段分布';

-- ----------------------------
-- 4. 试题维度统计
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_exam_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `question_type` varchar(30) DEFAULT '' COMMENT '题型',
  `question_category` varchar(20) DEFAULT '' COMMENT 'objective客观 subjective主观',
  `difficulty` varchar(20) DEFAULT '' COMMENT '难度',
  `sort` int NOT NULL DEFAULT 0 COMMENT '题号',
  `full_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '本题满分',
  `answer_count` int NOT NULL DEFAULT 0 COMMENT '作答人数',
  `blank_count` int NOT NULL DEFAULT 0 COMMENT '未作答人数',
  `correct_count` int NOT NULL DEFAULT 0 COMMENT '完全答对人数',
  `correct_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '正确率%',
  `partial_count` int NOT NULL DEFAULT 0 COMMENT '部分正确人数(部分得分模式)',
  `wrong_count` int NOT NULL DEFAULT 0 COMMENT '答错人数',
  `avg_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '平均得分',
  `score_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '得分率%',
  `max_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '最高得分',
  `min_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低得分',
  `zero_count` int NOT NULL DEFAULT 0 COMMENT '零分人数',
  `full_count` int NOT NULL DEFAULT 0 COMMENT '满分人数',
  `difficulty_index` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '难度系数',
  `discrimination` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '区分度',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_question` (`exam_id`, `question_id`, `del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试试题统计';

-- ----------------------------
-- 5. 选项分布
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_exam_question_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `option_key` varchar(10) NOT NULL DEFAULT '' COMMENT '选项标识 A/B/C/D',
  `option_content` varchar(500) DEFAULT '' COMMENT '选项内容',
  `select_count` int NOT NULL DEFAULT 0 COMMENT '选中人次',
  `select_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '选中率%',
  `is_correct` char(1) NOT NULL DEFAULT '0' COMMENT '是否正确选项 0否 1是',
  `trap` tinyint NOT NULL DEFAULT 0 COMMENT '易错项 0否 1是(非正确项但选中率超三成)',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_question_option` (`exam_id`, `question_id`, `option_key`, `del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试试题选项分布';

-- ----------------------------
-- 6. 考生维度统计
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_exam_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `user_id` bigint NOT NULL COMMENT '考生用户ID',
  `record_id` bigint DEFAULT NULL COMMENT '答卷记录ID',
  `attempt_no` int NOT NULL DEFAULT 1 COMMENT '第几次参加',
  `account` varchar(100) DEFAULT '' COMMENT '考生账号',
  `user_name` varchar(50) DEFAULT '' COMMENT '考生姓名快照',
  `dept_name` varchar(100) DEFAULT '' COMMENT '部门名快照',
  `total_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
  `objective_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '客观题得分',
  `subjective_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '主观题得分',
  `correct_count` int NOT NULL DEFAULT 0 COMMENT '答对题数',
  `wrong_count` int NOT NULL DEFAULT 0 COMMENT '答错题数',
  `blank_count` int NOT NULL DEFAULT 0 COMMENT '未答题数',
  `passed` tinyint NOT NULL DEFAULT 0 COMMENT '是否及格 0否 1是',
  `rank_no` int NOT NULL DEFAULT 0 COMMENT '排名',
  `used_seconds` int NOT NULL DEFAULT 0 COMMENT '用时(秒)',
  `submit_time` datetime DEFAULT NULL COMMENT '交卷时间',
  `stat_status` varchar(20) NOT NULL DEFAULT 'COUNTED' COMMENT 'COUNTED已入统 PENDING_MARK待阅 EXCLUDED作废',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_user_attempt` (`exam_id`, `user_id`, `attempt_no`, `del_flag`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`, `del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试考生统计';

-- ----------------------------
-- 7. 知识点维度统计（一期复用题库分类树）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_exam_knowledge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `knowledge_id` bigint NOT NULL COMMENT '知识点ID(一期=题库分类ID)',
  `knowledge_name` varchar(200) DEFAULT '' COMMENT '知识点名称',
  `knowledge_path` varchar(500) DEFAULT '' COMMENT '知识点全路径',
  `question_count` int NOT NULL DEFAULT 0 COMMENT '题目数',
  `full_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '知识点总分',
  `avg_score` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '平均得分',
  `score_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '得分率%',
  `wrong_count` int NOT NULL DEFAULT 0 COMMENT '错误人次',
  `wrong_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '错误率%',
  `mastery` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '掌握度%',
  `weak_level` varchar(20) DEFAULT '' COMMENT 'good良好 normal一般 weak薄弱',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_exam_knowledge` (`exam_id`, `knowledge_id`, `del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试知识点统计';

-- ----------------------------
-- 8. 计算任务
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_calc_task` (
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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_exam_id` (`exam_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计计算任务';

-- ----------------------------
-- 9. 导出任务
-- ----------------------------
CREATE TABLE IF NOT EXISTS `stat_export_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint DEFAULT NULL COMMENT '考试ID',
  `export_type` varchar(20) NOT NULL DEFAULT '' COMMENT 'overview汇总 score成绩单 question题目 knowledge知识点 detail明细',
  `param_json` varchar(2000) DEFAULT '' COMMENT '筛选条件快照',
  `status` varchar(20) NOT NULL DEFAULT 'waiting' COMMENT 'waiting/running/success/fail',
  `file_oss_id` varchar(200) DEFAULT '' COMMENT '文件OSS标识',
  `file_name` varchar(200) DEFAULT '' COMMENT '文件名',
  `row_count` int NOT NULL DEFAULT 0 COMMENT '导出行数',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `error_msg` varchar(1000) DEFAULT '' COMMENT '错误信息',
  `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计导出任务';
