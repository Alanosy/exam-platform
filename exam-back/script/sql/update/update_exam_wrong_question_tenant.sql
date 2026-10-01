-- 错题本补齐租户字段
-- 背景：项目开启多租户（tenant.enable=true），租户拦截器会给所有业务表拼 tenant_id，
--       wrong_question / wrong_review_record 建表时漏了该字段，查询与新增都会报 Unknown column。
-- 影响：ry-exam 库
-- 注意：两张表当前没有数据，新增字段不会影响存量记录；请在业务低峰执行。

ALTER TABLE `wrong_question`
    ADD COLUMN `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID' AFTER `is_deleted`;

ALTER TABLE `wrong_review_record`
    ADD COLUMN `tenant_id` varchar(20) DEFAULT NULL COMMENT '租户ID' AFTER `review_time`;
