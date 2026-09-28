package org.dromara.exam.question.domain.bo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题库名称 */
    private String bankName;

    /** 题库描述 */
    private String bankDesc;

    /** 创建人ID */
    private Long creatorId;

    /** 可见性 private / public */
    private String visibility;

    /** 状态 0草稿 1正常 2归档 */
    private Integer status;

    /** 租户id */
    private Long tenantId;

    /** 逻辑删除 */
    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
