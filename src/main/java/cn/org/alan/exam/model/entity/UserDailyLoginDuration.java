package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 用户每日登录时长实体类
 *
 * @Author Alan
 * @Version
 * @Date 2024/5/28 10:44 PM
 */
@Data
@ApiModel("用户每日登录时长实体类")
@TableName("t_user_daily_login_duration")
public class UserDailyLoginDuration implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户每日登录时长ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    @TableField("user_id")
    private Integer userId;

    @ApiModelProperty("登录日期")
    @TableField("login_date")
    private LocalDate loginDate;

    @ApiModelProperty("累积在线秒数")
    @TableField("total_seconds")
    private Integer totalSeconds;

}