-- 考试主表：公开考试加入码
-- 公开链接的考试需要一个不可枚举的随机码来拼加入链接，直接用自增 id 会被遍历到别人的考试

ALTER TABLE `exam`
    ADD COLUMN `join_code` varchar(32) DEFAULT NULL COMMENT '公开考试加入码，participant_type=public 时有效' AFTER `join_expire_time`,
    ADD UNIQUE KEY `uk_join_code` (`join_code`);

-- 历史数据里已有的公开考试补一个加入码
-- UPDATE `exam` SET `join_code` = SUBSTRING(REPLACE(UUID(), '-', ''), 1, 10)
--  WHERE `participant_type` = 'public' AND (`join_code` IS NULL OR `join_code` = '');
