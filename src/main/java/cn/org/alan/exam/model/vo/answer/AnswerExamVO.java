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
     * 待阅卷数量
     */
    private Integer neededMark;
    /**
     * 班级总人数
     */
    private Integer classSize;
    /**
     * 已阅人数
     */
    private Integer numberOfApplicants;
    private Integer correctedPaper;
}
