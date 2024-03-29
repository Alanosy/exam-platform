package cn.org.alan.exam.util;

import cn.org.alan.exam.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/30 0:10
 */
public class SecurityUtil {
    private SecurityUtil(){}

    /**
     * 获取当前用户id
     * @return 用户id
     */
    public static Integer getUserId(){
        User user = (User) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return user.getUserId();
    }

    /**
     * 获取当前用户角色
     * @return 角色
     */
    public static String getRole(){
        List<? extends GrantedAuthority> list = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().toList();
        return list.get(0).toString();
    }
}
