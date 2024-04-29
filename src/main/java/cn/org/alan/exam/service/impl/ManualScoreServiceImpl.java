package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.answer.CorrectAnswerFrom;
import cn.org.alan.exam.model.vo.answer.AnswerExamVO;
import cn.org.alan.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.alan.exam.model.vo.answer.UserAnswerDetailVO;
import cn.org.alan.exam.service.IManualScoreService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private ManualScoreMapper manualScoreMapper;



    @Override
    public Result<List<UserAnswerDetailVO>> getDetail(Integer userId, Integer examId) {
        List<UserAnswerDetailVO> list = examQuAnswerMapper.selectUserAnswer(userId, examId);
        return Result.success(null, list);
    }


    @Override
    @Transactional
    public Result<String> correct(List<CorrectAnswerFrom> correctAnswerFroms) {
        List<ManualScore> list = new ArrayList<>(correctAnswerFroms.size());
        correctAnswerFroms.forEach(correctAnswerFrom -> {

            //获取用户作答信息id
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<ExamQuAnswer>()
                    .select(ExamQuAnswer::getId)
                    .eq(ExamQuAnswer::getExamId, correctAnswerFrom.getExamId())
                    .eq(ExamQuAnswer::getUserId, correctAnswerFrom.getUserId())
                    .eq(ExamQuAnswer::getQuestionId, correctAnswerFrom.getQuestionId());

            ManualScore manualScore = new ManualScore();
            manualScore.setId(examQuAnswerMapper.selectOne(wrapper).getId());
            manualScore.setScore(correctAnswerFrom.getScore());
            list.add(manualScore);
        });
        manualScoreMapper.insertList(list);
        return Result.success("批改成功");
    }

    @Override
    public Result<IPage<AnswerExamVO>> examPage(Integer pageNum, Integer pageSize) {

        Page<AnswerExamVO> page = new Page<>(pageNum, pageSize);
        //获取自己创建的考试
        List<AnswerExamVO> list = examMapper.selectMarkedList(page, SecurityUtil.getUserId()).getRecords();
        //获取相关信息
        list.forEach(answerExamVO -> {
            //需要参加考试人数
            answerExamVO.setClassSize(examGradeMapper.selectClassSize(answerExamVO.getExamId()));
            //实际参加考试人数
            LambdaQueryWrapper<UserExamsScore> numberWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId());
            answerExamVO.setNumberOfApplicants(userExamsScoreMapper.selectCount(numberWrapper).intValue());
            //已阅人数
            LambdaQueryWrapper<UserExamsScore> correctedWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getWhetherMark, 1);
            answerExamVO.setCorrectedPaper(userExamsScoreMapper.selectCount(correctedWrapper).intValue());
        });

        return Result.success(null, page);

    }

    @Override

    public Result<IPage<UncorrectedUserVO>> stuExamPage(Integer pageNum, Integer pageSize, Integer examId) {
        IPage<UncorrectedUserVO> page = new Page<>(pageNum, pageSize);
        page = userExamsScoreMapper.uncorrectedUser(page, examId);
        return Result.success(null, page);
    }
}
