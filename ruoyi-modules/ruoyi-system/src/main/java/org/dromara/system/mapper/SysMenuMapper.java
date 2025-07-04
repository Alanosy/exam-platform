package org.dromara.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.system.domain.SysMenu;
import org.dromara.system.domain.vo.SysMenuVo;

import java.util.List;

/**
 * 菜单表 数据层
 *
 * @author Lion Li
 */
public interface SysMenuMapper extends BaseMapperPlus<SysMenu, SysMenuVo> {

    default String buildMenuByUserSql(Long userId) {
        return """
                select menu_id from sys_role_menu where role_id in (
                    select role_id from sys_user_role where user_id = %d
                )
            """.formatted(userId);
    }

    default String buildMenuByRoleSql(Long roleId) {
        return """
                select menu_id from sys_role_menu where role_id = %d
            """.formatted(roleId);
    }

    default String buildParentMenuByRoleSql(Long roleId) {
        return """
                select parent_id from sys_menu where menu_id in (
                    select menu_id from sys_role_menu where role_id = %d
                )
            """.formatted(roleId);
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    default List<String> selectMenuPermsByUserId(Long userId) {
        return this.selectObjs(
            new LambdaQueryWrapper<SysMenu>()
                .select(SysMenu::getPerms)
                .inSql(SysMenu::getMenuId, this.buildMenuByUserSql(userId))
        );
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    default List<String> selectMenuPermsByRoleId(Long roleId) {
        return this.selectObjs(
            new LambdaQueryWrapper<SysMenu>()
                .select(SysMenu::getPerms)
                .inSql(SysMenu::getMenuId, this.buildMenuByRoleSql(roleId))
        );
    }

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    default List<SysMenu> selectMenuTreeAll() {
        LambdaQueryWrapper<SysMenu> lqw = new LambdaQueryWrapper<SysMenu>()
            .in(SysMenu::getMenuType, SystemConstants.TYPE_DIR, SystemConstants.TYPE_MENU)
            .eq(SysMenu::getStatus, SystemConstants.NORMAL)
            .orderByAsc(SysMenu::getParentId)
            .orderByAsc(SysMenu::getOrderNum);
        return this.selectList(lqw);
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId            角色ID
     * @param menuCheckStrictly 菜单树选择项是否关联显示
     * @return 选中菜单列表
     */
    default List<Long> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysMenu::getMenuId)
            .inSql(SysMenu::getMenuId, buildMenuByRoleSql(roleId))
            .orderByAsc(SysMenu::getParentId)
            .orderByAsc(SysMenu::getOrderNum);
        if (menuCheckStrictly) {
            wrapper.notInSql(SysMenu::getMenuId, this.buildParentMenuByRoleSql(roleId));
        }
        return this.selectObjs(wrapper);
    }

}
