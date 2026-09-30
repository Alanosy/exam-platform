package cn.org.alan.exam.model.vo.exam;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/8 10:07 AM
 */
@Data
public class ExamGradeVO {
    private Integer id;

    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 班级id  唯一
     */
    private Integer gradeId;
}
