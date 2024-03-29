package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.service.IUserService;
import cn.org.alan.exam.util.DateTimeUtil;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.context.ApplicationContextException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;


/**
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private HttpServletRequest request;
    @Resource
    private UserConverter userConverter;


    @Override
    public Result<String> createUser(UserForm userForm, String permission) {

        userForm.setCreateTime(DateTimeUtil.getDateTime());
        userForm.setPassword(new BCryptPasswordEncoder().encode("123456"));
        //教师只能创建学生
        if ("role_teacher".equals(permission)) {
            userForm.setRoleId(1);
        }

        //避免管理员创建用户不传递角色
        if (userForm.getRoleId() == null || userForm.getRoleId() == 0) {
            return Result.failed("请选择用户角色");
        }

        User user = userConverter.fromToEntity(userForm);

        userMapper.insert(user);
        return Result.failed("用户创建成功");

    }

    @Override
    public Result<String> updatePassword(Integer userId, UserForm userForm) {
        if (!userForm.getNewPassword().equals(userForm.getCheckedPassword())) {
            return Result.failed("两次密码不一致");
        }
        if (!userMapper.selectById(userId).getPassword().equals(userForm.getOriginPassword())) {
            return Result.failed("旧密码错误");
        }

        userForm.setUserId(userId);
        //密码加密
        userForm.setPassword(new BCryptPasswordEncoder().encode(userForm.getNewPassword()));


        int updated = userMapper.updateById(userConverter.fromToEntity(userForm));
        //密码修改成功清除redis的token，让用户重新登录
        if (updated > 0) {
            stringRedisTemplate.delete(request.getSession().getId() + "token");
            return Result.success("修改成功，请重新登录");
        }
        return Result.failed("旧密码错误");

    }
}
