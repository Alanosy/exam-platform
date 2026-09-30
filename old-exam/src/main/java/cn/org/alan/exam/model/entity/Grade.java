package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 班级实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("班级实体类")
@TableName("t_grade")
public class Grade implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("班级ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("班级名称")
    private String gradeName;

    @ApiModelProperty("创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("班级口令")
    private String code;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;
}
