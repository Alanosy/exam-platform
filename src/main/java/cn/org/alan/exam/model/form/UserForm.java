package cn.org.alan.exam.model.form;

import cn.org.alan.exam.group.UserGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/29 15:17
 */
@Data
public class UserForm {

    private Integer userId;
    private String password;
    private LocalDateTime createTime;


    @NotBlank(groups = {UserGroup.CreateUserGroup.class},message = "用户名不能为空")
    private String userName;
    @NotBlank(groups = {UserGroup.CreateUserGroup.class},message = "真实姓名不能为空")
    private String realName;
    private Integer roleId;


    @NotBlank(groups = {UserGroup.UpdatePasswordGroup.class},message = "原密码不能为空")
    private String originPassword;
    @NotBlank(groups = {UserGroup.UpdatePasswordGroup.class},message = "新密码不能为空")
    private String newPassword;
    @NotBlank(groups = {UserGroup.UpdatePasswordGroup.class},message = "校验密码不能为空")
    private String checkedPassword;

}
