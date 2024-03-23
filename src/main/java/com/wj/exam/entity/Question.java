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
@TableName("t_question")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id   试题表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 试题类型
     */
    private String quType;

    /**
     * 试题图片
     */
    private String image;

    /**
     * 题干
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 题目分析
     */
    private String analysis;

    /**
     * 题库id
     */
    private Integer repoId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getQuType() {
        return quType;
    }

    public void setQuType(String quType) {
        this.quType = quType;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
    public Integer getRepoId() {
        return repoId;
    }

    public void setRepoId(Integer repoId) {
        this.repoId = repoId;
    }

    @Override
    public String toString() {
        return "Question{" +
            "id=" + id +
            ", quType=" + quType +
            ", image=" + image +
            ", content=" + content +
            ", createTime=" + createTime +
            ", analysis=" + analysis +
            ", repoId=" + repoId +
        "}";
    }
}
