package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 考试试题实体类
 *
 * @Author Alan
 * @Version
 * @Date 2024/4/7 3:42 PM
 */
@Data
@ApiModel("考试试题实体类")
@TableName("t_exam_question")
public class ExamQuestion {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("考试试题ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("考试ID")
    private Integer examId;

    @ApiModelProperty("试题ID")
    private Integer questionId;

    @ApiModelProperty("分数")
    private Integer score;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("类型")
    private Integer type;
}
