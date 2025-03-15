package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

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
@TableName("t_notice")
@Data
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID   公告表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 图片地址
     */
    private String image;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 创建用户ID   唯一
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 是否公开 教师：所有班级用户 管理员：所有用户
     */
    private Integer isPublic;

    /**
     * 逻辑删除字段
     */
    private Integer isDeleted;
}
