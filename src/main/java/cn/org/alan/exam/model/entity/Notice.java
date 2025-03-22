package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体类
 *
Apiauthor WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("公告实体类")
@TableName("t_notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("公告ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("公告标题")
    private String title;

    @ApiModelProperty("图片地址")
    private String image;

    @ApiModelProperty("公告内容")
    private String content;

    @ApiModelProperty("创建用户ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 教师：所有班级用户 管理员：所有用户
     */
    @ApiModelProperty("是否公开")
    private Integer isPublic;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;
}
