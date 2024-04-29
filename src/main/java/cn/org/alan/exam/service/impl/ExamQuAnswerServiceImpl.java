package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.vo.score.QuestionAnalyseVO;
import cn.org.alan.exam.service.IExamQuAnswerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
    public List<ExamQuAnswer> listForFill(Integer examId, Integer questionId) {
        QueryWrapper<ExamQuAnswer> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getQuestionId, questionId);

        return this.list(wrapper);
    }

    @Override
    public void updateByKey(ExamQuAnswer equ) {
        //查询条件
        QueryWrapper<ExamQuAnswer> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ExamQuAnswer::getExamId, equ.getExamId())
                .eq(ExamQuAnswer::getQuestionId, equ.getQuestionId());

        this.update(equ, wrapper);
    }

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
