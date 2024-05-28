package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.group.UserGroup;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.Auth.LoginForm;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.service.IAuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 权限管理
 *
 * @Author WeiJin
 * @Version
 * @Date 2024/3/25 11:05 AM
 */
@RestController
@RequestMapping("/api/auths")
public class AuthController {


    @Resource
    private IAuthService iAuthService;

    /**
     * 用户登录
     * @param request request对象，用户获取sessionId
     * @param user 用户信息
     * @return token
     */
    @PostMapping("/login")
    public Result<String> login(HttpServletRequest request,
                                @Validated @RequestBody LoginForm loginForm) {

        return iAuthService.login(request,loginForm);
    }

    /**
     * 用户注销
     * @param request request对象，需要清除session里面的内容
     * @return 响应结果
     */
    @DeleteMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        return iAuthService.logout(request);
    }

    /**
     * 用户注册，只能注册学生
     * @param request  request对象，用于获取sessionId
     * @param userForm 用户信息
     * @return 响应结果
     */
    @PostMapping("/register")
    public Result<String> register(HttpServletRequest request,
                                   @RequestBody @Validated(UserGroup.RegisterGroup.class) UserForm userForm) {
        return iAuthService.register(request, userForm);
    }

    /**
     * 获取图片验证码
     * @param request  request对象，获取sessionId
     * @param response response对象，响应图片
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        iAuthService.getCaptcha(request, response);
    }

    /**
     * 校验验证码
     * @param request request对象，获取sessionId
     * @param code    用户输入的验证码
     * @return 响应结果
     */
    @PostMapping("/verifyCode/{code}")
    public Result<String> verifyCode(HttpServletRequest request, @PathVariable("code") String code) {
        return iAuthService.verifyCode(request, code);
    }
    @PostMapping("/track-presence")
    public Result<String> trackPresence(HttpServletRequest request) {
        return iAuthService.sendHeartbeat(request);
    }

}
