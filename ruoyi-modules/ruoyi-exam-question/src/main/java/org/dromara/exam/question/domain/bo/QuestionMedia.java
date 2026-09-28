package org.dromara.exam.question.domain.bo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_media")
public class QuestionMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    /** image / audio / video */
    private String mediaType;

    /** minio资源地址 */
    private String mediaUrl;

    /** 原始文件名 */
    private String mediaName;

    private Integer sort;

    private Long tenantId;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
