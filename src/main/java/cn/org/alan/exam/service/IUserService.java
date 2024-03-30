package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IUserService extends IService<User> {

    /**
     * 创建用户，教师只能创建学生，管理员可以创建教师和学生
     *
     * @param userForm 请求参数，用户名、真实姓名[、角色id]
     * @return 响应结果
     */
    Result<String> createUser(UserForm userForm);


    /**
     * 修改密码
     * @param userForm 入参
     * @return 响应结果
     */
    Result<String> updatePassword( UserForm userForm);

    /**
     * 批量删除用户
     * @param ids 用户id
     * @return 响应结果
     */
    Result<String> deleteBatchByIds(String ids);

    /**
     * Excel导入用户信息
     * @param file 文件
     * @return 响应结果
     */
    Result<String> importUsers(MultipartFile file);
}
