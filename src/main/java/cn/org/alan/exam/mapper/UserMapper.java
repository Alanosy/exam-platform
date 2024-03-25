package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.dto.UserAuthInfo;
import cn.org.alan.exam.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Repository
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据用户名获取认证信息
     *
     * @param username
     * @return
     */
    User getUserAuthInfo(@Param("username") String username);
}
