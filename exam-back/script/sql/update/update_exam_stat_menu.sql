-- ----------------------------
-- 考试统计模块菜单
--
-- 说明：统计页面路由是 /system/stat，与「阅卷管理 / 监考中心」同级挂在顶层（parent_id = 0）。
--       单场考试详情、考生明细、试题分析、知识点分析都是详情页路由，不单独建菜单，
--       统一由 exam:stat:list 控制。
--       数据库：ry-cloud（sys_menu 在配置库，不在业务库）
-- ----------------------------

-- 考试统计菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2120', '考试统计', '0', '12', 'stat', 'system/stat/dashboard/index', '',
       1, 1, 'C', '0', '0', 'exam:stat:list', 'chart',
       103, 1, sysdate(), NULL, NULL, '考试大盘概览、成绩统计、试题分析、知识点薄弱分析'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2120'));

-- 统计查询
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2121', '统计查询', '2120', '1', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:stat:query', '#',
       103, 1, sysdate(), NULL, NULL, '查询考试统计、考生明细、试题分析、知识点分析'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2121'));

-- 重新计算
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2122', '统计重算', '2120', '2', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:stat:recalc', '#',
       103, 1, sysdate(), NULL, NULL, '手动触发一场考试的全量统计重算'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2122'));

-- 作废标记
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2123', '统计作废', '2120', '3', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:stat:exclude', '#',
       103, 1, sysdate(), NULL, NULL, '把作弊 / 异常的答卷标记作废，不计入统计'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2123'));

-- 统计导出
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2124', '统计导出', '2120', '4', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:stat:export', '#',
       103, 1, sysdate(), NULL, NULL, '导出成绩单、题目分析、知识点分析与答卷明细'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2124'));
