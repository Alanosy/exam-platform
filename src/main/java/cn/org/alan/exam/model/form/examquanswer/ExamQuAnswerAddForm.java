package cn.org.alan.exam.model.form.examquanswer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/6 11:27 AM
 */
@Data
public class ExamQuAnswerAddForm {
    private Integer examId;
    private Integer quId;
    /**
     * 回答答案
     */
    @NotBlank
    private String answer;
}
