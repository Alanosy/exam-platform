package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.sf.jsqlparser.expression.MySQLGroupConcat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("题库实体类")
@TableName("t_repo")
public class Repo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("题库ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("题库标题")
    @NotBlank(message = "题库名不能为空")
    private String title;

    @ApiModelProperty("是否可以刷题")
    private Integer isExercise;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;

}
