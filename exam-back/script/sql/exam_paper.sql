-- ----------------------------
-- 试卷模块：组卷页改造（新增页改为整页组卷，支持随机抽题 / 手动选题）
-- 执行前请先备份 paper 表
-- ----------------------------

-- 1. paper 表新增组卷配置列
ALTER TABLE paper
    ADD COLUMN category        varchar(64)  DEFAULT NULL COMMENT '试卷分类，取字典 paper_category 的字典值' AFTER creator_id,
    ADD COLUMN default_score   int(11)      DEFAULT 0    COMMENT '默认单题分值' AFTER category,
    ADD COLUMN question_shuffle char(1)     DEFAULT '0'  COMMENT '是否开启题目乱序 0否 1是' AFTER default_score,
    ADD COLUMN option_shuffle  char(1)      DEFAULT '0'  COMMENT '是否开启选项乱序 0否 1是' AFTER question_shuffle,
    ADD COLUMN auto_judge      char(1)      DEFAULT '1'  COMMENT '客观题是否自动判分 0否 1是' AFTER option_shuffle,
    ADD COLUMN manual_review   char(1)      DEFAULT '1'  COMMENT '主观题是否人工阅卷 0否 1是' AFTER auto_judge,
    ADD COLUMN partial_score   char(1)      DEFAULT '0'  COMMENT '是否支持部分得分 0否 1是' AFTER manual_review,
    ADD COLUMN wrong_deduct    char(1)      DEFAULT '0'  COMMENT '答错是否扣分 0否 1是' AFTER partial_score,
    ADD COLUMN share_scope     varchar(20)  DEFAULT 'SELF' COMMENT '可见范围 SELF仅自己可编辑 SHARED共享给其他管理员' AFTER wrong_deduct;

-- 已存在的历史数据补齐默认值，避免出现 null
UPDATE paper SET default_score = 0 WHERE default_score IS NULL;
UPDATE paper SET question_shuffle = '0' WHERE question_shuffle IS NULL;
UPDATE paper SET option_shuffle = '0' WHERE option_shuffle IS NULL;
UPDATE paper SET auto_judge = '1' WHERE auto_judge IS NULL;
UPDATE paper SET manual_review = '1' WHERE manual_review IS NULL;
UPDATE paper SET partial_score = '0' WHERE partial_score IS NULL;
UPDATE paper SET wrong_deduct = '0' WHERE wrong_deduct IS NULL;
UPDATE paper SET share_scope = 'SELF' WHERE share_scope IS NULL OR share_scope = '';

-- 2. 试卷分类字典（分类内容可在「系统管理 > 字典管理」里自行增删）
--    dict_id / dict_code 如与现有数据冲突，改成未占用的值即可
DELETE FROM sys_dict_type WHERE dict_type = 'paper_category';
DELETE FROM sys_dict_data WHERE dict_type = 'paper_category';

INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark)
VALUES (110, '000000', '试卷分类', 'paper_category', 103, 1, sysdate(), '试卷分类列表');

INSERT INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
VALUES
    (1101, '000000', 1, '入职考试',   'entry',       'paper_category', '', 'primary',   'N', 103, 1, sysdate(), '入职考试'),
    (1102, '000000', 2, '岗位认证',   'certification','paper_category', '', 'success',  'N', 103, 1, sysdate(), '岗位认证'),
    (1103, '000000', 3, '安全合规',   'compliance',  'paper_category', '', 'warning',  'N', 103, 1, sysdate(), '安全合规'),
    (1104, '000000', 4, '技能培训',   'training',    'paper_category', '', 'info',     'N', 103, 1, sysdate(), '技能培训'),
    (1105, '000000', 5, '日常练习',   'practice',    'paper_category', '', 'info',     'N', 103, 1, sysdate(), '日常练习'),
    (1106, '000000', 6, '其他',       'other',       'paper_category', '', 'info',     'N', 103, 1, sysdate(), '其他');
