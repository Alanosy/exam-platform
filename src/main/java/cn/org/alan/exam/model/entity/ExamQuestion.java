package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/7 3:42 PM
 */
@TableName("t_exam_question")
public class ExamQuestion {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 试题id  唯一
     */
    private Integer questionId;
    /**
     * 分数
     */
    private Integer score;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 类型
     */
    private Integer type;

    @Override
    public String toString() {
        return "ExamQuestion{" +
                "id=" + id +
                ", examId=" + examId +
                ", questionId=" + questionId +
                ", score=" + score +
                ", sort=" + sort +
                ", type=" + type +
                '}';
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public Integer getExamId() {
        return examId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getSort() {
        return sort;
    }

    public Integer getType() {
        return type;
    }
}
