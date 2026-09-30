ALTER TABLE `flow_definition`
    ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT '创建人' AFTER `create_time`,
    ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT '更新人' AFTER `update_time`;

ALTER TABLE `flow_node`
    ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT '创建人' AFTER `create_time`,
    ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT '更新人' AFTER `update_time`;

ALTER TABLE `flow_skip`
    ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT '创建人' AFTER `create_time`,
    ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT '更新人' AFTER `update_time`;

ALTER TABLE `flow_instance`
    ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT '更新人' AFTER `update_time`;

ALTER TABLE `flow_task`
    ADD COLUMN `create_by` varchar(64) NULL DEFAULT NULL COMMENT '创建人' AFTER `create_time`,
    ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT '更新人' AFTER `update_time`;

ALTER TABLE `flow_user`
    ADD COLUMN `update_by` varchar(64) NULL DEFAULT NULL COMMENT '更新人' AFTER `update_time`;
