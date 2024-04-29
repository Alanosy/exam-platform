package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ManualScore;
import cn.org.alan.exam.model.form.answer.AnswerUpdate;
import cn.org.alan.exam.model.vo.answer.AnswerExamVO;
import cn.org.alan.exam.model.vo.answer.UncorrectedUserVO;
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
     * 批改试卷
     * @param answerUpdate
     * @return
     */
    Result correct(AnswerUpdate answerUpdate);

    /**
     * 分页查找待阅卷考试
     * @return
     */
    Result<IPage<AnswerExamVO>> examPage(Integer pageNum, Integer pageSize);

    Result<IPage<UncorrectedUserVO>> stuExamPage(Integer pageNum, Integer pageSize, Integer examId);
}
