package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.vo.score.QuestionAnalyseVO;
import cn.org.alan.exam.service.IExamQuAnswerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
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
public class ExamQuAnswerServiceImpl extends ServiceImpl<ExamQuAnswerMapper, ExamQuAnswer> implements IExamQuAnswerService {

    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;

    @Override
    public Result<QuestionAnalyseVO> questionAnalyse(Integer examId, Integer questionId) {
        QuestionAnalyseVO questionAnalyseVO = examQuAnswerMapper.questionAnalyse(examId, questionId);
        //正确率保留两位小数
        DecimalFormat format = new DecimalFormat("#.00");
        String strAccuracy = format.format(questionAnalyseVO.getRightCount() / questionAnalyseVO.getTotalCount());
        questionAnalyseVO.setAccuracy(Double.parseDouble(strAccuracy));
        return Result.success(null, questionAnalyseVO);
    }

}
