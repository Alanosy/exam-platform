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
@TableName("t_exam_grade")
public class ExamGrade implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id   考试与班级关系表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 班级id  唯一
     */
    private Integer gradeId;

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
    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    @Override
    public String toString() {
        return "ExamGrade{" +
            "id=" + id +
            ", examId=" + examId +
            ", gradeId=" + gradeId +
        "}";
    }
}
