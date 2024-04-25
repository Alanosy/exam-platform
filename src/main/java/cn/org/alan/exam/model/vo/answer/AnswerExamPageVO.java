package cn.org.alan.exam.model.vo.answer;

import cn.org.alan.exam.model.entity.Exam;
import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/15 2:17 PM
 */
@Data
public class AnswerExamPageVO {
    private Exam exam;
    private Integer classSize;
    private Integer numberOfApplicants;
    private Integer correctedPaper;
}
