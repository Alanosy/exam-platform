package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.common.group.UserGroup;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.model.vo.UserVO;
import cn.org.alan.exam.service.IUserService;
import cn.org.alan.exam.util.AliOSSUtil;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 用户管理
 *
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/25 15:50
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private IUserService iUserService;


    /**
     * 获取用户登录信息
     *
     * @return 响应结果
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<UserVO> info() {
        return iUserService.info();
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
        return iUserService.createUser(userForm);
    }

    /**
     * 用户修改密码
     *
     * @param userForm 入参
     * @return 响应结果
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> updatePassword(@Validated(UserGroup.UpdatePasswordGroup.class) @RequestBody UserForm userForm) {
        return iUserService.updatePassword(userForm);
    }

    /**
     * 批量删除用户
     *
     * @param ids 字符串ids
     * @return 相应结果
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteBatchByIds(@PathVariable("ids") String ids) {
        return iUserService.deleteBatchByIds(ids);
    }

    /**
     * Excel导入用户数据
     *
     * @param file 文件
     * @return 响应结果
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) {
        return iUserService.importUsers(file);
    }


    /**
     * 用户加入班级，只有学生才能加入班级
     *
     * @param code 班级口令
     * @return 响应
     */
    @PutMapping("/grade/join")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> joinGrade(@RequestParam("code") String code) {
        return iUserService.joinGrade(code);
    }

    /**
     * 分页获取用户信息
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param gradeId  班级Id
     * @param realName 真实姓名
     * @return 响应结果
     */
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<UserVO>> pagingUser(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "gradeId", required = false) Integer gradeId,
                                            @RequestParam(value = "realName", required = false) String realName) {
        return iUserService.pagingUser(pageNum, pageSize, gradeId, realName);
    }

    /**
     * 用户上传头像
     *
     * @param file 文件
     * @return 响应结果
     */
    @PutMapping("/uploadAvatar")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return iUserService.uploadAvatar(file);
    }
}
