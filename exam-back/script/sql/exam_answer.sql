-- ----------------------------
-- 答题库（ry-exam-answer）：考生的一次考试记录与逐题作答
-- 与考试库分库，答卷表写入量大、后续便于分库分表
-- ----------------------------

-- ----------------------------
-- Table structure for exam_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record`  (
                                `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `exam_id` bigint(0) NOT NULL COMMENT '考试ID',
                                `paper_id` bigint(0) NOT NULL COMMENT '试卷ID（开考时快照，试卷后续改动不影响已开考的答卷）',
                                `user_id` bigint(0) NULL DEFAULT NULL COMMENT '考生用户ID',
                                `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考生账号（登录名）',
                                `attempt_no` int(0) NOT NULL DEFAULT 1 COMMENT '第几次参加，从1开始',
                                `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'answering' COMMENT 'answering答题中 / submitted已交卷 / expired超时作废',
                                `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开考时间',
                                `submit_time` datetime(0) NULL DEFAULT NULL COMMENT '交卷时间',
                                `used_seconds` int(0) NOT NULL DEFAULT 0 COMMENT '用时（秒）',
                                `duration_minutes` bigint(0) NOT NULL DEFAULT 0 COMMENT '本场限时（分钟），0表示不限时',
                                `question_count` int(0) NOT NULL DEFAULT 0 COMMENT '题目总数',
                                `answered_count` int(0) NOT NULL DEFAULT 0 COMMENT '已作答题目数',
                                `objective_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '客观题得分（自动判分）',
                                `subjective_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '主观题得分（人工阅卷后回填）',
                                `total_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总分',
                                `pass_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '及格分（开考时取自试卷）',
                                `passed` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否及格 0否 1是',
                                `auto_submit` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否超时系统自动交卷 0否 1是',
                                `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户ID',
                                `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_exam_account_attempt`(`exam_id`, `account`, `attempt_no`, `del_flag`) USING BTREE,
                                INDEX `idx_account`(`account`) USING BTREE,
                                INDEX `idx_exam_id`(`exam_id`) USING BTREE,
                                INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试答卷记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exam_answer
-- ----------------------------
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer`  (
                                `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `record_id` bigint(0) NOT NULL COMMENT '答卷记录ID',
                                `question_id` bigint(0) NOT NULL COMMENT '试题ID',
                                `question_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '题型，冗余便于统计',
                                `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '考生作答JSON：客观题{choices:[A]}或{blanks:[..]}，主观题{text:..}',
                                `score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '本题得分',
                                `correct` tinyint(0) NOT NULL DEFAULT 0 COMMENT '0未判 1正确 2错误',
                                `sort` int(0) NOT NULL DEFAULT 0 COMMENT '题号顺序',
                                `tenant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户ID',
                                `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                                `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_record_question`(`record_id`, `question_id`, `del_flag`) USING BTREE,
                                INDEX `idx_record_id`(`record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试逐题作答表' ROW_FORMAT = Dynamic;
