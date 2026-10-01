-- ----------------------------
-- 防作弊模块菜单（可选）
--
-- 说明：监考页面路由是 /system/proctor，与「阅卷管理」同级挂在顶层（parent_id = 0）。
--       菜单按 RuoYi 的规则由「父目录 path + 子菜单 path」拼出来，这里 path 直接给 proctor，
--       所以完整访问路径是 /system/proctor（顶层目录 system 由框架拼）。
--       也可以在系统后台「菜单管理」里手工添加，效果一样。
--       数据库：ry-cloud（sys_menu 在配置库，不在业务库）
-- ----------------------------

-- 监考中心菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2103', '监考中心', '0', '11', 'proctor', 'system/proctor/index', '',
       1, 1, 'C', '0', '0', 'exam:proctor:list', 'monitor',
       103, 1, sysdate(), NULL, NULL, '实时查看考生切屏 / 粘贴 / 摄像头抓拍等防作弊记录'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2103'));

-- 监考记录查询
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2104', '监考查询', '2103', '1', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:proctor:query', '#',
       103, 1, sysdate(), NULL, NULL, '查询监考会话、事件流水与抓拍'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2104'));

-- 考生端上报接口（答题页调用，不需要菜单，仅登记权限标识）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2105', '防作弊上报', '2103', '2', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:proctor:report', '#',
       103, 1, sysdate(), NULL, NULL, '答题页上报切屏 / 粘贴 / 抓拍等事件'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2105'));
