package cn.org.alan.exam.security.service;


import cn.org.alan.exam.mapper.RoleMapper;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.security.model.SysUserDetails;
import cn.org.alan.exam.service.IRoleService;
import cn.org.alan.exam.service.IUserService;
import cn.org.alan.exam.service.impl.UserServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统用户认证
 *
 * @author haoxr
 */
@Service
@RequiredArgsConstructor
public class SysUserDetailsService implements UserDetailsService {
    @Resource
    private IUserService iUserService;
    @Resource
    private RoleMapper roleMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户获取用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(username), User::getUserName, username);
        User user = iUserService.getOne(wrapper);

        //判读用户名是否存在
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("该用户不存在");
        }
        System.out.println(user);

        //根据用户Id获取权限
        List<String> permissions = roleMapper.selectCodeById(user.getRoleId());

        //数据库获取的权限是字符串springSecurity需要实现GrantedAuthority接口类型，所有这里做一个类型转换
        List<SimpleGrantedAuthority> userPermissions = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("role_" + permission))
                .collect(Collectors.toList());

        //创建一个sysUserDetails对象，该类实现了UserDetails接口
        SysUserDetails sysUserDetails = new SysUserDetails(user);
        //把转型后的权限放进sysUserDetails对象
        sysUserDetails.setPermissions(userPermissions);
        //sysUserDetails，密码校验由框架完成
        return sysUserDetails;
    }

}
