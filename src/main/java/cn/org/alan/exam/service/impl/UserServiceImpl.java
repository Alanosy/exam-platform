package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.model.vo.UserVO;
import cn.org.alan.exam.service.IUserService;
import cn.org.alan.exam.util.DateTimeUtil;
import cn.org.alan.exam.util.SecurityUtil;
import cn.org.alan.exam.util.excel.ExcelUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;


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
    @Resource
    private GradeMapper gradeMapper;


    @Override
    public Result<String> createUser(UserForm userForm) {

        userForm.setPassword(new BCryptPasswordEncoder().encode("123456"));

        //教师只能创建学生
        if ("role_teacher".equals(SecurityUtil.getRole())) {
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
    public Result<String> updatePassword(UserForm userForm) {
        if (!userForm.getNewPassword().equals(userForm.getCheckedPassword())) {
            return Result.failed("两次密码不一致");
        }
        Integer userId = SecurityUtil.getUserId();
        if (!userMapper.selectById(userId).getPassword().equals(userForm.getOriginPassword())) {
            return Result.failed("旧密码错误");
        }
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

    @Override
    public Result<String> deleteBatchByIds(String ids) {
        return null;
    }

    @SneakyThrows
    @Override
    @Transactional
    public Result<String> importUsers(MultipartFile file) {
        //文件类型判断
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            return Result.failed("文件类型必须是xls或xlsx");
        }

        //读取文件
        List<UserForm> list = ExcelUtils.readMultipartFile(file, UserForm.class);
        //参数补充
        list.forEach(userForm -> {
            userForm.setPassword(new BCryptPasswordEncoder().encode("123456"));
            userForm.setCreateTime(DateTimeUtil.getDateTime());
            if (userForm.getRoleId() == null) {
                userForm.setRoleId(1);
            }
        });

        if (list.size() > 300) {
            return Result.failed("表中最多存放300条数据");
        }
        userMapper.insertBatchUser(userConverter.listFromToEntity(list));
        return Result.success("用户导入成功");
    }

    @Override
    public Result<UserVO> info() {
        UserVO userVo = userMapper.info(SecurityUtil.getUserId());
        return Result.success(null,userVo);
    }

    @Override
    public Result<String> joinGrade(String code) {
        //获取班级信息
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>().eq(Grade::getCode, code);
        Grade grade = gradeMapper.selectOne(wrapper);
        if (Objects.isNull(grade)){
            return Result.failed("班级口令不存在");
        }
        User user = new User();
        user.setGradeId(grade.getGradeId());
        int updated = userMapper.updateById(user);
        if (updated>0){
            return Result.success("加入班级："+grade.getGradeName()+"成功");
        }
        return Result.failed("加入失败");
    }

    @Override
    public Result<IPage<UserVO>> pagingUser(Integer pageNum, Integer pageSize, Integer gradeId, String realName) {
        IPage<UserVO> page = new Page<>(pageNum,pageSize);
        if (SecurityUtil.getRole().equals("role_teacher")){
            page = userMapper.pagingUser(page,gradeId,realName,1);
        }else {
            page = userMapper.pagingUser(page,gradeId,realName,null);
        }

        return Result.success(null,page);
    }
}
