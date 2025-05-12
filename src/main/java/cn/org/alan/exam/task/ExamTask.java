package cn.org.alan.exam.task;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.enums.ExamState;
import cn.org.alan.exam.model.vo.exam.ExamQuDetailVO;
import cn.org.alan.exam.service.IAutoScoringService;
import cn.org.alan.exam.utils.ClassTokenGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 说明：
 *
 */
@Component
@Slf4j
public class ExamTask {
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private UserBookMapper userBookMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private CertificateUserMapper certificateUserMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private IAutoScoringService autoScoringService;

    /**
     * 维护任务定时检测是否有正在考试但是当前时间大于结束时间到，自动交卷
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 5 * 1000)
    public void test() {
        // 查询出正在考试的用户信息
        LambdaQueryWrapper<UserExamsScore> query = new LambdaQueryWrapper<>();
        query.eq(UserExamsScore::getState, ExamState.ONGOING.getCode());
        List<UserExamsScore> userExamsScores = userExamsScoreMapper.selectList(query);
        //  获取当前时间
        LocalDateTime now = LocalDateTime.now();
//         if(userExamsScores.size()>0){
//             for (UserExamsScore userExamsScore : userExamsScores) {
//                 try {
//                     // 查找到具体考试到信息
//                     Integer examId = userExamsScore.getExamId();
//                     Exam exam = examMapper.selectById(examId);
//                     // 3. 获取考试信息
//                     if(exam == null) {
//                         log.error("考试不存在，examId: {}", exam.getId());
//                         continue;
//                     }
//
//                     LocalDateTime endTime = exam.getEndTime();
//                     // 4. 检查是否超时
//                     if(now.isAfter(exam.getEndTime())) {
//                         // 5. 调用交卷函数
//                         handExam(userExamsScore);
//
//                         log.info("自动交卷成功，用户ID: {}, 考试ID: {}",
//                                 userExamsScore.getUserId(), userExamsScore.getExamId());
//                     }
//                 } catch (Exception e) {
//                     log.error("自动交卷处理异常，用户考试记录ID: {}", userExamsScore.getId(), e);
//                 }
//             }
//         }
        for (UserExamsScore userExamsScore : userExamsScores) {
            try {
                // 查找到具体考试的信息
                Integer examId = userExamsScore.getExamId();
                Exam exam = examMapper.selectById(examId);

                if (exam == null) {
                    log.error("考试不存在，examId: {}", examId);
                    continue;
                }

                // 计算考试结束时间
                LocalDateTime userStartTime = userExamsScore.getCreateTime();//获取用户实际的开始时间 (UserExamsScore 记录的创建时间
                if (userStartTime == null) {
                    // 如果因为某些原因没取到 userExamsScore 或者其 createTime 为 null，需要处理
                    // 可以尝试重新从数据库获取一次该记录确保拿到最新数据
                    UserExamsScore currentRecord = userExamsScoreMapper.selectById(userExamsScore.getId());
                    if (currentRecord == null || currentRecord.getCreateTime() == null) {
                        log.error("无法获取用户考试记录的实际开始时间，用户考试记录ID: {}", userExamsScore.getId());
                        continue; // 跳过此记录
                    }
                    userStartTime = currentRecord.getCreateTime();
                }

                LocalDateTime userEndTime = userStartTime.plusMinutes(exam.getExamDuration());

                // 检查是否超时
                if (now.isAfter(userEndTime)) {
                    // 调用交卷函数
                    handExam(userExamsScore);
                    log.info("自动交卷成功，用户ID: {}, 考试ID: {}",
                            userExamsScore.getUserId(), userExamsScore.getExamId());
                }
            } catch (Exception e) {
                log.error("自动交卷处理异常，用户考试记录ID: {}", userExamsScore.getId(), e);
            }
        }
    }

    /**
     * 交卷操作
     * @return
     */
    @Transactional
    public Result<ExamQuDetailVO> handExam(UserExamsScore ues) {
        // 获取当前时间

        LocalDateTime nowTime = LocalDateTime.now();
        // 查询考试表记录
        Exam examOne = examMapper.selectById(ues.getExamId());
        // 设置考试状态
        UserExamsScore userExamsScore = new UserExamsScore();
        userExamsScore.setUserScore(0);
        userExamsScore.setState(1);

        // 查询用户答题记录
        LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQuery = new LambdaQueryWrapper<>();
        examQuAnswerLambdaQuery.eq(ExamQuAnswer::getUserId, ues.getUserId())
                .eq(ExamQuAnswer::getExamId, ues.getExamId());
        List<ExamQuAnswer> examQuAnswer = examQuAnswerMapper.selectList(examQuAnswerLambdaQuery);
        // 客观分
        List<UserBook> userBookArrayList = new ArrayList<>();
        for (ExamQuAnswer temp : examQuAnswer) {
            if (temp.getIsRight() == 1) {
                if (temp.getQuestionType() == 1) {
                    userExamsScore.setUserScore(userExamsScore.getUserScore() + examOne.getRadioScore());
                } else if (temp.getQuestionType() == 2) {
                    userExamsScore.setUserScore(userExamsScore.getUserScore() + examOne.getMultiScore());
                } else if (temp.getQuestionType() == 3) {
                    userExamsScore.setUserScore(userExamsScore.getUserScore() + examOne.getJudgeScore());
                }
            } else if (temp.getIsRight() == 0) {
                UserBook userBook = new UserBook();
                userBook.setExamId(ues.getExamId());
                userBook.setUserId(ues.getUserId());
                userBook.setQuId(temp.getQuestionId());
                userBook.setCreateTime(nowTime);
                userBookArrayList.add(userBook);
            }
        }
        if (!userBookArrayList.isEmpty()) {
            // 把打错的问题加入错题本
            userBookMapper.addUserBookList(userBookArrayList);
        }
        // 设置用户用时和提交试卷
        userExamsScore.setLimitTime(nowTime);
        // 开始时间
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, ues.getUserId())
                .eq(UserExamsScore::getExamId, ues.getExamId());
        UserExamsScore userExamsScore1 = userExamsScoreMapper.selectOne(userExamsScoreLambdaQueryWrapper);
        LocalDateTime createTime = userExamsScore1.getCreateTime();
        long secondsDifference = Duration.between(createTime, nowTime).getSeconds();
        int differenceAsInteger = (int) secondsDifference;
        // 检查是否在Integer范围内
        // if (secondsDifference <= Integer.MAX_VALUE && secondsDifference >= Integer.MIN_VALUE)
        userExamsScore.setUserTime(differenceAsInteger);
        // 添加总分和状态
        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdate = new LambdaUpdateWrapper<>();
        userExamsScoreLambdaUpdate.eq(UserExamsScore::getUserId, ues.getUserId())
                .eq(UserExamsScore::getExamId, ues.getExamId());
        userExamsScoreMapper.update(userExamsScore, userExamsScoreLambdaUpdate);
        // 判断是否有简答题
        if (examOne.getSaqCount() != 0) {
            LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userExamsScoreLambdaUpdateWrapper.set(UserExamsScore::getWhetherMark, 0)
                    .eq(UserExamsScore::getExamId, ues.getExamId())
                    .eq(UserExamsScore::getUserId, ues.getUserId());
            userExamsScoreMapper.update(userExamsScoreLambdaUpdateWrapper);
            autoScoringService.autoScoringExam(ues.getExamId(), ues.getUserId());
            return Result.success("提交成功，待老师阅卷");
        }
        if (userExamsScore.getUserScore() >= examOne.getPassedScore()) {
            CertificateUser certificateUser = new CertificateUser();
            certificateUser.setCertificateId(examOne.getCertificateId());
            certificateUser.setUserId(ues.getUserId());
            certificateUser.setExamId(ues.getExamId());
            certificateUser.setCode(ClassTokenGenerator.generateClassToken(18));
            certificateUserMapper.insert(certificateUser);
        }
        // 查询有简答题是否回答
        Exam byId = examMapper.selectById(ues.getExamId());
        if (byId.getSaqCount() > 0) {
            LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
            examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getUserId, ues.getUserId())
                    .eq(ExamQuAnswer::getExamId, ues.getExamId())
                    .eq(ExamQuAnswer::getQuestionType, 4);
            List<ExamQuAnswer> examQuAnswers = examQuAnswerMapper.selectList(examQuAnswerLambdaQueryWrapper);
            if (examQuAnswers.isEmpty()) {
                LambdaQueryWrapper<ExamQuestion> examQuestionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                examQuestionLambdaQueryWrapper.eq(ExamQuestion::getExamId, ues.getExamId())
                        .eq(ExamQuestion::getType, 4);
                List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionLambdaQueryWrapper);
                examQuestions.forEach(temp -> {
                    ExamQuAnswer examQuAnswer1 = new ExamQuAnswer();
                    examQuAnswer1.setExamId(ues.getExamId());
                    examQuAnswer1.setUserId(ues.getUserId());
                    examQuAnswer1.setQuestionId(temp.getQuestionId());
                    examQuAnswer1.setQuestionType(temp.getType());
                    examQuAnswer1.setIsRight(-1);
                    examQuAnswerMapper.insert(examQuAnswer1);
                });
            }

        }

        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userExamsScoreLambdaUpdateWrapper.set(UserExamsScore::getWhetherMark, -1)
                .eq(UserExamsScore::getExamId, ues.getExamId())
                .eq(UserExamsScore::getUserId, ues.getUserId());
        userExamsScoreMapper.update(userExamsScoreLambdaUpdateWrapper);
        return Result.success("交卷成功");
    }

}