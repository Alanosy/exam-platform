package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:33 PM
 */
public interface AuthService {
    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    Result login(String username, String password);

    /**
     * 登出
     */
    void logout();

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    Result getCaptcha();
}