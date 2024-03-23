package com.wj.exam.entity;

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
@TableName("t_user_book")
public class UserBook implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID   错题本
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 用户id    唯一
     */
    private Integer userId;

    /**
     * 题目id    唯一
     */
    private Integer quId;

    /**
     * 创建时间    YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getQuId() {
        return quId;
    }

    public void setQuId(Integer quId) {
        this.quId = quId;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserBook{" +
            "id=" + id +
            ", examId=" + examId +
            ", userId=" + userId +
            ", quId=" + quId +
            ", createTime=" + createTime +
        "}";
    }
}
