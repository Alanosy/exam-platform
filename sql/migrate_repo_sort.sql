-- 题库排序字段（仅需执行一次；若 sort 字段已存在，请勿重复执行 ALTER）
ALTER TABLE `t_repo`
    ADD COLUMN `sort` int(11) NOT NULL DEFAULT 0 COMMENT '显示排序，越小越靠前' AFTER `is_exercise`;

-- 使用 ID 初始化存量排序，保持上线前列表按 ID 升序的稳定顺序
UPDATE `t_repo` SET `sort` = `id` WHERE `sort` = 0;
