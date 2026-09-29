package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.answer.CorrectAnswerFrom;
import cn.org.alan.exam.model.vo.answer.AnswerExamVO;
import cn.org.alan.exam.model.vo.answer.AnswerPaperSummaryVO;
import cn.org.alan.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.alan.exam.model.vo.answer.UserAnswerDetailVO;
import cn.org.alan.exam.service.IManualScoreService;
import cn.org.alan.exam.utils.ClassTokenGenerator;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 答卷管理服务实现类
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
    @Resource
    private CertificateUserMapper certificateUserMapper;

    /**
     * 试卷查询信息
     *
     * @param userId
     * @param examId
     * @return
     */
    @Override
    public Result<List<UserAnswerDetailVO>> getDetail(Integer userId, Integer examId) {
        List<UserAnswerDetailVO> list = examQuAnswerMapper.selectUserAnswer(userId, examId);
        return Result.success("查询成功", list);
    }

    @Override
    public Result<List<UserAnswerDetailVO>> getMyDetail(Integer examId) {
        Result<AnswerPaperSummaryVO> summaryResult = getMyPaperSummary(examId);
        if (summaryResult.getCode() == null || summaryResult.getCode() != 1) {
            return Result.failed(summaryResult.getMsg());
        }
        return Result.success("查询成功",
                examQuAnswerMapper.selectUserAnswer(SecurityUtil.getUserId(), examId));
    }

    @Override
    @Transactional
    public Result<String> correct(List<CorrectAnswerFrom> correctAnswerFroms) {
        if (correctAnswerFroms == null || correctAnswerFroms.isEmpty()) {
            return Result.failed("批改数据不能为空");
        }
        CorrectAnswerFrom first = correctAnswerFroms.get(0);
        UserExamsScore scoreRecord = userExamsScoreMapper.selectOne(new LambdaQueryWrapper<UserExamsScore>()
                .eq(UserExamsScore::getExamId, first.getExamId())
                .eq(UserExamsScore::getUserId, first.getUserId())
                .last("limit 1"));
        if (scoreRecord == null) {
            return Result.failed("未找到该考生的交卷记录");
        }
        if (scoreRecord.getWhetherMark() == null || scoreRecord.getWhetherMark() != 0) {
            return Result.failed("该答卷已批改或无需阅卷，请勿重复提交");
        }

        Exam examCfg = examMapper.selectOne(new LambdaQueryWrapper<Exam>()
                .select(Exam::getId, Exam::getFillNeedMark, Exam::getCertificateId, Exam::getPassedScore)
                .eq(Exam::getId, first.getExamId()));
        if (examCfg == null) {
            return Result.failed("考试不存在");
        }
        boolean fillRemarkable = examCfg.getFillNeedMark() != null && examCfg.getFillNeedMark() == 1;

        List<ManualScore> list = new ArrayList<>(correctAnswerFroms.size());
        AtomicInteger manualTotalScore = new AtomicInteger();
        for (CorrectAnswerFrom correctAnswerFrom : correctAnswerFroms) {
            ExamQuAnswer answer = examQuAnswerMapper.selectOne(new LambdaQueryWrapper<ExamQuAnswer>()
                    .select(ExamQuAnswer::getId, ExamQuAnswer::getQuestionType)
                    .eq(ExamQuAnswer::getExamId, correctAnswerFrom.getExamId())
                    .eq(ExamQuAnswer::getUserId, correctAnswerFrom.getUserId())
                    .eq(ExamQuAnswer::getQuestionId, correctAnswerFrom.getQuestionId())
                    .last("limit 1"));
            if (answer == null) {
                return Result.failed("未找到题目作答记录，题目ID：" + correctAnswerFrom.getQuestionId());
            }
            // 填空题仅在开启二次阅卷时允许人工改分，避免与自动分重复入账
            if (answer.getQuestionType() != null && answer.getQuestionType() == 5 && !fillRemarkable) {
                return Result.failed("该试卷填空题仅自动评分，不可人工改分");
            }

            ManualScore manualScore = new ManualScore();
            manualScore.setExamQuAnswerId(answer.getId());
            manualScore.setScore(correctAnswerFrom.getScore());
            list.add(manualScore);
            manualTotalScore.addAndGet(correctAnswerFrom.getScore());
        }
        manualScoreMapper.insertList(list);

        // 把用户考试记录修改为已批改，并把人工分添加进去
        CorrectAnswerFrom correctAnswerFrom = correctAnswerFroms.get(0);
        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<UserExamsScore>()
                .eq(UserExamsScore::getExamId, correctAnswerFrom.getExamId())
                .eq(UserExamsScore::getUserId, correctAnswerFrom.getUserId())
                .eq(UserExamsScore::getWhetherMark, 0)
                .set(UserExamsScore::getWhetherMark, 1)
                .setSql("user_score = user_score + " + manualTotalScore.get());
        int updated = userExamsScoreMapper.update(null, userExamsScoreLambdaUpdateWrapper);
        if (updated < 1) {
            return Result.failed("该答卷已批改或状态已变更，请刷新后重试");
        }

        // 根据该考试是否有证书来给用户颁发对应证书
        Exam exam = examCfg;
        if (exam.getCertificateId() != null && exam.getCertificateId() > 0) {
            // 有证书 获取用户得分
            LambdaQueryWrapper<UserExamsScore> examsScoreWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .select(UserExamsScore::getId, UserExamsScore::getUserScore)
                    .eq(UserExamsScore::getExamId, correctAnswerFrom.getExamId())
                    .eq(UserExamsScore::getUserId, correctAnswerFrom.getUserId());
            UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(examsScoreWrapper);
            // 不必对userExamsScore做非空验证，这里一定不为null
            if (userExamsScore.getUserScore() >= exam.getPassedScore()) {
                // 分数合格，判罚证书
                CertificateUser certificateUser = new CertificateUser();
                certificateUser.setUserId(correctAnswerFrom.getUserId());
                certificateUser.setExamId(correctAnswerFrom.getExamId());
                certificateUser.setCode(ClassTokenGenerator.generateClassToken(18));
                certificateUser.setCertificateId(exam.getCertificateId());
                certificateUserMapper.insert(certificateUser);
            }

        }
        return Result.success("批改成功");
    }

    @Override
    public Result<IPage<AnswerExamVO>> examPage(Integer pageNum, Integer pageSize, String examName) {

        Page<AnswerExamVO> page = new Page<>(pageNum, pageSize);
        // 获取自己创建的考试
        List<AnswerExamVO> list = examMapper.selectMarkedList(page, SecurityUtil.getUserId(), SecurityUtil.getRole(), examName).getRecords();

        // 获取相关信息
        list.forEach(answerExamVO -> {
            // 需要参加考试人数
            answerExamVO.setClassSize(examGradeMapper.selectClassSize(answerExamVO.getExamId()));
            // 实际交卷人数（不含仅开考未交卷）
            LambdaQueryWrapper<UserExamsScore> numberWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId())
                    .eq(UserExamsScore::getState, 1);
            answerExamVO.setNumberOfApplicants(userExamsScoreMapper.selectCount(numberWrapper).intValue());
            // 已阅人数
            LambdaQueryWrapper<UserExamsScore> correctedWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getWhetherMark, 1)
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId());
            answerExamVO.setCorrectedPaper(userExamsScoreMapper.selectCount(correctedWrapper).intValue());
            // 待阅卷人数（详情页列表条件一致）
            LambdaQueryWrapper<UserExamsScore> pendingWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getWhetherMark, 0)
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId());
            answerExamVO.setPendingMark(userExamsScoreMapper.selectCount(pendingWrapper).intValue());
        });
        return Result.success(null, page);

    }

    @Override
    public Result<IPage<UncorrectedUserVO>> stuExamPage(Integer pageNum, Integer pageSize, Integer examId, String realName) {
        IPage<UncorrectedUserVO> page = new Page<>(pageNum, pageSize);
        page = userExamsScoreMapper.uncorrectedUser(page, examId, realName);
        return Result.success(null, page);
    }

    @Override
    public Result<AnswerPaperSummaryVO> paperSummary(Integer examId, Integer userId) {
        AnswerPaperSummaryVO summary = userExamsScoreMapper.selectPaperSummary(examId, userId);
        if (summary == null) {
            return Result.failed("未找到该考生的交卷记录");
        }
        return Result.success("查询成功", summary);
    }

    @Override
    public Result<AnswerPaperSummaryVO> getMyPaperSummary(Integer examId) {
        Integer userId = SecurityUtil.getUserId();
        if (userId == null) {
            return Result.failed("登录已过期，请重新登录");
        }

        UserExamsScore scoreRecord = userExamsScoreMapper.selectOne(new LambdaQueryWrapper<UserExamsScore>()
                .select(UserExamsScore::getId, UserExamsScore::getState)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, userId)
                .last("limit 1"));
        if (scoreRecord == null || scoreRecord.getState() == null || scoreRecord.getState() != 1) {
            return Result.failed("未找到你的交卷记录");
        }

        AnswerPaperSummaryVO summary = userExamsScoreMapper.selectPaperSummary(examId, userId);
        if (summary == null) {
            return Result.failed("未找到你的交卷记录");
        }
        return Result.success("查询成功", summary);
    }
}
