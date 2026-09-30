package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 角色表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 获取角色代码
     *
     * @param roleId 角色ID
     * @return 结果集
     */
    List<String> selectCodeById(Integer roleId);

}
