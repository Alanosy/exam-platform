-- ----------------------------
-- 证书模块表结构（考试主库 ry-exam）
--
-- 说明：ruoyi-exam-cert 服务的数据源指向考试主库（见 script/config/nacos/ruoyi-exam-cert.yml），
--       所以两张表都建在 ry-exam，与 exam 同库（证书服务要读 exam.cert_id 判断这场考试发不发证书）。
--       答卷与成绩在 ry-exam-answer 库，跨库一律走 Dubbo，只把 record_id 存过来。
--
-- 两张表的关系：
--   exam_certificate         证书模板：一场考试选一个模板，改模板不影响已经发出去的证书
--   exam_certificate_record  颁发记录：考生及格后生成，正文/标题/印章全部快照，
--                            模板后续被改甚至被删都不影响这张已发出的证书
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exam_certificate
-- ----------------------------
CREATE TABLE IF NOT EXISTS `exam_certificate` (
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
  UNIQUE KEY `uk_cert_code` (`cert_code`, `del_flag`) USING BTREE,
  KEY `idx_cert_name` (`cert_name`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '证书模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exam_certificate_record
-- ----------------------------
CREATE TABLE IF NOT EXISTS `exam_certificate_record` (
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
  -- 同一份答卷只发一张证书：重考另开一份答卷，所以按 record_id 唯一而不是按人唯一
  UNIQUE KEY `uk_exam_record` (`exam_id`, `record_id`, `del_flag`) USING BTREE,
  KEY `idx_user` (`user_id`) USING BTREE,
  KEY `idx_exam_status` (`exam_id`, `status`) USING BTREE,
  KEY `idx_cert` (`cert_id`) USING BTREE,
  KEY `idx_issue_time` (`issue_time`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '证书颁发记录' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
