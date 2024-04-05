package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.vo.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


import java.util.List;

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

    // List<User> getText(Integer id);
    Integer removeUserGrade(List<Integer> userIds);

    /**
     * 批量添加用户
     * @param list
     * @return
     */
    Integer insertBatchUser(List<User> list);

    /**
     * 获取用户信息
     * @param userId 用户id
     * @return 响应
     */
    UserVO info(Integer userId);
}
