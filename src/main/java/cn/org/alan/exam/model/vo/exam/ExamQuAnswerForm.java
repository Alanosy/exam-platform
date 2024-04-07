package cn.org.alan.exam.model.vo.exam;

import lombok.Data;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/7 11:54 PM
 */
@Data
public class ExamQuAnswerForm {
    private Integer examId;
    private Integer questionId;
    /**
     * 回答列表
     */
    private List<String> answers;

    /**
     * 主观答案
     */
    private String answer;
}
