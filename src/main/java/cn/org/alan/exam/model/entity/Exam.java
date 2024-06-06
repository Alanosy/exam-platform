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
@TableName("t_exam")
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id    考试表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试名称
     */
    private String title;

    /**
     * 考试时长
     */
    private Integer examDuration;

    /**
     * 及格分
     */
    private Integer passedScore;

    /**
     * 总分数
     */
    private Integer grossScore;

    /**
     * 最大切屏次数
     */
    private Integer maxCount;

    /**
     * 创建者id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 证书id
     */
    private Integer certificateId;

    /**
     * 单选题数量
     */
    private Integer radioCount;

    /**
     * 单选题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer radioScore;

    /**
     * 多选题数量 
     */
    private Integer multiCount;

    /**
     * 多选题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer multiScore;

    /**
     * 判断题数量 
     */
    private Integer judgeCount;

    /**
     * 判断题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer judgeScore;

    /**
     * 简答题数量
     */
    private Integer saqCount;

    /**
     * 简答题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer saqScore;

    /**
     * 开始时间     YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime startTime;

    /**
     * 结束时间     YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime endTime;

    /**
     * 创建时间     YYYY-MM-DD hh:mm:ss  
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除字段
     */
    private Integer isDeleted;

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getExamDuration() {
        return examDuration;
    }

    public void setExamDuration(Integer examDuration) {
        this.examDuration = examDuration;
    }
    public Integer getPassedScore() {
        return passedScore;
    }

    public void setPassedScore(Integer passedScore) {
        this.passedScore = passedScore;
    }
    public Integer getGrossScore() {
        return grossScore;
    }

    public void setGrossScore(Integer grossScore) {
        this.grossScore = grossScore;
    }
    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(Integer certificateId) {
        this.certificateId = certificateId;
    }
    public Integer getRadioCount() {
        return radioCount;
    }

    public void setRadioCount(Integer radioCount) {
        this.radioCount = radioCount;
    }
    public Integer getRadioScore() {
        return radioScore;
    }

    public void setRadioScore(Integer radioScore) {
        this.radioScore = radioScore;
    }
    public Integer getMultiCount() {
        return multiCount;
    }

    public void setMultiCount(Integer multiCount) {
        this.multiCount = multiCount;
    }
    public Integer getMultiScore() {
        return multiScore;
    }

    public void setMultiScore(Integer multilScore) {
        this.multiScore = multilScore;
    }
    public Integer getJudgeCount() {
        return judgeCount;
    }

    public void setJudgeCount(Integer judgeCount) {
        this.judgeCount = judgeCount;
    }
    public Integer getJudgeScore() {
        return judgeScore;
    }

    public void setJudgeScore(Integer judgeScore) {
        this.judgeScore = judgeScore;
    }
    public Integer getSaqCount() {
        return saqCount;
    }

    public void setSaqCount(Integer saqCount) {
        this.saqCount = saqCount;
    }
    public Integer getSaqScore() {
        return saqScore;
    }

    public void setSaqScore(Integer saqScore) {
        this.saqScore = saqScore;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", examDuration=" + examDuration +
                ", passedScore=" + passedScore +
                ", grossScore=" + grossScore +
                ", maxCount=" + maxCount +
                ", userId=" + userId +
                ", certificateId=" + certificateId +
                ", radioCount=" + radioCount +
                ", radioScore=" + radioScore +
                ", multiCount=" + multiCount +
                ", multiScore=" + multiScore +
                ", judgeCount=" + judgeCount +
                ", judgeScore=" + judgeScore +
                ", saqCount=" + saqCount +
                ", saqScore=" + saqScore +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createTime=" + createTime +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
