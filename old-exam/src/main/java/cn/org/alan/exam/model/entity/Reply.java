package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回复实体类
 *
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/4 13:16
 */
@Data
@TableName("t_reply")
@ApiModel("回复实体类")
public class Reply implements Serializable {

    @ApiModelProperty("id")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("讨论id")
    private Integer discussionId;

    @ApiModelProperty("用户id")
    private Integer userId;

    @ApiModelProperty("父id")
    private Integer parentId;

    @ApiModelProperty("回复内容")
    private String content;

    @ApiModelProperty("回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
