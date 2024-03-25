package cn.org.alan.exam.service;

import cn.org.alan.exam.model.dto.UserAuthInfo;
import cn.org.alan.exam.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IUserService extends IService<User> {
    /**
     * 根据用户名获取认证信息
     *
     * @param username 用户名
     * @return {@link UserAuthInfo}
     */

    User getUserAuthInfo(String username);

}
