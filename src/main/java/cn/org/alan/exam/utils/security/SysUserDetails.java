package cn.org.alan.exam.utils.security;


import cn.org.alan.exam.model.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 用户对象
 *
 * @author haoxr
 * @since 3.0.0
 */
@Data
@NoArgsConstructor
public class SysUserDetails implements UserDetails {
    // 权限列表
    private List<SimpleGrantedAuthority> permissions;
    // 用户信息
    private User user;
    // 用户名称
    private String username;

    public SysUserDetails(User user) {
        this.user = user;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions;
    }

    public void setPermissions(List<SimpleGrantedAuthority> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String getPassword() {
        String myPassword=user.getPassword();
        user.setPassword("");
        return myPassword;
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
