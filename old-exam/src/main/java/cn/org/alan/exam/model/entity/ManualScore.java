package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("人工评分表")
@TableName("t_manual_score")
public class ManualScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("人工评分表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("考试记录答案ID")
    private Integer examQuAnswerId;

    @ApiModelProperty("批改人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("得分")
    private Integer score;

    @ApiModelProperty("批改时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
