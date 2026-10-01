-- -----------------------------------------------------------------------------
-- 考试业务字典：把前端写死的下拉 / 标签文案收敛到 RuoYi 字典
--
-- 背景：
--   1) 考试、阅卷、错题本、考试中心、考试记录里有一批状态 / 类型是写死在 .vue 里的，
--      改一个文案要动代码。这里把它们全部建成字典，页面改成「字典优先 + 写死兜底」。
--   2) 题库(bank_*)、试题(question_*)、代码语言(code_languages) 前端其实已经是字典驱动，
--      但脚本里没有种子数据 —— 说明是有人在「字典管理」页面上手工建的。
--      一旦从脚本重建库，这些下拉会全部变空。所以这里一并补上种子。
--
-- 幂等：每个字典类型按 dict_type 判重、每条字典数据按 (dict_type, dict_value) 判重，
--       重复执行不会插重，也不会覆盖你手工改过的文案。
--
-- 租户：tenant_id 跟随 sys_dict_type 里已有的平台字典 sys_yes_no，
--       避免多租户环境下只有 000000 能看到这些字典。
--
-- 执行方式：手工执行本文件，不要在应用启动时自动执行。
-- -----------------------------------------------------------------------------

-- =============================================================================
-- 1. 考试管理 exam
-- =============================================================================

-- 1.1 考试类型 exam_type（1 正式考试 / 2 练习考试）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '考试类型', 'exam_type', `t`.`tenant_id`, 103, 1, sysdate(), '考试任务类型：正式考试 / 练习考试'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'exam_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'exam_type', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '正式考试' `l`, '1' `val`, 'warning' `c`, 'Y' `d`, '有时间窗口、默认不可多次作答' `r`
    UNION ALL SELECT 2, '练习考试', '2', 'success', 'N', '长期可练、不限次数、可开即时判题'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'exam_type' AND `d`.`dict_value` = `v`.`val`);

-- 1.2 考试状态 exam_status
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '考试状态', 'exam_status', `t`.`tenant_id`, 103, 1, sysdate(), '考试生命周期：未开始 / 进行中 / 已结束 / 已归档'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'exam_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'exam_status', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '未开始' `l`, 'not_start' `val`, 'info' `c`, 'Y' `d`, '尚未到开始时间' `r`
    UNION ALL SELECT 2, '进行中', 'ongoing', 'success', 'N', '处于考试窗口内'
    UNION ALL SELECT 3, '已结束', 'finished', 'info', 'N', '已过结束时间'
    UNION ALL SELECT 4, '已归档', 'archived', 'warning', 'N', '已归档，不再变动'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'exam_status' AND `d`.`dict_value` = `v`.`val`);

-- 1.3 考生准入方式 exam_participant_type
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '考生准入方式', 'exam_participant_type', `t`.`tenant_id`, 103, 1, sysdate(), '考生进入考试的方式：白名单 / 公开链接'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'exam_participant_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'exam_participant_type', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '白名单' `l`, 'white' `val`, 'primary' `c`, 'Y' `d`, '只有导入名单的考生可参加' `r`
    UNION ALL SELECT 2, '公开链接', 'public', 'success', 'N', '任何人凭链接（及密码）可参加'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'exam_participant_type' AND `d`.`dict_value` = `v`.`val`);

-- 1.4 答案展示时机 exam_show_answer_mode（immediate 仅练习考试可用）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '答案展示时机', 'exam_show_answer_mode', `t`.`tenant_id`, 103, 1, sysdate(), '答案与解析对考生开放的时机，immediate 仅练习考试提供'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'exam_show_answer_mode');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'exam_show_answer_mode', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '不展示' `l`, 'none' `val`, 'info' `c`, 'Y' `d`, '任何阶段都不给答案' `r`
    UNION ALL SELECT 2, '交卷后展示', 'after_submit', 'primary', 'N', '交卷后才能看答案与解析'
    UNION ALL SELECT 3, '考试结束后展示', 'after_exam', 'warning', 'N', '整场考试结束后统一公开答案'
    UNION ALL SELECT 4, '立即展示（刷题即时判题）', 'immediate', 'success', 'N', '做完一题立刻判对错并显示答案与解析'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'exam_show_answer_mode' AND `d`.`dict_value` = `v`.`val`);

