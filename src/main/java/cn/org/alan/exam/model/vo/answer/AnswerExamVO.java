package cn.org.alan.exam.model.vo.answer;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/15 2:17 PM
 */
@Data
public class AnswerExamVO {
    private Integer examId;
    private String examTitle;
    private Integer neededMark;
    private Integer classSize;
    private Integer numberOfApplicants;
    private Integer correctedPaper;
}
