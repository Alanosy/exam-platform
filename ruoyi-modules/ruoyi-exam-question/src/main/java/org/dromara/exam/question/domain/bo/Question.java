package org.dromara.exam.question.domain.bo;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "question", autoResultMap = true)
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属题库ID */
    private Long bankId;

    /** 题干富文本 */
    private String title;

    /** 题型 SINGLE/MULTIPLE... */
    private String questionType;

    /** 难度 easy / medium / hard */
    private String difficulty;

    /** 默认分值 */
    private BigDecimal score;

    /** 试题解析 */
    private String analysis;

    /** 参考答案JSON */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String,Object> answer;

    /** 题目创建人ID */
    private Long createUser;

    /** 0草稿 1启用 2废弃 */
    private Integer status;

    private Long tenantId;

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
