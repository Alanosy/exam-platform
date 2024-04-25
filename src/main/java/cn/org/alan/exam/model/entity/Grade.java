package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/4/25 14:02
 */
@TableName("t_grade")
public class Grade implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Id   班级表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer Id;

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

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        this.Id = id;
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
                "Id=" + Id +
                ", gradeName=" + gradeName +
                ", userId=" + userId +
                ", code=" + code +
                "}";
    }
}
