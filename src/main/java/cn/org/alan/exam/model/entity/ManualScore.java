package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("t_manual_score")
public class ManualScore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id   人工评分表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试记录答案id
     */
    private Integer examQuAnswerId;

    /**
     * 批改人id
     */
    private Integer userId;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 批改时间
     */
    private LocalDateTime createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getExamQuAnswerId() {
        return examQuAnswerId;
    }

    public void setExamQuAnswerId(Integer examQuAnswerId) {
        this.examQuAnswerId = examQuAnswerId;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ManualScore{" +
            "id=" + id +
            ", examQuAnswerId=" + examQuAnswerId +
            ", userId=" + userId +
            ", score=" + score +
            ", createTime=" + createTime +
        "}";
    }
}
