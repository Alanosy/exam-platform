package cn.org.alan.exam.model.form.answer;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/15 1:37 PM
 */
@Data
public class CorrectAnswer {
    private Integer userId;
    private Integer examId;
    private Integer questionId;
    private Integer score;
}
