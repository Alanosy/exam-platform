ALTER TABLE `flow_task`
    ADD COLUMN `flow_status` varchar(20) NOT NULL COMMENT '流程状态（0待提交 1审批中 2审批通过 4终止 5作废 6撤销 8已完成 9已退回 10失效 11拿回）' AFTER `node_type`;

ALTER TABLE `flow_instance`
    MODIFY COLUMN `flow_status` varchar(20) NOT NULL COMMENT '流程状态（0待提交 1审批中 2审批通过 4终止 5作废 6撤销 8已完成 9已退回 10失效 11拿回）' AFTER `variable`;

ALTER TABLE `flow_his_task`
    MODIFY COLUMN `flow_status` varchar(20) NOT NULL COMMENT '流程状态（0待提交 1审批中 2审批通过 4终止 5作废 6撤销 8已完成 9已退回 10失效 11拿回）' AFTER `skip_type`

ALTER TABLE `sys_social`
    MODIFY COLUMN `access_token` varchar(2000)  NOT NULL COMMENT '用户的授权令牌' AFTER `avatar`;
ALTER TABLE `sys_social`
    MODIFY COLUMN `refresh_token` varchar(2000) DEFAULT NULL COMMENT '刷新令牌，部分平台可能没有' AFTER `expire_in`;
