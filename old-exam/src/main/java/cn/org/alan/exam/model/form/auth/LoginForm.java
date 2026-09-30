package cn.org.alan.exam.model.form.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录表单
 *
 * @Author Alan
 * @Version
 * @Date 2024/5/6 10:07 PM
 */
@Data
public class LoginForm {

    // 用户名
    @NotBlank(message = "用户名不能为空")
    private String username;

    // 密码
    @NotBlank(message = "密码不能为空")
    private String password;
}
