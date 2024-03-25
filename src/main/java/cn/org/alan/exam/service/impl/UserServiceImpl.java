package cn.org.alan.exam.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.org.alan.exam.model.dto.UserAuthInfo;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.service.IRoleService;
import cn.org.alan.exam.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private IRoleService iRoleService;
    /**
     * 根据用户名获取认证信息
     *
     * @param username 用户名
     * @return 用户认证信息 {@link UserAuthInfo}
     */
    @Override
    public User getUserAuthInfo(String username) {
        User userAuthInfo = this.baseMapper.getUserAuthInfo(username);
        // if (userAuthInfo != null) {
        //     Set<String> roles = userAuthInfo.getRoles();

            // 获取最大范围的数据权限
            // Integer dataScope = iRoleService.getMaximumDataScope(roles);
            // userAuthInfo.setDataScope(dataScope);
        // }
        return userAuthInfo;
    }
}
