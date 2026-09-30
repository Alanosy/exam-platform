package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 说明：
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/4/4 2:50 PM
 */
@Data
@ApiModel("日志")
@TableName("t_log")
@Builder
public class Log {
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("登录地点")
    private String place;

    @ApiModelProperty("操作行为")
    private String behavior;

    @ApiModelProperty("登录设备")
    private String device;

    @ApiModelProperty("创建用户")
    private Integer userId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
