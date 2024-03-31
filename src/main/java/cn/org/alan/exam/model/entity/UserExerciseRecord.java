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
@TableName("t_user_exercise_record")
public class UserExerciseRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id   用户刷题记录表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 题库id
     */
    private Integer repoId;

    /**
     * 总题数
     */
    private Integer totalCount;

    /**
     * 已刷题数
     */
    private Integer exerciseCount;

    /**
     * 刷题时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getRepoId() {
        return repoId;
    }

    public void setRepoId(Integer repoId) {
        this.repoId = repoId;
    }
    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    public Integer getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(Integer exerciseCount) {
        this.exerciseCount = exerciseCount;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserExerciseRecord{" +
            "id=" + id +
            ", userId=" + userId +
            ", repoId=" + repoId +
            ", totalCount=" + totalCount +
            ", exerciseCount=" + exerciseCount +
            ", createTime=" + createTime +
        "}";
    }
}
