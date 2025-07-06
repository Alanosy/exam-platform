-- ry-workflow 数据库
CREATE TABLE flow_spel (
    id bigint(20) NOT NULL COMMENT '主键id',
    component_name varchar(255) DEFAULT NULL COMMENT '组件名称',
    method_name varchar(255) DEFAULT NULL COMMENT '方法名',
    method_params varchar(255) DEFAULT NULL COMMENT '参数',
    view_spel varchar(255) DEFAULT NULL COMMENT '预览spel表达式',
    remark varchar(255) DEFAULT NULL COMMENT '备注',
    status char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag char(1) DEFAULT '0' COMMENT '删除标志',
    create_dept bigint(20) DEFAULT NULL COMMENT '创建部门',
    create_by bigint(20) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_by bigint(20) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT='流程spel表达式定义表';

INSERT INTO flow_spel VALUES (1, 'spelRuleComponent', 'selectDeptLeaderById', 'initiatorDeptId', '#{@spelRuleComponent.selectDeptLeaderById(#initiatorDeptId)}', '根据部门id获取部门负责人', '0', '0', 103, 1, sysdate(), 1, sysdate());
INSERT INTO flow_spel VALUES (2, NULL, NULL, 'initiator', '${initiator}', '流程发起人', '0', '0', 103, 1, sysdate(), 1, sysdate());

-- ry-cloud 数据库
INSERT INTO sys_menu VALUES ('11801', '流程表达式', '11616', '2', 'spel',    'workflow/spel/index', '', 1, 0, 'C', '0', '0', 'workflow:spel:list', 'input', 103, 1, sysdate(), 1, sysdate(), '流程达式定义菜单');
INSERT INTO sys_menu VALUES ('11802', '流程spel达式定义查询', '11801', 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11803', '流程spel达式定义新增', '11801', 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:add', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11804', '流程spel达式定义修改', '11801', 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:edit', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11805', '流程spel达式定义删除', '11801', 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:remove', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11806', '流程spel达式定义导出', '11801', 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:export', '#', 103, 1, sysdate(), NULL, NULL, '');
