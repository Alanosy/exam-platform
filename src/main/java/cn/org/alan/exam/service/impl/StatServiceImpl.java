package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.mapper.ExamGradeMapper;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.vo.stat.AllStatsVO;
import cn.org.alan.exam.model.vo.stat.DailyVO;
import cn.org.alan.exam.model.vo.stat.GradeExamVO;
import cn.org.alan.exam.model.vo.stat.GradeStudentVO;
import cn.org.alan.exam.service.IStatService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统计管理服务实现类
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
    @Resource
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;
    @Resource
    private UserGradeMapper userGradeMapper;

    @Override
    public Result<List<GradeStudentVO>> getStudentGradeCount() {
        //获取班级
        List<GradeStudentVO> gradeStudentVOs;
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode==2) {
            List<Integer> gradeIdList = userGradeMapper.getGradeIdListByUserId(userId);
            if(gradeIdList.isEmpty()){
                throw new ServiceRuntimeException("教师还没加入班级暂无数据");
            }
            gradeStudentVOs = statMapper.StudentGradeCount(2, userId,gradeIdList);
        } else {
            gradeStudentVOs = statMapper.StudentGradeCount(3, userId,null);
        }
        return Result.success("查询成功", gradeStudentVOs);
    }

    @Override
    public Result<List<GradeExamVO>> getExamGradeCount() {
        //获取班级
        List<GradeExamVO> gradeExamVOs;
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode==2) {
            List<Integer> gradeIdList = userGradeMapper.getGradeIdListByUserId(userId);
            if(gradeIdList.isEmpty()){
                throw new ServiceRuntimeException("教师还没加入班级暂无数据");
            }
            gradeExamVOs = statMapper.ExamGradeCount(2, userId,gradeIdList);
        } else {
            gradeExamVOs = statMapper.ExamGradeCount(3, userId, null);
        }
        return Result.success("查询成功", gradeExamVOs);
    }

    @Override
    public Result<AllStatsVO> getAllCount() {
        AllStatsVO allStatsVO = new AllStatsVO();
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if(roleCode==3){
            allStatsVO.setClassCount(gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                            .eq(Grade::getIsDeleted,0)).intValue());
            allStatsVO.setExamCount(examMapper.selectCount(new LambdaQueryWrapper<Exam>()
                            .eq(Exam::getIsDeleted,0)).intValue());
            allStatsVO.setQuestionCount(questionMapper.selectCount( new LambdaQueryWrapper<Question>()
                    .eq(Question::getIsDeleted,0)).intValue());
        }else if(roleCode==2){
            allStatsVO.setClassCount(userGradeMapper.selectCount(new LambdaQueryWrapper<UserGrade>()
                            .eq(UserGrade::getIsDeleted,0)
                            .eq(UserGrade::getUId,userId)).intValue());
            allStatsVO.setExamCount(examMapper.selectCount(
                    new LambdaQueryWrapper<Exam>()
                            .eq(Exam::getIsDeleted,0)
                            .eq(Exam::getUserId,userId)).intValue());
            allStatsVO.setQuestionCount(questionMapper.selectCount(
                    new LambdaQueryWrapper<Question>()
                            .eq(Question::getIsDeleted,0)
                            .eq(Question::getUserId,userId)).intValue());
        }
        return Result.success("查询成功", allStatsVO);
    }

    @Override
    public Result<List<DailyVO>> getDaily() {
        List<DailyVO> daily = userDailyLoginDurationMapper.getDaily(SecurityUtil.getUserId());
        return Result.success("请求成功",daily);
    }
}

