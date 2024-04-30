package cn.org.alan.exam.model.vo.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/7 11:54 PM
 */
@Data
public class ExamQuAnswerVO {
    private Integer examId;
    private Integer quId;
    /**
     * 回答答案
     */
    @NotBlank
    private String answer;
}
