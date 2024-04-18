package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@TableName("t_grade")
public class Grade implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * gradeId   班级表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer gradeId;

    /**
     * 班级名称
     */
    private String gradeName;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 班级口令
     */
    private String code;

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }
    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Grade{" +
            "gradeId=" + gradeId +
            ", gradeName=" + gradeName +
            ", userId=" + userId +
            ", code=" + code +
        "}";
    }
}
