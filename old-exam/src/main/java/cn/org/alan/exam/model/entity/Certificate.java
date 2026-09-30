package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 证书实体类
 *
 * @ Author Alan
 * @ Version 1.0
 * @ Date 2024/4/25 14:08
 */
@Data
@ApiModel("证书实体类")
@TableName("t_certificate")
public class Certificate implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("证书ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("证书名称")
    private String certificateName;

    @ApiModelProperty("背景图片")
    private String image;

    @ApiModelProperty("认证单位")
    private String certificationNuit;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;
}
