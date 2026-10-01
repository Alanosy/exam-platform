package org.dromara.exam.mark.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 阅卷操作日志对象 exam_mark_log
 *
 * <p>打分、改分、AI 预评、完成阅卷都留痕，分数有争议时能查到是谁在什么时候改的。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_mark_log")
public class MarkLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 建任务 */
    public static final String ACTION_CREATE = "create";

    /** 打分 */
    public static final String ACTION_SCORE = "score";

    /** 改分 */
    public static final String ACTION_RESCORE = "rescore";

    /** AI 预评 */
    public static final String ACTION_AI = "ai";

    /** 完成阅卷 */
    public static final String ACTION_FINISH = "finish";

    /** 主键ID */
    private Long id;

    /** 阅卷任务ID */
    private Long taskId;

    /** 阅卷明细ID，任务级操作为空 */
    private Long itemId;

    /** 答卷记录ID */
    private Long recordId;

    /** 试题ID，任务级操作为空 */
    private Long questionId;

    /** create建任务 / score打分 / rescore改分 / ai预评 / finish完成阅卷 */
    private String action;

    /** 改动前得分 */
    private BigDecimal oldScore;

    /** 改动后得分 */
    private BigDecimal newScore;

    /** manual / ai / auto */
    private String markType;

    /** 备注 */
    private String remark;

    /** 操作人ID */
    private Long operator;

    /** 操作人姓名 */
    private String operatorName;

    /** 逻辑删除 0未删 1已删 */
    private Long delFlag;
}
