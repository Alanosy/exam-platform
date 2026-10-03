-- ----------------------------
-- AI 能力权限菜单
--
-- 背景：ruoyi-exam-ai（Java 网关）+ ruoyi-exam-agent（Python Agent）上线后，
--       AI 出题 / AI 试卷审查 / AI 错题诊断 / AI 主观题预评都要走 @SaCheckPermission
--       校验，权限标识必须先落库，否则接口一律 401。
--
-- 说明：AI 没有独立页面，全部能力以抽屉形式嵌在题库、试卷、阅卷、错题本里，
--       所以这里只建一个**隐藏目录（visible=1 表示隐藏）**承载权限按钮，
--       侧边栏不会多出一个点进去是空白的菜单。
--
-- 数据库：ry-cloud（sys_menu 在配置库，不在 ry-exam）
-- 注意：admin 超级管理员默认拥有全部权限，不需要插 sys_role_menu；
--       其它角色（如「考试管理员」）请在「角色管理」里自行勾选下方两个按钮权限。
-- ----------------------------

-- AI 能力（隐藏目录，仅用于承载权限）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2130', 'AI 能力', '0', '13', 'ai', NULL, '',
       1, 0, 'M', '1', '0', '', 'magic-stick',
       103, 1, sysdate(), NULL, NULL, 'AI 出题 / 质检 / 试卷审查 / 错题诊断 / 主观题预评的权限容器（无独立页面）'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2130'));

-- AI 查询：模型清单、Skill 自测
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2131', 'AI 查询', '2130', '1', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:ai:list', '#',
       103, 1, sysdate(), NULL, NULL, '查看模型清单与调用通用 Skill'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2131'));

-- AI 使用：出题、质检、试卷审查、主观题评分
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`,
                        `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
                        `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT '2132', 'AI 使用', '2130', '2', '', NULL, '',
       1, 0, 'F', '0', '0', 'exam:ai:edit', '#',
       103, 1, sysdate(), NULL, NULL, '调用 AI 出题、质检、试卷审查与主观题评分'
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t
                  WHERE EXISTS (SELECT 1 FROM `sys_menu` m WHERE m.`menu_id` = '2132'));
