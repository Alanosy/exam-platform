package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.GradeConverter;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.vo.GradeVO;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.util.ClassTokenGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 服务实现类
 *
 * @author Alan
 * @since 2024-03-21
 */
@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements IGradeService {
    @Resource
    private GradeMapper gradeMapper;

    @Resource
    private GradeConverter gradeConverter;
    @Resource
    private UserMapper userMapper;


    @Override
    public Result<String> addGrade(Integer userId, GradeForm gradeForm) {
        gradeForm.setCode(ClassTokenGenerator.generateClassToken(18));
        Grade grade = gradeConverter.formToEntity(gradeForm);
        grade.setUserId(userId);
        int rowsAffected = gradeMapper.insert(grade);
        if (rowsAffected == 0) {
            return Result.failed("添加失败");
        }
        return Result.success("添加成功");
    }

    @Override
    public Result<String> updateGrade(String id, GradeForm gradeForm) {
        LambdaUpdateWrapper<Grade> gradeUpdateWrapper = new LambdaUpdateWrapper<>();
        gradeUpdateWrapper
                .set(Grade::getGradeName, gradeForm.getGradeName())
                .eq(Grade::getId, id);
        int rowsAffected = gradeMapper.update(gradeUpdateWrapper);
        if (rowsAffected == 0) {
            return Result.failed("修改失败");
        }
        return Result.success("修改成功");
    }

    @Override
    public Result<String> deleteGrade(Integer id) {
        int rowsAffected = gradeMapper.deleteById(id);
        if (rowsAffected == 0) {
            return Result.failed("删除失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<GradeVO>> getPaging(Integer userId, Integer pageNum, Integer pageSize, String gradeName) {
        Page<Grade> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Grade> gradeQueryWrapper = new LambdaQueryWrapper<>();
        gradeQueryWrapper.like(StringUtils.isNotBlank(gradeName), Grade::getGradeName, gradeName)
                .eq(Grade::getUserId, userId);
        Page<Grade> gradePage = gradeMapper.selectPage(page, gradeQueryWrapper);
        return Result.success("查询成功", gradeConverter.pageEntityToVo(gradePage));
    }

    @Override
    public Result<String> removeUserGrade(String ids) {
        List<Integer> userIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        int rowsAffected = userMapper.removeUserGrade(userIds);
        if (rowsAffected == 0) {
            return Result.failed("移除失败");
        }
        return Result.success("移除成功");
    }

    @Override
    public Result<List<GradeVO>> getGradeAll(Integer userId) {
        LambdaQueryWrapper<Grade> gradeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        gradeLambdaQueryWrapper.eq(Grade::getUserId, userId);
        List<Grade> grades = gradeMapper.selectList(gradeLambdaQueryWrapper);
        return Result.success("查询成功", gradeConverter.listEntityToVo(grades));
    }

    @Override
    public Result<String> userAddGrade(Integer userId, String code) {
        LambdaQueryWrapper<Grade> gradeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        gradeLambdaQueryWrapper.eq(Grade::getCode, code);
        Grade grade = gradeMapper.selectOne(gradeLambdaQueryWrapper);
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getUserId, userId)
                .set(User::getGradeId, grade.getId());
        int rowsAffected = userMapper.update(lambdaUpdateWrapper);
        if (rowsAffected == 0) {
            return Result.failed("加入失败");
        }
        return Result.success("加入成功");
    }
}
