package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/25 11:05 AM
 */
@RestController
@RequestMapping("/auths")
public class AuthController {

    // @Autowired
    // private AuthService
    // @PostMapping("/login")
    public Result login(@RequestParam String username,@RequestParam String password) {

        return null;
    }

    @DeleteMapping("/logout")
    public Result logout() {
        // authService.logout();
        return Result.success();
    }

    @GetMapping("/captcha")
    public Result getCaptcha() {
        return null;
    }
}
