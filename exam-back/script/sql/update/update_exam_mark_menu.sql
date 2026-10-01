-- ----------------------------
-- 阅卷模块菜单（可选）
--
-- 说明：阅卷页面路由是 /system/mark，菜单按 RuoYi 的规则由「父目录 path + 子菜单 path」拼出来。
--       这里默认挂到「系统管理」目录（menu_id = 1，path = system）下，
--       与「考试管理」/system/exam 同级；如果你们单独建了考试目录，
--       把下面 parent_id 换成那个目录的 menu_id 即可。
--       菜单也可以在系统后台「菜单管理」里手工添加，效果一样。
-- ----------------------------

-- 阅卷列表菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2101', '阅卷管理', '1', '10', 'mark', 'system/mark/index', '',
       1, 1, 'C', '0', '0', 'exam:mark:list', 'clipboard',
       103, 1, sysdate(), NULL, NULL, '按考试查看待阅答卷并逐题打分'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2101'));

-- 阅卷打分按钮权限（列表页也需要，打分接口单独校验）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2102', '阅卷打分', '2101', '1', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:mark:edit', '#',
       103, 1, sysdate(), NULL, NULL, '主观题打分 / 完成阅卷'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2102'));
