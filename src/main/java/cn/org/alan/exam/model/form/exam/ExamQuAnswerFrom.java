package cn.org.alan.exam.model.form.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/7 11:54 PM
 */
@Data
public class ExamQuAnswerFrom {
    private Integer examId;
    private Integer quId;
    /**
     * 回答答案
     */
    @NotBlank
    private String answer;
}
