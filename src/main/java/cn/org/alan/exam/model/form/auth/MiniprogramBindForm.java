package cn.org.alan.exam.model.form.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 小程序绑定账号表单
 *
 * @Author Alan
 * @Version
 * @Date 2024/9/28
 */
@Data
@ApiModel("小程序绑定账号表单")
public class MiniprogramBindForm {

    @ApiModelProperty("微信登录code")
    @NotBlank(message = "微信登录code不能为空")
    private String code;

    @ApiModelProperty("用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能为空")
    private String password;
}
