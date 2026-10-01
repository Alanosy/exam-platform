package org.dromara.exam.mark.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 阅卷任务对象 exam_mark_task
 *
 * <p>一份答卷一条任务：考生交卷后由答题服务推主观题过来创建，
 * 教师逐题打分，全部打完才算 finished，成绩再回写答题库。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_mark_task")
public class MarkTask extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 待阅 */
    public static final String STATUS_PENDING = "pending";

    /** 阅卷中：至少阅过一道题，但还没阅完 */
    public static final String STATUS_MARKING = "marking";

    /** 已阅完 */
    public static final String STATUS_FINISHED = "finished";

    /** 主键ID */
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 试卷ID */
    private Long paperId;

    /** 答卷记录ID（答题库 exam_record 主键，跨库只存ID） */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 主观题总题数 */
    private Integer questionCount;

    /** 已阅题数 */
    private Integer markedCount;

    /** pending待阅 / marking阅卷中 / finished已阅完 */
    private String status;

    /** 客观题得分（开卷快照，阅卷时不改） */
    private BigDecimal objectiveScore;

    /** 主观题得分（阅卷累加） */
    private BigDecimal subjectiveScore;

    /** 总分 */
    private BigDecimal totalScore;

    /** 及格分（开卷快照） */
    private BigDecimal passScore;

    /** 是否及格 0否 1是 */
    private Long passed;

    /** 考生交卷时间快照 */
    private Date submitTime;

    /** 最近一次阅卷人ID */
    private Long marker;

    /** 最近一次阅卷人姓名 */
    private String markerName;

    /** 最近一次阅卷时间 */
    private Date markTime;

    /** 逻辑删除 0未删 1已删 */
    private Long delFlag;
}
