package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户证书关联实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("用户证书关联实体类")
@TableName("t_certificate_user")
public class CertificateUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("证书与用户关系表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @ApiModelProperty("考试ID")
    private Integer examId;

    @ApiModelProperty("证书编号")
    private String code;

    @ApiModelProperty("证书ID")
    private Integer certificateId;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @ApiModelProperty("获奖时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
