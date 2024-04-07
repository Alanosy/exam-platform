package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.vo.exam.PaperQuAnswerExtVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface ExamQuAnswerMapper extends BaseMapper<ExamQuAnswer> {

    List<PaperQuAnswerExtVO> list(String examId, String questionId);
}
