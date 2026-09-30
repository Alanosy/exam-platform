package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 刷题记录实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("刷题记录实体类")
@TableName("t_exercise_record")
public class ExerciseRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("刷题记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("题库ID")
    private Integer repoId;

    @ApiModelProperty("试题ID")
    private Integer questionId;

    @ApiModelProperty("用户ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("主观题答案")
    private String answer;

    @ApiModelProperty("题目类型")
    private Integer questionType;

    /**
     * 用于客观题,多选题id使用","分隔
     */
    @ApiModelProperty("客观题答案集合")
    private String options;

    @ApiModelProperty("客观题是否正确")
    private Integer isRight;

}
