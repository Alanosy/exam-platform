package cn.org.alan.exam.service;

import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.vo.exam.PaperQuAnswerExtVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IExamQuAnswerService extends IService<ExamQuAnswer> {

    List<PaperQuAnswerExtVO> listForExam(String examId, String questionId);

    List<ExamQuAnswer> listForFill(Integer examId, Integer questionId);

    void updateByKey(ExamQuAnswer equ);
}
