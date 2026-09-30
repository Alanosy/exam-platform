package cn.org.alan.exam.model.form.exam;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/7 11:54 PM
 */
@Data
public class ExamQuAnswerFrom {
    // 试卷ID
    private Integer examId;
    // 试题ID
    private Integer quId;
    // 回答答案
    @NotBlank
    private String answer;
}
