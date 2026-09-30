package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级刷题关联实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("班级刷题关联实体类")
@TableName("t_grade_exercise")
public class GradeExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("刷题表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("题库ID")
    private Integer repoId;

    @ApiModelProperty("班级ID")
    private Integer gradeId;

    @ApiModelProperty("创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
