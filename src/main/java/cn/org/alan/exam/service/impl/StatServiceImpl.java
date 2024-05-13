package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.ExamGradeMapper;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.ExamGrade;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.vo.stat.AllStatsVO;
import cn.org.alan.exam.model.vo.stat.GradeExamVO;
import cn.org.alan.exam.model.vo.stat.GradeStudentVO;
import cn.org.alan.exam.service.IStatService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class StatServiceImpl extends ServiceImpl<ExamGradeMapper, ExamGrade> implements IStatService {

    @Resource
    private StatMapper statMapper;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private QuestionMapper questionMapper;


    @Autowired
    public StatServiceImpl(StatMapper statMapper,GradeMapper gradeMapper, ExamMapper examMapper, QuestionMapper questionMapper) {
        this.statMapper = statMapper;
        this.gradeMapper = gradeMapper;
        this.examMapper = examMapper;
        this.questionMapper = questionMapper;
    }

    /**
     * 各班人数统计信息
     *
     * @return
     */
    @Override
    public Result<List<GradeStudentVO>> getStudentGradeCount() {
        //获取班级
        List<GradeStudentVO> gradeStudentVOs;
        if (SecurityUtil.getRole().equals("role_teacher")) {
            gradeStudentVOs = statMapper.StudentGradeCount(2, SecurityUtil.getUserId());
        } else {
            gradeStudentVOs = statMapper.StudentGradeCount(0, SecurityUtil.getUserId());
        }

        return Result.success("查询成功", gradeStudentVOs);
    }

    /**
     * 各班试卷统计信息
     *
     * @return
     */
    @Override
    public Result<List<GradeExamVO>> getExamGradeCount() {
        //获取班级
        List<GradeExamVO> gradeExamVOs;
        if (SecurityUtil.getRole().equals("role_teacher")) {
            gradeExamVOs = statMapper.ExamGradeCount(2, SecurityUtil.getUserId());
        } else {
            gradeExamVOs = statMapper.ExamGradeCount(0, SecurityUtil.getUserId());
        }

        return Result.success("查询成功", gradeExamVOs);
    }

    /**
     * 所有班级、试卷、试题数量统计信息
     *
     * @return
     */
    @Override
    public Result<AllStatsVO> getAllCount() {
        AllStatsVO allStatsVO = new AllStatsVO();
        allStatsVO.setClassCount(gradeMapper.selectCount(null).intValue());
        allStatsVO.setExamCount(examMapper.selectCount(null).intValue());
        allStatsVO.setQuestionCount(questionMapper.selectCount(null).intValue());
        return Result.success("查询成功", allStatsVO);
    }
}

