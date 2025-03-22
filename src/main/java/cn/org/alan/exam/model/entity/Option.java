package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 选项实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("选项实体类")
@TableName("t_option")
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("选项ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("试题ID")
    private Integer quId;

    @ApiModelProperty("是否正确")
    @NotNull(message = "选型是否正确(isRight)不能为空")
    @Min(value = 0,message = "选项是否正确(isRight)只能是：0错误1正确")
    @Max(value = 1,message = "选项是否正确(isRight)只能是：0错误1正确")
    private Integer isRight;

    /**
     * 0错误 1正确
     */
    @ApiModelProperty("图片地址")
    private String image;

    @ApiModelProperty("选项内容")
    @NotBlank(message = "选型内容(content)不能为空")
    private String content;

    @ApiModelProperty("排序")
    private Integer sort;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;
}
