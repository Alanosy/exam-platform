package cn.org.alan.exam.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.service.IAuthService;
import cn.org.alan.exam.utils.agent.AIChat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.org.alan.exam.model.vo.question.QuestionScoreVO;
import cn.org.alan.exam.service.IAutoScoringService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AutoScoringServiceImpl extends ServiceImpl<ExamQuAnswerMapper, ExamQuAnswer> implements IAutoScoringService {

    @Autowired
    private ExamQuAnswerMapper examQuAnswerMapper;

    @Autowired
    private AIChat aiChat;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Override
    @Async
    public void autoScoringExam(Integer examId, Integer userId) {
        int maxAttempts = 3; // 最大重试次数
        long retryDelay = 5000; // 每次重试之间的间隔时间（毫秒）

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // 定义事务属性
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            // 开启事务
            TransactionStatus status = platformTransactionManager.getTransaction(def);
            try {
                // 1. 获取考试答案
                List<QuestionScoreVO> questions = examQuAnswerMapper.getQuestionsForGrading(examId, userId);

                // 2. 构建评分请求并转化为 JSON 字符串
                String scoringRequest = JSONUtil.toJsonStr(questions);

                // 调用 AI 聊天接口，返回结果是 JSON 格式
                String scoringResult = null; // 去掉 Markdown 标记
                String response = aiChat.getChatResponse(scoringRequest).trim();
                System.out.println(response);
                Pattern pattern = Pattern.compile("```json\\r?\\n(.*?)```", Pattern.DOTALL | Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    scoringResult = matcher.group(1).trim();
                } else {
                    System.out.println("JSON内容未匹配");
                }

                // 4. 解析评分结果
                JSONArray scoreArray = JSONUtil
                        .parseArray(JSONUtil
                                .parseObj(scoringResult)
                                .getStr("评分结果"));

                // 5. 更新ai评分和扣分原因到数据库
                for (int i = 0; i < scoreArray.size(); i++) {
                    JSONObject item = scoreArray.getJSONObject(i);
                    ExamQuAnswer examQuAnswer = new ExamQuAnswer();
                    examQuAnswer.setQuestionId(Integer.valueOf(item.getStr("题目ID")));
                    examQuAnswer.setAiScore(Integer.valueOf(item.getStr("最终得分")));
                    examQuAnswer.setAiReason(item.getStr("扣分原因"));

                    // 构建查询条件
                    LambdaQueryWrapper<ExamQuAnswer> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ExamQuAnswer::getExamId, examId)
                            .eq(ExamQuAnswer::getUserId, userId)
                            .eq(ExamQuAnswer::getQuestionId, examQuAnswer.getQuestionId());

                    // 获取当前需要评分的记录
                    ExamQuAnswer existingRecord = getOne(queryWrapper);
                    if (existingRecord != null) {
                        // 如果存在，更新记录
                        examQuAnswer.setId(existingRecord.getId());
                        updateById(examQuAnswer);
                    } else {
                        // 否则抛出异常
                        throw new ServiceRuntimeException("ai评分失败！");
                    }
                }

                // ai评分成功，提交事务，跳出重试循环
                platformTransactionManager.commit(status);
                return;
            } catch (Exception e) {
                // 回滚事务
                platformTransactionManager.rollback(status);
                // 如果达到最大重试次数，抛出异常
                if (attempt == maxAttempts) {
                    throw new RuntimeException("ai评分重试多次后仍然失败！", e);
                }

                // 等待一段时间后重试
                try {
                    TimeUnit.MILLISECONDS.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("线程中断！", ie);
                }
            }
        }
    }
}