-- =============================================================================
-- 2. 考试中心 / 考试记录
-- =============================================================================

-- 2.1 我的考试状态 exam_my_status（后端按时间窗与作答记录实时算出来的，不落库）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '我的考试状态', 'exam_my_status', `t`.`tenant_id`, 103, 1, sysdate(), '考生视角的考试状态，由 ExamRecordServiceImpl 实时计算'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'exam_my_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'exam_my_status', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '未开始' `l`, 'not_start' `val`, 'info' `c`, 'Y' `d`, '考试尚未开始' `r`
    UNION ALL SELECT 2, '待考试', 'pending', 'primary', 'N', '可参加但尚未开始作答'
    UNION ALL SELECT 3, '答题中', 'answering', 'warning', 'N', '有未提交的作答记录'
    UNION ALL SELECT 4, '已交卷', 'submitted', 'success', 'N', '已有提交记录'
    UNION ALL SELECT 5, '已结束', 'ended', 'info', 'N', '考试已结束'
    UNION ALL SELECT 6, '迟到不可参加', 'late', 'danger', 'N', '超过允许的迟到时间'
    UNION ALL SELECT 7, '不可参加', 'blocked', 'danger', 'N', '次数用尽或未在名单内'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'exam_my_status' AND `d`.`dict_value` = `v`.`val`);

-- 2.2 作答记录状态 exam_record_status
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '作答记录状态', 'exam_record_status', `t`.`tenant_id`, 103, 1, sysdate(), 'exam_record.status：答题中 / 已交卷 / 已过期'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'exam_record_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'exam_record_status', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '答题中' `l`, 'answering' `val`, 'warning' `c`, 'Y' `d`, '尚未交卷' `r`
    UNION ALL SELECT 2, '已交卷', 'submitted', 'success', 'N', '已提交，等待/已完成阅卷'
    UNION ALL SELECT 3, '已过期', 'expired', 'info', 'N', '超时未交卷，系统自动作废'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'exam_record_status' AND `d`.`dict_value` = `v`.`val`);

-- =============================================================================
-- 3. 阅卷 mark
-- =============================================================================

-- 3.1 阅卷任务状态 mark_task_status
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '阅卷任务状态', 'mark_task_status', `t`.`tenant_id`, 103, 1, sysdate(), 'exam_mark_task.status：待阅 / 阅卷中 / 已阅完'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'mark_task_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'mark_task_status', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '待阅' `l`, 'pending' `val`, 'warning' `c`, 'Y' `d`, '主观题一道都没阅' `r`
    UNION ALL SELECT 2, '阅卷中', 'marking', 'primary', 'N', '已阅部分主观题'
    UNION ALL SELECT 3, '已阅完', 'finished', 'success', 'N', '全部主观题已阅并回写成绩'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'mark_task_status' AND `d`.`dict_value` = `v`.`val`);

-- 3.2 阅卷操作类型 mark_log_action
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '阅卷操作类型', 'mark_log_action', `t`.`tenant_id`, 103, 1, sysdate(), 'exam_mark_log.action：建任务 / 打分 / 改分 / AI预评 / 完成阅卷'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'mark_log_action');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'mark_log_action', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '建任务' `l`, 'create' `val`, 'info' `c`, 'Y' `d`, '交卷后自动生成阅卷任务' `r`
    UNION ALL SELECT 2, '打分', 'score', 'success', 'N', '首次给分'
    UNION ALL SELECT 3, '改分', 'rescore', 'warning', 'N', '修改已给的分数'
    UNION ALL SELECT 4, 'AI 预评', 'ai', 'primary', 'N', '由 AI 给出建议分'
    UNION ALL SELECT 5, '完成阅卷', 'finish', 'success', 'N', '全部阅完并回写成绩'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'mark_log_action' AND `d`.`dict_value` = `v`.`val`);

