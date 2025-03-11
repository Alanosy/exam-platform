ALTER TABLE `flow_node` DROP COLUMN `skip_any_node`;
ALTER TABLE `flow_node`
    ADD COLUMN `ext` text NULL COMMENT '扩展属性' AFTER `update_time`;

ALTER TABLE `sys_oss`
    ADD COLUMN `ext1` text NULL COMMENT '扩展属性' AFTER `url`;
