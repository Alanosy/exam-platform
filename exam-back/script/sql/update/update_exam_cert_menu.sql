-- ----------------------------
-- 证书模块菜单
--
-- 说明：证书管理（管理端）与监考中心同级挂在顶层（parent_id = 0），path 给 cert，
--       完整访问路径 /system/cert；「我的证书」是考生端页面，path 给 exam/certs，
--       与「考试记录」(exam/records) 并列，完整访问路径 /exam/certs。
--       数据库：ry-cloud（sys_menu 在配置库，不在业务库）
--       也可以在系统后台「菜单管理」里手工添加，效果一样。
-- ----------------------------

-- 证书管理（管理端）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2106', '证书管理', '0', '13', 'cert', 'system/cert/index', '',
       1, 1, 'C', '0', '0', 'exam:cert:list', 'certificate',
       103, 1, sysdate(), NULL, NULL, '维护证书模板、查看与吊销已颁发的证书'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2106'));

-- 证书查询
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2107', '证书查询', '2106', '1', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:cert:query', '#',
       103, 1, sysdate(), NULL, NULL, '查询证书模板与颁发记录'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2107'));

-- 证书新增
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2108', '证书新增', '2106', '2', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:cert:add', '#',
       103, 1, sysdate(), NULL, NULL, '新增证书模板'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2108'));

-- 证书修改
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2109', '证书修改', '2106', '3', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:cert:edit', '#',
       103, 1, sysdate(), NULL, NULL, '修改证书模板'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2109'));

-- 证书删除
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2110', '证书删除', '2106', '4', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:cert:remove', '#',
       103, 1, sysdate(), NULL, NULL, '删除证书模板'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2110'));

-- 证书颁发 / 吊销
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2111', '证书颁发', '2106', '5', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:cert:issue', '#',
       103, 1, sysdate(), NULL, NULL, '手动补发 / 吊销证书'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2111'));

-- 我的证书（考生端）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2112', '我的证书', '0', '14', 'exam/certs', 'exam/certs/index', '',
       1, 1, 'C', '0', '0', 'exam:cert:mine', 'certificate',
       103, 1, sysdate(), NULL, NULL, '考生查看自己获得的证书'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2112'));
