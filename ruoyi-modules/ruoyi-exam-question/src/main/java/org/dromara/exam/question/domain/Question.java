package org.dromara.exam.question.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 试题主对象 question
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属题库ID
     */
    private Long bankId;

    /**
     * 题干富文本
     */
    private String title;

    /**
     * 题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题
     */
    private String questionType;

    /**
     * 难度 easy简单 medium中等 hard困难
     */
    private String difficulty;

    /**
     * 题目默认分值
     */
    private Long score;

    /**
     * 试题解析富文本
     */
    private String analysis;

    /**
     * 参考答案JSON，不同题型结构不同
     */
    private String answer;

    /**
     * 题目创建人ID
     */
    private Long createUser;

    /**
     * 0草稿 1启用 2废弃
     */
    private Long status;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
