package cn.org.alan.exam.model.vo.exam;

import cn.org.alan.exam.model.entity.ExamQuestion;
import lombok.Data;

import java.util.Calendar;
import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/11 9:45 AM
 */
@Data
public class ExamQuestionListVO {
    private List<ExamQuestionVO> radioList;
    private List<ExamQuestionVO> multiList;
    private List<ExamQuestionVO> judgeList;
    private List<ExamQuestionVO> saqList;
    private Integer examDuration;
    public Long leftSeconds;
}
