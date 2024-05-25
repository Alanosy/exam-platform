package cn.org.alan.exam.model.vo.exam;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/20 9:26 AM
 */
@Data
public class ExamQuestionVO {
    private Integer id;
    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 试题id  唯一
     */
    private Integer questionId;
    /**
     * 分数
     */
    private Integer score;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 类型
     */
    private Boolean checkout;
}
