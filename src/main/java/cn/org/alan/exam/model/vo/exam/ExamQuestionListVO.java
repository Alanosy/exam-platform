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
    private List<ExamQuestion> radioList;
    private List<ExamQuestion> multiList;
    private List<ExamQuestion> judgeList;
    private List<ExamQuestion> saqList;
    private Integer examDuration;
    public Long leftSeconds;
}