-- =============================================================================
-- 4. 错题本 wrong
-- =============================================================================

-- 4.1 错题来源类型 wrong_source_type
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '错题来源类型', 'wrong_source_type', `t`.`tenant_id`, 103, 1, sysdate(), 'wrong_question.source_type：考试 / 试卷练习'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'wrong_source_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'wrong_source_type', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '考试' `l`, 'EXAM' `val`, 'warning' `c`, 'Y' `d`, '参加考试时答错' `r`
    UNION ALL SELECT 2, '试卷练习', 'PAPER_PRACTICE', 'info', 'N', '自主练习试卷时答错'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'wrong_source_type' AND `d`.`dict_value` = `v`.`val`);

-- 4.2 错题掌握状态 wrong_master_status
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '错题掌握状态', 'wrong_master_status', `t`.`tenant_id`, 103, 1, sysdate(), 'wrong_question.master_status：未掌握 / 已掌握 / 已忽略'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'wrong_master_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'wrong_master_status', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '未掌握' `l`, 'NOT_MASTER' `val`, 'danger' `c`, 'Y' `d`, '还需要继续练' `r`
    UNION ALL SELECT 2, '已掌握', 'MASTERED', 'success', 'N', '连续答对后自动标记'
    UNION ALL SELECT 3, '已忽略', 'IGNORED', 'info', 'N', '用户主动移出错题本'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'wrong_master_status' AND `d`.`dict_value` = `v`.`val`);

-- =============================================================================
-- 5. 试题 question（前端已是字典驱动，这里补种子数据）
-- =============================================================================

-- 5.1 题型 question_type
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '题型', 'question_type', `t`.`tenant_id`, 103, 1, sysdate(), '试题题型，编码与 questionMeta.ts 保持一致'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'question_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'question_type', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '单选题' `l`, 'SINGLE' `val`, 'primary' `c`, 'Y' `d`, '勾选唯一正确答案' `r`
    UNION ALL SELECT 2, '多选题', 'MULTIPLE', 'primary', 'N', '至少两个正确答案，可部分给分'
    UNION ALL SELECT 3, '判断题', 'JUDGE', 'primary', 'N', '固定「正确 / 错误」两项'
    UNION ALL SELECT 4, '填空题', 'BLANK', 'primary', 'N', '按空依次录入参考答案'
    UNION ALL SELECT 5, '简答题', 'SHORT_ANSWER', 'primary', 'N', '录入参考答案要点'
    UNION ALL SELECT 6, '论述题', 'ESSAY', 'primary', 'N', '录入参考答案要点'
    UNION ALL SELECT 7, '代码题', 'CODE', 'primary', 'N', '指定语言并录入参考实现'
    UNION ALL SELECT 8, '文件上传题', 'UPLOAD_FILE', 'primary', 'N', '录入评分要点 / 材料要求'
    UNION ALL SELECT 9, '匹配题', 'MATCH', 'primary', 'N', '按行录入左右两列'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'question_type' AND `d`.`dict_value` = `v`.`val`);

-- 5.2 难度 question_difficulty
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '试题难度', 'question_difficulty', `t`.`tenant_id`, 103, 1, sysdate(), 'easy简单 / medium中等 / hard困难'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'question_difficulty');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'question_difficulty', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '简单' `l`, 'easy' `val`, 'success' `c`, 'N' `d`, '基础题' `r`
    UNION ALL SELECT 2, '中等', 'medium', 'warning', 'Y', '常规题'
    UNION ALL SELECT 3, '困难', 'hard', 'danger', 'N', '拔高题'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'question_difficulty' AND `d`.`dict_value` = `v`.`val`);

