package org.dromara.exam.cert.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 证书颁发入参（答题服务 → 证书服务）
 *
 * <p>答卷与成绩在 ry-exam-answer 库，证书表在 ry-exam 库，跨库不直连，
 * 所以成绩由答题服务算完后整体带过来，证书服务只做「够不够格 + 落库」。
 *
 * <p>这里带的分数一律是**最终成绩**：客观题卷交卷时就是最终分；
 * 含主观题的卷子要等阅卷完成（writeBackMark finished=true）才调用，
 * 否则会把「客观题分」当最终分发出去，考生阅完卷反而分数变了。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class RemoteCertIssueBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称（快照，考试改名后证书上仍是当时的名字） */
    private String examName;

    /** 答卷记录ID，同一份答卷只发一张证书 */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号（登录名） */
    private String account;

    /** 考生姓名 */
    private String nickName;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 最终得分 */
    private BigDecimal score;

    /** 及格分 */
    private BigDecimal passScore;

    /** 试卷总分 */
    private BigDecimal totalScore;

    /** 是否及格，false 时不颁发（重复交卷 / 阅卷后变成不及格会走这个分支） */
    private Boolean passed;

    /** 交卷时间，作为证书颁发时间 */
    private java.util.Date submitTime;

    /**
     * 租户ID：跨服务调用拿不到租户上下文，显式带过去
     *
     * <p>类型是 String 而不是 Long：TenantHelper.getTenantId() 返回的就是 String
     * （TenantEntity.tenantId 同为 String），写成 Long 会在 ObjectUtil.defaultIfNull
     * 那里类型推断冲突，直接编译不过。
     */
    private String tenantId;
}
