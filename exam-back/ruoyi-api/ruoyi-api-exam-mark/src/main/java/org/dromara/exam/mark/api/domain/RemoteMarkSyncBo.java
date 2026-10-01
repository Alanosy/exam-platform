package org.dromara.exam.mark.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 阅卷任务同步入参（答题服务交卷后调用）
 *
 * <p>只放建任务必需的字段，题目正文 / 参考答案 / 解析由阅卷服务自己向题库服务取，
 * 避免把富文本在 Dubbo 上来回传一遍。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class RemoteMarkSyncBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷记录ID（答题库 exam_record 主键） */
    private Long recordId;

    /** 考试ID */
    private Long examId;

    /** 试卷ID */
    private Long paperId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 交卷时间 */
    private Date submitTime;

    /** 客观题得分（已自动判分） */
    private BigDecimal objectiveScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 租户ID，跨服务调用时 TenantHelper 取不到，由调用方显式带过来 */
    private String tenantId;

    /** 待阅的主观题作答，为空表示本卷没有主观题 */
    private List<RemoteMarkItemSyncBo> items;
}
