package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.vo.answer.UserAnswerDetailVO;
import cn.org.alan.exam.model.vo.exam.ExamQuAnswerExtVO;
import cn.org.alan.exam.model.vo.score.QuestionAnalyseVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 试卷试题回答情况表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface ExamQuAnswerMapper extends BaseMapper<ExamQuAnswer> {

    /**
     * 获取单题作答信息
     *
     * @param examId     考试id
     * @param questionId 试题id
     * @return 查询结果
     */
    QuestionAnalyseVO questionAnalyse(Integer examId, Integer questionId);

    /**
     * 获取用户回答主观题信息
     *
     * @param userId 用户id
     * @param examId 考试id
     * @return 结果集
     */
    List<UserAnswerDetailVO> selectUserAnswer(Integer userId, Integer examId);

}
