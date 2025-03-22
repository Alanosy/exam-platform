package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 试题实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("试题实体类")
@TableName("t_question")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("试题ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("试题类型")
    private Integer quType;

    @ApiModelProperty("试题图片")
    private String image;

    @ApiModelProperty("题干")
    private String content;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("题目分析")
    private String analysis;

    @ApiModelProperty("题库ID")
    private Integer repoId;

    @ApiModelProperty("创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;
}
