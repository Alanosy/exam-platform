package org.dromara.exam.question.domain.bo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_option")
public class QuestionOption {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试题id */
    private Long questionId;

    /** 选项标识 A/B/C/D */
    private String optionKey;

    /** 选项内容富文本 */
    private String optionContent;

    /** 排序 */
    private Integer sort;

    private Long tenantId;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
