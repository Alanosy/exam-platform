package org.dromara.exam.answer.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 考试逐题作答对象 exam_answer
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_answer")
public class ExamAnswer extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 未判分 */
    public static final int CORRECT_UNKNOWN = 0;

    /** 正确 */
    public static final int CORRECT_YES = 1;

    /** 错误 */
    public static final int CORRECT_NO = 2;

    /**
     * 部分正确：开了部分得分后答对了一部分（多选漏选、填空只对部分空）
     *
     * <p>既不是「完全答对」也不是「完全答错」，统计时要单独拎出来，
     * 否则「正确率」会把半对的算成错的，数字会比实际偏低。
     */
    public static final int CORRECT_PARTIAL = 3;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 答卷记录ID
     */
    private Long recordId;

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 题型
     */
    private String questionType;

    /**
     * 考生作答JSON
     */
    private String answerContent;

    /**
     * 本题得分
     */
    private BigDecimal score;

    /**
     * 0未判 1正确 2错误
     */
    private Integer correct;

    /**
     * 题号顺序
     */
    private Integer sort;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Long delFlag;


}
