package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface RoleMapper extends BaseMapper<Role> {

    List<String> selectCodeById(Integer roleId);
}
