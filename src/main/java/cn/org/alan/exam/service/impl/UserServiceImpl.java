package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.UserForm;
import cn.org.alan.exam.model.vo.UserVO;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.service.IUserService;
import cn.org.alan.exam.util.impl.DateTimeUtil;
import cn.org.alan.exam.util.impl.SecurityUtil;
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

import java.util.Arrays;
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
    @Resource
    private IQuestionService iQuestionService;
    @Resource
    private UserGradeMapper userGradeMapper;

    /**
     * 创建用户，教师只能创建学生，管理员可以创建教师和学生
     *
     * @param userForm
     * @return
     */
    @Override
    public Result<String> createUser(UserForm userForm) {
        // 设置默认密码
        userForm.setPassword(new BCryptPasswordEncoder().encode("123456"));
        // 获取角色
        Integer roleCode = SecurityUtil.getRoleCode();
        // 教师只能创建学生
        if (roleCode == 2) {
            userForm.setRoleId(1);
        }
        // 避免管理员创建用户不传递角色
        if (userForm.getRoleId() == null || userForm.getRoleId() == 0) {
            return Result.failed("请选择用户角色");
        }
        User user = userConverter.fromToEntity(userForm);
        // 调用Mapper插入用户
        userMapper.insert(user);
        return Result.success("用户创建成功");

    }

    /**
     * 用户修改密码
     *
     * @param userForm
     * @return
     */
    @Override
    public Result<String> updatePassword(UserForm userForm) {
        Integer userId = SecurityUtil.getUserId();
        if (!userForm.getNewPassword().equals(userForm.getCheckedPassword())) {
            return Result.failed("两次密码不一致");
        }
        if (!new BCryptPasswordEncoder()
                .matches(userForm.getOriginPassword(), userMapper.selectById(userId).getPassword())) {
            return Result.failed("旧密码错误");
        }
        // 设置新加密后到密码配置好自己到userID
        userForm.setPassword(new BCryptPasswordEncoder().encode(userForm.getNewPassword()));
        userForm.setId(userId);
        // 转还为User实体
        User user = userConverter.fromToEntity(userForm);
        // 调用mapper更新用户密码
        int updated = userMapper.updateById(user);
        // 密码修改成功清除redis的token，让用户重新登录
        if (updated > 0) {
            stringRedisTemplate.delete("token:" + request.getSession().getId());
            return Result.success("修改成功，请重新登录");
        }
        return Result.failed("旧密码错误");

    }

    /**
     * 批量删除用户
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> userIds = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        if (userIds.isEmpty()) {
            return Result.failed("未传入用户Id");
        }
        // //删除用户证书
        // certificateUserMapper.deleteByUserIds(userIds);
        // //删除用户创建的考试
        // examMapper.deleteByUserIds(userIds);
        // //删除用户考试作答记录
        // examQuAnswerMapper.deleteByUserIds(userIds);
        // //删除用户练习记录
        // exerciseRecordMapper.deleteByUserIds(userIds);
        // //清除用户表中的班级id
        // List<Integer> gradeIds = gradeMapper.selectIdsByUserIds(userIds);
        // if (!gradeIds.isEmpty()) {
        //     userMapper.removeGradeIdByGradeIds(gradeIds);
        // }
        //
        // //删除用户创建的班级
        // gradeMapper.deleteByUserId(userIds);
        // //删除用户创建的可供学生练习的题库关联表
        // gradeExerciseMapper.deleteByUserIds(userIds);
        // //删除用户批改的主观题分数
        // manualScoreMapper.deleteByUserIds(userIds);
        // //删除公告与班级关联表
        // //1.获取公告id
        // List<Integer> noticeIds = noticeMapper.selectIdsByUserIds(userIds);
        // //2.删除公告与班级关联
        // if (!noticeIds.isEmpty()) {
        //     noticeGradeMapper.deleteByNoticeIds(noticeIds);
        // }
        // //删除用户创建的公告
        // noticeMapper.deleteByUserIds(userIds);
        //
        // //删除试题选项
        // //1.获取用户创建的试题id
        // List<Integer> quIds = questionMapper.selectIdsByUserIds(userIds);
        // //2.根据试题id列表删除选项
        // if (!quIds.isEmpty()) {
        //     optionMapper.deleteBatchByQuIds(quIds);
        // }
        // //删除用户创建的试题
        // questionMapper.deleteByUserIds(userIds);
        // //删除用户创建的题库
        // repoMapper.deleteByUserIds(userIds);
        // //删除用户的错题本
        // userBookMapper.deleteByUserIds(userIds);
        // //删除用户
        Integer row = userMapper.deleteByUserIds(userIds);
        if (row < 1) {
            return Result.failed("删除失败");
        }
        return Result.success("删除成功");
    }

    /**
     * Excel导入用户数据
     *
     * @param file
     * @return
     */
    @SneakyThrows(Exception.class)
    @Override
    @Transactional
    public Result<String> importUsers(MultipartFile file) {
        // 文件类型判断
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            return Result.failed("文件类型必须是xls或xlsx");
        }
        // 读取文件
        List<UserForm> list = ExcelUtils.readMultipartFile(file, UserForm.class);
        // 参数补充
        list.forEach(userForm -> {
            userForm.setPassword(new BCryptPasswordEncoder().encode("123456"));
            userForm.setCreateTime(DateTimeUtil.getDateTime());
            if (userForm.getRoleId() == null) {
                userForm.setRoleId(1);
            }
        });
        // 判断条数是否过多
        if (list.size() > 300) {
            return Result.failed("表中最多存放300条数据");
        }
        userMapper.insertBatchUser(userConverter.listFromToEntity(list));
        return Result.success("用户导入成功");
    }

    /**
     * 获取用户个人信息
     *
     * @return
     */
    @Override
    public Result<UserVO> info() {
        // 获取用户信息
        Integer userId = SecurityUtil.getUserId();
        UserVO userVo = userMapper.info(userId);
        // 将密码去除
        userVo.setPassword(null);
        return Result.success("获取用户信息成功", userVo);
    }

    /**
     * 用户加入班级，只有学生才能加入班级
     * @param code
     * @return
     */
    @Override
    public Result<String> joinGrade(String code) {
        // 获取班级信息
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>().eq(Grade::getCode, code);
        Grade grade = gradeMapper.selectOne(wrapper);
        if (Objects.isNull(grade)) {
            return Result.failed("班级口令不存在");
        }
        User user = new User();
        user.setId(SecurityUtil.getUserId());
        user.setGradeId(grade.getId());
        int updated = userMapper.updateById(user);
        if (updated > 0) {
            stringRedisTemplate.delete("cache:grade:getPaging:" + SecurityUtil.getUserId().toString());
            return Result.success("加入班级：" + grade.getGradeName() + "成功");
        }
        return Result.failed("加入失败");
    }

    /**
     * 教师和管理员 用户管理 分页获取用户信息
     * @param pageNum
     * @param pageSize
     * @param gradeId
     * @param realName
     * @return
     */
    @Override
    public Result<IPage<UserVO>> pagingUser(Integer pageNum, Integer pageSize, Integer gradeId, String realName) {
        IPage<UserVO> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        if ("role_teacher".equals(SecurityUtil.getRole())) {
            List<Integer> gradeIdList = userGradeMapper.getGradeIdListByUserId(userId);
            page = userMapper.pagingUser(page, gradeId, realName, SecurityUtil.getUserId(), 1, gradeIdList);
        } else {
            page = userMapper.pagingUser(page, gradeId, realName, SecurityUtil.getUserId(), null, null);
        }

        return Result.success(null, page);
    }

    /**
     * 用户上传头像
     * @param file
     * @return
     */
    @Transactional
    @Override
    public Result<String> uploadAvatar(MultipartFile file) {
        Result<String> result = iQuestionService.uploadImage(file);
        if (result.getCode() == 0) {
            return Result.failed("图片上传失败");
        }
        String url = result.getData();
        User user = new User();
        user.setId(SecurityUtil.getUserId());
        user.setAvatar(url);
        if (userMapper.updateById(user) > 0) {
            return Result.success("上传成功", url);
        }
        return Result.failed("图片上传失败");
    }


}
