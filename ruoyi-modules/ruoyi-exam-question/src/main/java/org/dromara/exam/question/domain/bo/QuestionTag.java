package org.dromara.exam.question.domain.bo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_tag")
public class QuestionTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;

    private Long creatorId;

    private Long tenantId;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
