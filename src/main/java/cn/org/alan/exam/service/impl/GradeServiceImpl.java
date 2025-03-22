package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.GradeConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.UserGrade;
import cn.org.alan.exam.model.form.grade.GradeForm;
import cn.org.alan.exam.model.vo.grade.GradeVO;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.utils.ClassTokenGenerator;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 班级服务实现类
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
    @Resource
    private UserGradeMapper userGradeMapper;

    @Override
    @Transactional
    public Result<String> addGrade(GradeForm gradeForm) {
        // 生成班级口令
        gradeForm.setCode(ClassTokenGenerator.generateClassToken(18));
        // 实体转换
        Grade grade = gradeConverter.formToEntity(gradeForm);
        // 开始添加数据
        int rows = gradeMapper.insert(grade);
        if (rows == 0) {
            throw new ServiceRuntimeException("新建班级失败");
        }
        return Result.success("新建班级成功");
    }

    @Override
    @Transactional
    public Result<String> updateGrade(Integer id, GradeForm gradeForm) {
        // 建立更新条件
        LambdaUpdateWrapper<Grade> gradeUpdateWrapper = new LambdaUpdateWrapper<>();
        gradeUpdateWrapper
                .set(Grade::getGradeName, gradeForm.getGradeName())
                .eq(Grade::getId, id);
        // 更新班级
        int rows = gradeMapper.update(gradeUpdateWrapper);
        if (rows == 0) {
            throw new ServiceRuntimeException("修改班级失败");
        }
        return Result.success("修改班级成功");
    }

    @Override
    @Transactional
    public Result<String> deleteGrade(Integer gradeId) {
        // 逻辑删除班级
        int rows = gradeMapper.deleteById(gradeId);
        if (rows == 0) {
            throw new ServiceRuntimeException("删除班级失败");
        }
        // 逻辑删除教师与班级的关联
        userGradeMapper.deleteById(gradeId);
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<GradeVO>> getPaging(Integer pageNum, Integer pageSize, String gradeName) {
        Page<GradeVO> page = new Page<>(pageNum, pageSize);
        // 获取当前角色代码和用户ID
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        // 如果是教师获取教师加入班级的ID
        List<Integer> gradeIdList = null;
        if (roleCode == 2) {
            gradeIdList = userGradeMapper.getGradeIdListByUserId(userId);
        }
        // 开始查询班级
        page = gradeMapper.selectGradePage(page, userId, gradeName, roleCode, gradeIdList);
        return Result.success("查询成功", page);
    }

    @Override
    public Result<String> removeUserGrade(String ids) {
        // 字符串转换为列表
        List<Integer> userIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .collect(java.util.stream.Collectors.toList());
        // 移出班级
        int rows = userMapper.removeUserGrade(userIds);
        if (rows == 0) {
            throw new ServiceRuntimeException("批量用户移除班级失败");
        }
        return Result.success("批量用户移除班级成功");
    }

    @Override
    public Result<List<GradeVO>> getAllGrade() {
        // 获取角色代码和用户ID
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        List<Integer> gradeIdList = null;
        if (roleCode == 2) {
            gradeIdList = userGradeMapper.getGradeIdListByUserId(userId);
            if (gradeIdList.isEmpty()) {
                throw new ServiceRuntimeException("教师还没加入班级暂无数据");
            }
        }
        // 开始查询当前用户管理的所有班级
        List<GradeVO> grades = gradeMapper.getAllGrade(userId, roleCode, gradeIdList);
        return Result.success("查询成功", grades);
    }

    @Override
    public Result teacherJoinClass(String code) {
        // 获取班级信息 用户ID
        Grade grade = gradeMapper.getGradeByCode(code);
        Integer userId = SecurityUtil.getUserId();
        // 设置教师和班级的联系
        UserGrade userGrade = new UserGrade();
        userGrade.setGId(grade.getId());
        userGrade.setUId(userId);
        // 开始添加教师和班级的联系
        int insert = userGradeMapper.insert(userGrade);
        if (insert > 0) {
            return Result.success("教师加入班级成功");
        }
        throw new ServiceRuntimeException("教师加入班级失败");
    }

    @Override
    public Result teacherExitClass(String gradeId) {
        // 获取用户ID
        Integer userId = SecurityUtil.getUserId();
        // 开始调用sql教师退出班级
        Integer row = userGradeMapper.teacherExitClass(userId, gradeId);
        if (row > 0) {
            return Result.success("教师退出班级成功");
        }
        throw new ServiceRuntimeException("教师退出班级失败");
    }

    @Override
    public Result userExitGrade() {
        // 获取班级和用户ID
        Integer gradeId = SecurityUtil.getGradeId();
        Integer userId = SecurityUtil.getUserId();
        // 开始调用sql用户退出班级
        Integer row = userMapper.userExitGrade(gradeId, userId);
        if (row > 0) {
            return Result.success("学生退出班级成功");
        }
        throw new ServiceRuntimeException("学生退出班级失败");
    }

}

