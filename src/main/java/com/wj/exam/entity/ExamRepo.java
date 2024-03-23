package com.wj.exam.entity;

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
@TableName("t_exam_repo")
public class ExamRepo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id  考试与题库
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 题库id  唯一
     */
    private Integer repoId;

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
    public Integer getRepoId() {
        return repoId;
    }

    public void setRepoId(Integer repoId) {
        this.repoId = repoId;
    }

    @Override
    public String toString() {
        return "ExamRepo{" +
            "id=" + id +
            ", examId=" + examId +
            ", repoId=" + repoId +
        "}";
    }
}
