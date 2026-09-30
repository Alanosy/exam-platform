package cn.org.alan.exam.model.vo.stat;

import lombok.Data;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/5/12 14:38
 */
@Data
public class GradeStudentVO {
    private Long id;
    // 班级名称
    private String gradeName;
    // 班级下总学生数
    private Integer  totalStudent;

}
