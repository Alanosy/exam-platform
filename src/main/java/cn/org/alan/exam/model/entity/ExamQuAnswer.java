package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 考试试题答案关联实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("考试试题答案关联实体类")
@TableName("t_exam_qu_answer")
public class ExamQuAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("考试记录答案ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("考试ID")
    private Integer examId;

    @ApiModelProperty("试题ID")
    private Integer questionId;

    @ApiModelProperty("题目类型")
    private Integer questionType;

    /**
     * 用于客观题，多选题id使用“，”分隔
     */
    @ApiModelProperty("答案ID")
    private String answerId;

    /**
     * 用于主观题
     */
    @ApiModelProperty("答案内容")
    private String answerContent;

    /**
     * 0未选中  1选中
     */
    @ApiModelProperty("是否选中")
    private Integer checkout;

    /**
     * 0未标记  1标记
     */
    @ApiModelProperty("是否标记")
    private Integer isSign;

    /**
     * 用于客观题，0错误 1正确
     */
    @ApiModelProperty("是否正确")
    private Integer isRight;

    /**
     * 用于客观题, ai评分
     */
    @ApiModelProperty("是否正确")
    private Integer aiScore;

    /**
     * 用于客观题，ai评分原因
     */
    @ApiModelProperty("ai评分原因")
    private String aiReason;
}
