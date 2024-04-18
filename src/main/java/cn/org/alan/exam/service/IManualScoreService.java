package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ManualScore;
import cn.org.alan.exam.model.form.answer.AnswerUpdate;
import cn.org.alan.exam.model.vo.answer.AnswerExamPageVO;
import cn.org.alan.exam.model.vo.answer.AnswerPageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IManualScoreService extends IService<ManualScore> {
    /**
     * 试卷查询信息
     * @param userId
     * @param examId
     * @return
     */
    Result getDetail(Integer userId, Integer examId);

    /**
     * 分页查询答题列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    Result<IPage<AnswerPageVO>> getPaging(Integer pageNum, Integer pageSize);

    /**
     * 批改试卷
     * @param answerUpdate
     * @return
     */
    Result Correct(AnswerUpdate answerUpdate);

    /**
     * 分页查找待阅卷考试
     * @return
     */
    Result<IPage<AnswerExamPageVO>> examPage();
}
