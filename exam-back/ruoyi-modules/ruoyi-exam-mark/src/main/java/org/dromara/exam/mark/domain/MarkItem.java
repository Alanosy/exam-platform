package org.dromara.exam.mark.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 阅卷明细对象 exam_mark_item
 *
 * <p>一道主观题一条：题干 / 参考答案 / 解析在题库服务，阅卷时按 questionId 批量取回，
 * 这里只冗余存作答快照与分值，避免富文本在主库里再存一份。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_mark_item")
public class MarkItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 待阅 */
    public static final String STATUS_PENDING = "pending";

    /** 已阅 */
    public static final String STATUS_MARKED = "marked";

    /** 定稿方式：人工 */
    public static final String TYPE_MANUAL = "manual";

    /** 定稿方式：AI 预评后确认 */
    public static final String TYPE_AI = "ai";

    /** AI 未调用 */
    public static final String AI_NONE = "none";

    /** AI 评估中 */
    public static final String AI_RUNNING = "running";

    /** AI 评估成功 */
    public static final String AI_SUCCESS = "success";

    /** AI 评估失败 */
    public static final String AI_FAIL = "fail";

    /** 主键ID */
    private Long id;

    /** 阅卷任务ID */
    private Long taskId;

    /** 考试ID，冗余便于按考试统计 */
    private Long examId;

    /** 试卷ID */
    private Long paperId;

    /** 答卷记录ID */
    private Long recordId;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 题号顺序，与答卷一致 */
    private Integer sort;

    /** 本题满分 */
    private BigDecimal fullScore;

    /** 考生作答快照，阅卷时不再回查答题库 */
    private String answerContent;

    /** 最终得分 */
    private BigDecimal score;

    /** 0未判 1正确 2错误 */
    private Integer correct;

    /** pending待阅 / marked已阅 */
    private String status;

    /** 定稿方式 manual人工 / ai智能预评后确认 */
    private String markType;

    /** 阅卷评语 */
    private String markComment;

    /** 阅卷人ID */
    private Long marker;

    /** 阅卷人姓名 */
    private String markerName;

    /** 阅卷时间 */
    private Date markTime;

    /** AI 建议分 */
    private BigDecimal aiScore;

    /** AI 评分理由 */
    private String aiReason;

    /** none未调用 / running评估中 / success成功 / fail失败 */
    private String aiStatus;

    /** AI 模型标识 */
    private String aiModel;

    /** AI 评估时间 */
    private Date aiTime;

    /** 逻辑删除 0未删 1已删 */
    private Long delFlag;
}
