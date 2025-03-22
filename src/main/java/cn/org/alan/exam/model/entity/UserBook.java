package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 错题本实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("错题本实体类")
@TableName("t_user_book")
public class UserBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("错题本ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("考试ID")
    private Integer examId;

    @ApiModelProperty("用户ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("题目ID")
    private Integer quId;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
