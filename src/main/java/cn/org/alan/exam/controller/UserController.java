package cn.org.alan.exam.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *     用户管理
 * </p>
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/25 15:50
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/getUser")
    // @PreAuthorize("hasAnyAuthority('role_student')")
    public Authentication getUser(Authentication authentication){
        return authentication;
    }
}
