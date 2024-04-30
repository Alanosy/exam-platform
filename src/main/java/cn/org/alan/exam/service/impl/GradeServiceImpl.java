package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.GradeConverter;
import cn.org.alan.exam.mapper.ExamMapper;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.form.count.ClassCountResult;
import cn.org.alan.exam.model.vo.GradeVO;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.util.ClassTokenGenerator;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.annotation.Resource;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 班级服务实现类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements IGradeService {
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private GradeConverter gradeConverter;
    @Resource
    private UserMapper userMapper;

    @Resource
    private IGradeService gradeService;  //用此服务来获取班级信息



    @Override
    public Result<String> addGrade(GradeForm gradeForm) {
        // 生成班级口令
        gradeForm.setCode(ClassTokenGenerator.generateClassToken(18));
        // 实体转换
        Grade grade = gradeConverter.formToEntity(gradeForm);
        int rows = gradeMapper.insert(grade);
        if (rows== 0) {
            return Result.failed("添加失败");
        }
        return Result.success("添加成功");
    }

    @Override
    public Result<String> updateGrade(Integer id, GradeForm gradeForm) {
        // 更新班级
        LambdaUpdateWrapper<Grade> gradeUpdateWrapper = new LambdaUpdateWrapper<>();
        gradeUpdateWrapper
                .set(Grade::getGradeName, gradeForm.getGradeName())
                .eq(Grade::getId, id);
        int rows = gradeMapper.update(gradeUpdateWrapper);
        if (rows == 0) {
            return Result.failed("修改失败");
        }
        return Result.success("修改成功");
    }

    @Override
    public Result<String> deleteGrade(Integer id) {
        // 删除班级
        int rows = gradeMapper.deleteById(id);
        if (rows == 0) {
            return Result.failed("删除失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<GradeVO>> getPaging(Integer pageNum, Integer pageSize, String gradeName) {
        Page<Grade> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Grade> gradeQueryWrapper = new LambdaQueryWrapper<>();
        gradeQueryWrapper.like(StringUtils.isNotBlank(gradeName), Grade::getGradeName, gradeName)
                .eq(Grade::getUserId, SecurityUtil.getUserId());
        Page<Grade> gradePage = gradeMapper.selectPage(page, gradeQueryWrapper);
        return Result.success("查询成功", gradeConverter.pageEntityToVo(gradePage));
    }

    @Override
    public Result<String> removeUserGrade(String ids) {
        List<Integer> userIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        int rows = userMapper.removeUserGrade(userIds);
        if (rows == 0) {
            return Result.failed("移除失败");
        }
        return Result.success("移除成功");
    }

    @Override
    public Result<List<GradeVO>> getAllGrade() {
        LambdaQueryWrapper<Grade> gradeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        gradeLambdaQueryWrapper.eq(Grade::getUserId,SecurityUtil.getUserId());
        List<Grade> grades = gradeMapper.selectList(gradeLambdaQueryWrapper);
        return Result.success("查询成功",gradeConverter.listEntityToVo(grades));
    }

    //统计各班级人数，
    @Override
    public List<ClassCountResult> countStudentsByRoleId(int roleId) {
        return userMapper.countAndGroupByGradeAndRoleId(roleId);
    }


    @Override
    public Result getAllCounts() {
        long gradeCount = gradeMapper.selectCount(null);
        long examCount = examMapper.selectCount(null);
        long questionCount = questionMapper.selectCount(null);

        ClassCountResult result = new ClassCountResult();
        result.setGradeCount((int) gradeCount);
        result.setExamCount((int) examCount);
        result.setQuestionCount((int) questionCount);

        return Result.success(null,result);
    }

}

