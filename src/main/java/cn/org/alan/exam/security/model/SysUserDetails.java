package cn.org.alan.exam.security.model;


import cn.org.alan.exam.model.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 用户对象
 *
 * @author haoxr
 * @since 3.0.0
 */
@Data
@NoArgsConstructor
public class SysUserDetails implements UserDetails {
    private List<SimpleGrantedAuthority> permissions;
    private User user;

    private String username;


    public SysUserDetails(User user) {
        this.user = user;
        // this.userId = user.getUserId();
        // Set<String> roles = user.getRoles();
        // Set<SimpleGrantedAuthority> authorities;
        // if (CollectionUtil.isNotEmpty(roles)) {
        //     authorities = roles.stream()
        //             .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // 标识角色
        //             .collect(Collectors.toSet());
        // } else {
        //     authorities = Collections.EMPTY_SET;
        // }
        // this.authorities = authorities;
        // this.username = user.getUsername();
        // this.password = user.getPassword();
        // this.enabled = ObjectUtil.equal(user.getStatus(), 1);
        // this.perms = user.getPerms();
        // this.deptId = user.getDeptId();
        // this.dataScope = user.getDataScope();
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
