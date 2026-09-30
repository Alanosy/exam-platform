package cn.org.alan.exam.model.form.exam_qu_answer;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/6 11:27 AM
 */
@Data
public class ExamQuAnswerAddForm {
    // 试卷ID
    private Integer examId;
    // 试题ID
    private Integer quId;
    // 回答答案
    @NotBlank
    private String answer;
}
