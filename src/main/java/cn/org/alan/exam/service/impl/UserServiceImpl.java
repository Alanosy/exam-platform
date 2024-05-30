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
import cn.org.alan.exam.util.AliOSSUtil;
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
    private CertificateUserMapper certificateUserMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private GradeExerciseMapper gradeExerciseMapper;
    @Resource
    private ManualScoreMapper manualScoreMapper;
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private RepoMapper repoMapper;
    @Resource
    private UserBookMapper userBookMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private NoticeGradeMapper noticeGradeMapper;
    @Resource
    private IQuestionService iQuestionService;


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
        return Result.success("用户创建成功");

    }

    @Override
    public Result<String> updatePassword(UserForm userForm) {
        if (!userForm.getNewPassword().equals(userForm.getCheckedPassword())) {
            return Result.failed("两次密码不一致");
        }
        Integer userId = SecurityUtil.getUserId();
        if (!new BCryptPasswordEncoder()
                .matches(userForm.getOriginPassword(),userMapper.selectById(userId).getPassword())) {
            return Result.failed("旧密码错误");
        }
        //密码加密
        userForm.setPassword(new BCryptPasswordEncoder().encode(userForm.getNewPassword()));
        userForm.setId(userId);
        int updated = userMapper.updateById(userConverter.fromToEntity(userForm));
        //密码修改成功清除redis的token，让用户重新登录
        if (updated > 0) {
            stringRedisTemplate.delete(request.getSession().getId() + "token");
            return Result.success("修改成功，请重新登录");
        }
        return Result.failed("旧密码错误");

    }

    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> userIds = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        //删除用户证书
        certificateUserMapper.deleteByUserIds(userIds);
        //删除用户创建的考试
        examMapper.deleteByUserIds(userIds);
        //删除用户考试作答记录
        examQuAnswerMapper.deleteByUserIds(userIds);
        //删除用户练习记录
        exerciseRecordMapper.deleteByUserIds(userIds);
        //清除用户表中的班级id
        List<Integer> gradeIds = gradeMapper.selectIdsByUserIds(userIds);
        if (!gradeIds.isEmpty()) {
            userMapper.removeGradeIdByGradeIds(gradeIds);
        }

        //删除用户创建的班级
        gradeMapper.deleteByUserId(userIds);
        //删除用户创建的可供学生练习的题库关联表
        gradeExerciseMapper.deleteByUserIds(userIds);
        //删除用户批改的主观题分数
        manualScoreMapper.deleteByUserIds(userIds);
        //删除公告与班级关联表
        //1.获取公告id
        List<Integer> noticeIds = noticeMapper.selectIdsByUserIds(userIds);
        //2.删除公告与班级关联
        if (!noticeIds.isEmpty()) {
            noticeGradeMapper.deleteByNoticeIds(noticeIds);
        }
        //删除用户创建的公告
        noticeMapper.deleteByUserIds(userIds);

        //删除试题选项
        //1.获取用户创建的试题id
        List<Integer> quIds = questionMapper.selectIdsByUserIds(userIds);
        //2.根据试题id列表删除选项
        if (!quIds.isEmpty()) {
            optionMapper.deleteBatchByQuIds(quIds);
        }
        //删除用户创建的试题
        questionMapper.deleteByUserIds(userIds);
        //删除用户创建的题库
        repoMapper.deleteByUserIds(userIds);
        //删除用户的错题本
        userBookMapper.deleteByUserIds(userIds);
        //删除用户
        userMapper.deleteBatchIds(userIds);
        //
        return Result.success("删除成功");
    }

    @SneakyThrows(Exception.class)
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
        userVo.setPassword(null);
        return Result.success(null, userVo);
    }

    @Override
    public Result<String> joinGrade(String code) {
        //获取班级信息
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
            return Result.success("加入班级：" + grade.getGradeName() + "成功");
        }
        return Result.failed("加入失败");
    }

    @Override
    public Result<IPage<UserVO>> pagingUser(Integer pageNum, Integer pageSize, Integer gradeId, String realName) {
        IPage<UserVO> page = new Page<>(pageNum, pageSize);
        if ("role_teacher".equals(SecurityUtil.getRole())) {
            page = userMapper.pagingUser(page, gradeId, realName, SecurityUtil.getUserId(),1);
        } else {
            page = userMapper.pagingUser(page, gradeId, realName, SecurityUtil.getUserId(),null);
        }

        return Result.success(null, page);
    }

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
