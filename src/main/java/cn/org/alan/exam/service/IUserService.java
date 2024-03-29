package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IUserService extends IService<User> {

    /**
     * 创建用户，教师只能创建学生，管理员可以创建教师和学生
     *
     * @param userForm 请求参数，用户名、真实姓名[、角色id]
     * @param permission 当前用户角色
     * @return 响应结果
     */
    Result<String> createUser(UserForm userForm, String permission);


    /**
     * 修改密码
     * @param userId 用户id
     * @param userForm 入参
     * @return 响应结果
     */
    Result<String> updatePassword(Integer userId, UserForm userForm);

}
