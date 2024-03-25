package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // List<User> getText(Integer id);
}
