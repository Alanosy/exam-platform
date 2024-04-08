package cn.org.alan.exam.model.vo.exam;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/1 3:37 PM
 */
@Data
public class ExamDetailRespVO {
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
}