-- 5.3 试题状态 question_status
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '试题状态', 'question_status', `t`.`tenant_id`, 103, 1, sysdate(), '0草稿 / 1启用 / 2废弃'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'question_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'question_status', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '草稿' `l`, '0' `val`, 'info' `c`, 'Y' `d`, '编辑中，不可组卷' `r`
    UNION ALL SELECT 2, '启用', '1', 'success', 'N', '可被组卷使用'
    UNION ALL SELECT 3, '废弃', '2', 'danger', 'N', '不再使用'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'question_status' AND `d`.`dict_value` = `v`.`val`);

-- 5.4 代码语言 code_languages
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '代码语言', 'code_languages', `t`.`tenant_id`, 103, 1, sysdate(), '代码题可选的编程语言'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'code_languages');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'code_languages', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, 'Java' `l`, 'java' `val`, '' `c`, 'Y' `d`, '' `r`
    UNION ALL SELECT 2, 'Python', 'python', '', 'N', ''
    UNION ALL SELECT 3, 'C++', 'cpp', '', 'N', ''
    UNION ALL SELECT 4, 'C', 'c', '', 'N', ''
    UNION ALL SELECT 5, 'C#', 'csharp', '', 'N', ''
    UNION ALL SELECT 6, 'Go', 'go', '', 'N', ''
    UNION ALL SELECT 7, 'JavaScript', 'javascript', '', 'N', ''
    UNION ALL SELECT 8, 'TypeScript', 'typescript', '', 'N', ''
    UNION ALL SELECT 9, 'SQL', 'sql', '', 'N', ''
    UNION ALL SELECT 10, 'Shell', 'shell', '', 'N', ''
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'code_languages' AND `d`.`dict_value` = `v`.`val`);

-- =============================================================================
-- 6. 题库 bank（前端已是字典驱动，这里补种子数据）
-- =============================================================================

-- 6.1 题库可见性 bank_visibility_type
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '题库可见性', 'bank_visibility_type', `t`.`tenant_id`, 103, 1, sysdate(), 'private私有 / public公开'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'bank_visibility_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'bank_visibility_type', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '私有' `l`, 'private' `val`, 'info' `c`, 'Y' `d`, '仅创建者及授权人可见' `r`
    UNION ALL SELECT 2, '公开', 'public', 'success', 'N', '同租户内所有人可见'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'bank_visibility_type' AND `d`.`dict_value` = `v`.`val`);

-- 6.2 题库状态 bank_status
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `tenant_id`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT '题库状态', 'bank_status', `t`.`tenant_id`, 103, 1, sysdate(), '0草稿 / 1正常 / 2归档'
FROM (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` `d` WHERE `d`.`dict_type` = 'bank_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `tenant_id`, `css_class`, `list_class`, `is_default`, `create_dept`, `create_by`, `create_time`, `remark`)
SELECT `v`.`s`, `v`.`l`, `v`.`val`, 'bank_status', `t`.`tenant_id`, '', `v`.`c`, `v`.`d`, 103, 1, sysdate(), `v`.`r`
FROM (
    SELECT 1 `s`, '草稿' `l`, '0' `val`, 'info' `c`, 'Y' `d`, '编辑中' `r`
    UNION ALL SELECT 2, '正常', '1', 'success', 'N', '可正常使用'
    UNION ALL SELECT 3, '归档', '2', 'warning', 'N', '已归档，不再新增试题'
) `v`, (SELECT `tenant_id` FROM `sys_dict_type` WHERE `dict_type` = 'sys_yes_no' LIMIT 1) `t`
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` `d` WHERE `d`.`dict_type` = 'bank_status' AND `d`.`dict_value` = `v`.`val`);

-- -----------------------------------------------------------------------------
-- 说明：字典建好后，页面可以在「系统管理 → 字典管理」里直接改文案与标签颜色，
--       不需要改代码、不需要重启。前端采用「字典优先 + 代码兜底」，
--       字典被清空时页面仍能用兜底文案正常渲染。
-- -----------------------------------------------------------------------------
