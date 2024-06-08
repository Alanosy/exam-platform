package cn.org.alan.exam.model.vo;


import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 8:06 PM
 */
@Data
public class GradeVO {
    private static final long serialVersionUID = 1L;

    /**
     * id   班级表
     */
    private Integer id;

    /**
     * 班级名称
     */
    private String gradeName;

    /**
     * 创建人id
     */
    private Integer userId;

    /**
     * 创建人名称
     */
    private String userName;

    /**
     * 班级口令
     */
    private String code;

    /**
     * 班级人数
     */
    private Integer gradeCount;
}
