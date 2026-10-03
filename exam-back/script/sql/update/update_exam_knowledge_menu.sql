-- ----------------------------
-- 知识点管理菜单
--
-- 说明：知识点是全局共享的两级树（章节 → 知识点），与「题库分类」平级挂在
--       「试题管理」目录（menu_id = 2104612941584130050）下，path 给 knowledge，
--       完整访问路径 /questions/knowledge。
--       数据库：ry-cloud（sys_menu 在配置库，不在 ry-exam）
--       admin 超级管理员默认拥有全部权限，不需要插 sys_role_menu；
--       其它角色请在「角色管理」里自行勾选下面的按钮权限。
-- ----------------------------

-- 知识点管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2104780000000000001', '知识点管理', '2104612941584130050', '3', 'knowledge', 'system/knowledge/index', '',
       1, 0, 'C', '0', '0', 'system:knowledge:list', 'collection-tag',
       103, 1, sysdate(), NULL, NULL, '维护两级知识点（章节 → 知识点），供出题、组卷、错题归因使用'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2104780000000000001'));

-- 知识点查询
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2104780000000000002', '知识点查询', '2104780000000000001', '1', '', NULL, '',
       1, 0, 'F', '0', '0', 'system:knowledge:query', '#',
       103, 1, sysdate(), NULL, NULL, '查询知识点树'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2104780000000000002'));

-- 知识点新增
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2104780000000000003', '知识点新增', '2104780000000000001', '2', '', NULL, '',
       1, 0, 'F', '0', '0', 'system:knowledge:add', '#',
       103, 1, sysdate(), NULL, NULL, '新增章节或知识点'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2104780000000000003'));

-- 知识点修改
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2104780000000000004', '知识点修改', '2104780000000000001', '3', '', NULL, '',
       1, 0, 'F', '0', '0', 'system:knowledge:edit', '#',
       103, 1, sysdate(), NULL, NULL, '修改章节或知识点'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2104780000000000004'));

-- 知识点删除
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2104780000000000005', '知识点删除', '2104780000000000001', '4', '', NULL, '',
       1, 0, 'F', '0', '0', 'system:knowledge:remove', '#',
       103, 1, sysdate(), NULL, NULL, '删除章节或知识点（有子节点或已被试题引用时禁止）'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2104780000000000005'));
