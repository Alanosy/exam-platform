package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.group.UserGroup;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.service.IUserService;
import cn.org.alan.exam.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/25 15:50
 */
@RestController
@RequestMapping("/api/user")
public class UserController {


    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private HttpServletRequest request;
    @Resource
    private IUserService iUserService;


    @GetMapping("/getUser")
//     @PreAuthorize("hasAnyAuthority('role_student')")
    public Authentication getUser(Authentication authentication) {
        return authentication;
    }


    /**
     * 创建用户，教师只能创建学生，管理员可以创建教师和学生
     *
     * @param userForm 请求参数，用户名、真实姓名[、角色id]
     * @return 响应结果
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> createUser(@Validated(UserGroup.CreateUserGroup.class) @RequestBody UserForm userForm) {
        return iUserService.createUser(userForm, jwtUtil.getPermission(request));
    }

    /**
     * 用户修改密码
     * @param userForm 入参
     * @return 响应结果
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> updatePassword(@Validated(UserGroup.UpdatePasswordGroup.class) @RequestBody UserForm userForm){
       return iUserService.updatePassword(jwtUtil.getUserId(request),userForm);
    }
}
