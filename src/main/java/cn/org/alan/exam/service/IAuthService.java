package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.UserForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:33 PM
 */
public interface IAuthService {
    /**
     * 登录
     *
     * @param username 用户名
     * @param request request对象，用户获取sessionId
     * @param password 密码
     * @return token
     */
    Result<String> login(HttpServletRequest request,String username, String password);

    /**
     * 用户注销
     * @param request request对象，需要清除session里面的内容
     * @return 响应结果
     */
    Result<String> logout(HttpServletRequest request);

    /**
     * 获取图片验证码
     *
     * @param request  request对象，获取sessionId
     * @param response response对象，响应图片
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);

    /**
     * 校验验证码
     *
     * @param request request对象获取sessionId
     * @param code    用户输入的验证码
     * @return 响应结果
     */
    Result<String> verifyCode(HttpServletRequest request, String code);

    /**
     * 用户注册，只能注册学生
     *
     * @param request  request对象，用于获取sessionId
     * @param userForm 用户信息
     * @return 响应结果
     */
    Result<String> register(HttpServletRequest request, UserForm userForm);
}