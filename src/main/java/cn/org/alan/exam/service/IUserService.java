package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.model.vo.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IUserService extends IService<User> {

    /**
     * 创建用户，教师只能创建学生，管理员可以创建教师和学生
     * @param userForm
     * @return
     */
    Result<String> createUser(UserForm userForm);

    /**
     * 用户修改密码
     * @param userForm
     * @return
     */
    Result<String> updatePassword(UserForm userForm);

    /**
     * 批量删除用户
     * @param ids
     * @return
     */
    Result<String> deleteBatchByIds(String ids);

    /**
     * Excel导入用户数据
     * @param file
     * @return
     */
    Result<String> importUsers(MultipartFile file);

    /**
     * 获取用户个人信息
     * @return 响应结果
     */
    Result<UserVO> info();

    /**
     * 用户加入班级，只有学生才能加入班级
     * @param code
     * @return
     */
    Result<String> joinGrade(String code);

    /**
     * 教师和管理员 用户管理 分页获取用户信息
     * @param pageNum
     * @param pageSize
     * @param gradeId
     * @param realName
     * @return
     */
    Result<IPage<UserVO>> pagingUser(Integer pageNum, Integer pageSize, Integer gradeId, String realName);


    /**
     * 用户上传头像
     * @param file
     * @return
     */
    Result<String> uploadAvatar(MultipartFile file);
}
