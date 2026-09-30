package cn.org.alan.exam.model.vo.userbook;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/28 2:24 PM
 */
@Data
public class AddBookAnswerVO {
    // 返回是否正确
    private Integer correct;
    // 返回正确答案
    private String rightAnswers;
    // 返回分析
    private String analysis;
}
