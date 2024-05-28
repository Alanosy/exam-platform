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
    /**
     * 是否需要阅卷
     */
    private Integer neededMark;
    /**
     * 班级总人数
     */
    private Integer classSize;

    private Integer numberOfApplicants;
    /**
     * 已阅人数
     */
    private Integer correctedPaper;
}
