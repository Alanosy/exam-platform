package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import net.sf.jsqlparser.expression.MySQLGroupConcat;

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
@TableName("t_repo")
public class Repo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id   题库表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 题库标题
     */
    @NotNull(message = "题库名不能为空")
    private String title;

    /**
     * 创建时间
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
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Repo{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", createTime=" + createTime +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
