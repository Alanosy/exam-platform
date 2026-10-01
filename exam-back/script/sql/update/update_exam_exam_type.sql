-- -----------------------------------------------------------------------------
-- 考试任务类型：正式考试 / 练习考试
--
-- 说明：一场「活动」的规则全部挂在考试这一层，试卷只管题目与组卷属性。
--       exam_type 取自字典 exam_type：1=正式考试，2=练习考试。
--       practice / answer / 错题本都靠它区分「正式」与「练习」。
--
-- 已有数据：exam 表里的存量考试按 1（正式考试）兜底，不需要额外刷数据。
-- 执行方式：手工执行本文件，不要在应用启动时自动执行。
-- -----------------------------------------------------------------------------

-- 1. exam 主表加任务类型
ALTER TABLE `exam`
    ADD COLUMN `exam_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1'
    COMMENT '任务类型 1正式考试 2练习考试，取字典 exam_type' AFTER `paper_id`;

-- 存量数据统一按正式考试处理（DEFAULT 已经带了值，这里显式刷一遍防止脏数据）
UPDATE `exam` SET `exam_type` = '1' WHERE `exam_type` IS NULL OR `exam_type` = '';

-- 2. 起止时间放开非空：练习考试可以不填，长期可练
--    正式考试仍需必填，由 ExamServiceImpl.checkExamRules 按类型校验
ALTER TABLE `exam`
    MODIFY COLUMN `start_time` datetime NULL COMMENT '考试开始时间，练习考试可为空表示长期有效',
    MODIFY COLUMN `end_time` datetime NULL COMMENT '考试结束时间，练习考试可为空表示长期有效';

-- 3. 字典：考试类型
--    注意：RuoYi-Cloud-Plus 的 sys_dict_type 只有 dict_name/dict_type/remark 等列，
--    没有 visible / status（那是 sys_menu 的），别照着菜单表抄。
--    租户号跟着 sys_dict_type 里已有的平台字典走，避免多租户下只有 000000 能看到。
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '考试类型', 'exam_type', t.`tenant_id`, 103, 1, sysdate(), '考试任务类型：正式考试 / 练习考试'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) t
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` d WHERE d.`dict_type` = 'exam_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT 1, '正式考试', '1', 'exam_type', t.`tenant_id`, '', 'primary', 'Y', 103, 1, sysdate(), '有时间窗口、默认不可多次作答'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) t
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` d WHERE d.`dict_type` = 'exam_type' AND d.`dict_value` = '1');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT 2, '练习考试', '2', 'exam_type', t.`tenant_id`, '', 'success', 'N', 103, 1, sysdate(), '长期可练、不限次数、可开即时判题'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) t
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` d WHERE d.`dict_type` = 'exam_type' AND d.`dict_value` = '2');
