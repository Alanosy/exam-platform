package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@TableName("t_exam_qu_answer")
public class ExamQuAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id  考试记录答案
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 考试id
     */
    private Integer examId;

    /**
     * 试题id
     */
    private Integer questionId;

    /**
     * 题目类型
     */
    private Integer questionType;

    /**
     * 答案id  用于客观题，多选题id使用“，”分隔
     */
    private String answerId;

    /**
     * 答案内容    用于主观题
     */
    private String answerContent;

    /**
     * 是否选中   0未选中  1选中
     */
    private Integer checkout;

    /**
     * 是否标记   0未标记  1标记
     */
    private Integer isSign;

    /**
     * 是否正确   用于客观题，0错误 1正确
     */
    private Integer isRight;

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
    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }
    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }
    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }
    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }
    public Integer getCheckout() {
        return checkout;
    }

    public void setCheckout(Integer checkout) {
        this.checkout = checkout;
    }
    public Integer getIsSign() {
        return isSign;
    }

    public void setIsSign(Integer isSign) {
        this.isSign = isSign;
    }
    public Integer getIsRight() {
        return isRight;
    }

    public void setIsRight(Integer isRight) {
        this.isRight = isRight;
    }

    @Override
    public String toString() {
        return "ExamQuAnswer{" +
            "id=" + id +
            ", userId=" + userId +
            ", examId=" + examId +
            ", questionId=" + questionId +
            ", questionType=" + questionType +
            ", answerId=" + answerId +
            ", answerContent=" + answerContent +
            ", checkout=" + checkout +
            ", isSign=" + isSign +
            ", isRight=" + isRight +
        "}";
    }
}
