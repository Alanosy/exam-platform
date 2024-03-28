package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.AuthService;


/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:33 PM
 */
public class AuthServiceImpl implements AuthService {


    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @Override
    public Result login(String username, String password) {

        return null;
    }

    /**
     * 注销
     */
    @Override
    public void logout() {

    }

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    @Override
    public Result getCaptcha() {
        return null;
    }
}
