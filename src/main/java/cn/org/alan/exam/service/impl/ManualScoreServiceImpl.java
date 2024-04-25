package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.answer.AnswerUpdate;
import cn.org.alan.exam.model.vo.answer.AnswerExamPageVO;
import cn.org.alan.exam.model.vo.answer.AnswerPageVO;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.service.IManualScoreService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class ManualScoreServiceImpl extends ServiceImpl<ManualScoreMapper, ManualScore> implements IManualScoreService {

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamGradeMapper examGradeMapper;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public Result getDetail(Integer userId, Integer examId) {
        return null;
    }

    @Override
    public Result<IPage<AnswerPageVO>> getPaging(Integer pageNum, Integer pageSize) {
        return null;
    }

    @Override
    public Result Correct(AnswerUpdate answerUpdate) {
        return null;
    }

    @Override
    public Result<IPage<AnswerExamPageVO>> examPage(Integer pageNum,Integer pageSize) {

        List<AnswerExamPageVO> answerExamPageVOS = new ArrayList<>();
        LambdaQueryWrapper<Exam> examLambdaQueryWrapper = new LambdaQueryWrapper<>();
        examLambdaQueryWrapper.eq(Exam::getUserId, SecurityUtil.getUserId())
                .select(Exam::getId)
                .select(Exam::getTitle);
        List<Exam> exams = examMapper.selectList(examLambdaQueryWrapper);
        // 查找自己创建的考试
        // SELECT id,title FROM t_exam WHERE user_id = 1
        for (Exam temp : exams) {
            AnswerExamPageVO answerExamPageVO = new AnswerExamPageVO();
            answerExamPageVO.setExam(temp);
            Integer classSize = gradeMapper.getGradeCount(temp.getId());
            Integer numberOfApplicants = userExamsScoreMapper.getNumberOfApplicants(temp.getId());
            Integer correctedPaper = userExamsScoreMapper.getCorrectedPaper(temp.getId());
            answerExamPageVO.setClassSize(classSize);
            answerExamPageVO.setCorrectedPaper(correctedPaper);
            answerExamPageVO.setNumberOfApplicants(numberOfApplicants);
            answerExamPageVOS.add(answerExamPageVO);
        }
        // 查询考试班级人数（总人数）
        // SELECT COUNT(*) FROM t_grade WHERE id in (SELECT grade_id FROM t_exam_grade WHERE exam_id= 1)

        // 查询已考人数（待阅卷）
        // SELECT COUNT(*) FROM t_user_exams_score WHERE state=1 and exam_id= 1

        // 查询已改试卷数（已阅卷）
        // SELECT COUNT(*) FROM t_user_exams_score WHERE whether_mark=1

        return null;
    }

    @Override
    public Result stuExamPage(Integer pageNum, Integer pageSize, Integer examId) {
        // 查询该场考试的用户
        // SELECT * FROM t_user WHERE id in (SELECT grade_id FROM t_exam_grade WHERE exam_id= 1)
        userMapper.stuExamPage(examId);
        return null;
    }
}
