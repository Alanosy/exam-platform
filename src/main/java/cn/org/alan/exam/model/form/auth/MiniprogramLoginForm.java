package cn.org.alan.exam.model.form.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 小程序登录表单
 *
 * @Author Alan
 * @Version
 * @Date 2024/9/28
 */
@Data
@ApiModel("小程序登录表单")
public class MiniprogramLoginForm {

    @ApiModelProperty("微信登录code")
    @NotBlank(message = "微信登录code不能为空")
    private String code;

    @ApiModelProperty("加密数据（可选，获取用户信息）")
    private String encryptedData;

    @ApiModelProperty("加密算法初始向量（可选）")
    private String iv;
}
