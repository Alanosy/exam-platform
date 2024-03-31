package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@TableName("t_grade_exercise")
public class GradeExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id  刷题表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题库id
     */
    private Integer repoId;

    /**
     * 班级id
     */
    private Integer gradeId;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getRepoId() {
        return repoId;
    }

    public void setRepoId(Integer repoId) {
        this.repoId = repoId;
    }
    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "GradeExercise{" +
            "id=" + id +
            ", repoId=" + repoId +
            ", gradeId=" + gradeId +
            ", userId=" + userId +
            ", createTime=" + createTime +
        "}";
    }
}
