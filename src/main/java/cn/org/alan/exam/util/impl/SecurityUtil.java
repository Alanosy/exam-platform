package cn.org.alan.exam.util.impl;

import cn.org.alan.exam.common.exception.AppException;
import cn.org.alan.exam.security.SysUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/30 0:10
 */
@Slf4j
public class SecurityUtil {

    private SecurityUtil(){}

    /**
     * 获取当前用户id
     * @return 用户id
     */
    public static Integer getUserId(){
        SysUserDetails user = (SysUserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return user.getUser().getId();
    }

    /**
     * 获取当前用户角色
     * @return 角色
     */
    public static String getRole(){
        List<? extends GrantedAuthority> list = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().toList();
        return list.get(0).toString();
    }
    /**
     * 获取当前用户角色代码 1：学生、2：教师、3管理员
     * @return 角色
     */
    public static Integer getRoleCode(){
        List<? extends GrantedAuthority> list = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().toList();
        String roleName = list.get(0).toString();
        Integer roleCode;
        if("role_admin".equals(roleName)){
            roleCode=3;
        } else if ("role_teacher".equals(roleName)) {
            roleCode=2;
        } else if ("role_student".equals(roleName)) {
            roleCode=1;
        }else {
            throw new AppException("无法获取角色代码");
        }
        return roleCode;
    }

    /**
     * 获取当前用户所在班级Id
     * @return
     */
    public static Integer getGradeId(){
        SysUserDetails user = (SysUserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return user.getUser().getGradeId();
    }


}
