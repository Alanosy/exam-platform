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
@TableName("t_user_exams_score")
public class UserExamsScore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id  用户考试成绩表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id  唯一
     */
    private Integer userId;

    /**
     * 试卷id  唯一
     */
    private Integer examId;

    /**
     * 总时长  YYYY-MM-DD hh:mm:ss
     */
    private Integer totalTime;

    /**
     * 用户用时  YYYY-MM-DD hh:mm:ss
     */
    private Integer userTime;

    /**
     * 用户得分
     */
    private Integer userScore;

    /**
     * 交卷时间  YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime limitTime;

    /**
     * 切屏次数
     */
    private Integer count;

    /**
     * 状态   0正在考试 1考试完成
     */
    private Integer state;

    /**
     * 创建时间  YYYY-MM-DD hh:mm:ss
     */
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
    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }
    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }
    public Integer getUserTime() {
        return userTime;
    }

    public void setUserTime(Integer userTime) {
        this.userTime = userTime;
    }
    public Integer getUserScore() {
        return userScore;
    }

    public void setUserScore(Integer userScore) {
        this.userScore = userScore;
    }
    public LocalDateTime getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(LocalDateTime limitTime) {
        this.limitTime = limitTime;
    }
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserExamsScore{" +
            "id=" + id +
            ", userId=" + userId +
            ", examId=" + examId +
            ", totalTime=" + totalTime +
            ", userTime=" + userTime +
            ", userScore=" + userScore +
            ", limitTime=" + limitTime +
            ", count=" + count +
            ", state=" + state +
            ", createTime=" + createTime +
        "}";
    }
}
