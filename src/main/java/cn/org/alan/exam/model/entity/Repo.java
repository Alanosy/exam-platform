package cn.org.alan.exam.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    
    @ApiModelProperty(value = "分类ID")
    private Integer categoryId;

    @ApiModelProperty(value = "显示排序，越小越靠前")
    private Integer sort;
}
