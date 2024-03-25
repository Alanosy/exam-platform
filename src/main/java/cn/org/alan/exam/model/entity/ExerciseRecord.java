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
@TableName("t_exercise_record")
public class ExerciseRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题库id
     */
    private Integer repoId;

    /**
     * 试题id
     */
    private Integer questionId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 主观题答案
     */
    private String answer;

    /**
     * 题目类型
     */
    private Integer questionType;

    /**
     * 客观题答案集合  用于客观题,多选题id使用","分隔
     */
    private String options;

    /**
     * 客观题是否正确
     */
    private Integer isRight;

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
    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }
    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }
    public Integer getIsRight() {
        return isRight;
    }

    public void setIsRight(Integer isRight) {
        this.isRight = isRight;
    }

    @Override
    public String toString() {
        return "ExerciseRecord{" +
            "id=" + id +
            ", repoId=" + repoId +
            ", questionId=" + questionId +
            ", userId=" + userId +
            ", answer=" + answer +
            ", questionType=" + questionType +
            ", options=" + options +
            ", isRight=" + isRight +
        "}";
    }
}
