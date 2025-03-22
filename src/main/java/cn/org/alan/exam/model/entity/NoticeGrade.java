package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 公告班级关联实体类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Data
@ApiModel("公告班级关联实体类")
@TableName("t_notice_grade")
public class NoticeGrade implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("公告班级关联表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("公告ID")
    private Integer noticeId;

    @ApiModelProperty("班级ID")
    private Integer gradeId;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Integer isDeleted;
}
