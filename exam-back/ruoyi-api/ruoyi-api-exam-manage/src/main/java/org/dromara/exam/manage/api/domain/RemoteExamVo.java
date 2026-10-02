package org.dromara.exam.manage.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 考试信息（跨服务传输用）
 *
 * <p>答题服务开考前用它校验时间、迟到、重考次数等规则，
 * 只包含考生侧需要的字段，不含参与密码等敏感配置。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class RemoteExamVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 考试描述 */
    private String examDesc;

    /** 关联试卷ID */
    private Long paperId;

    /** 任务类型：1正式考试 / 2练习考试，答题服务据此决定要不要卡时间窗、允不允许反复练 */
    private String examType;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 限时(分钟)，0不限时 */
    private Long duration;

    /** 是否允许迟到入场 0否 1是 */
    private Long allowLate;

    /** 允许迟到分钟数 */
    private Long lateMinute;

    /** 是否允许重考 0否 1是 */
    private Long allowRetry;

    /** 最大重考次数 */
    private Long maxRetryCount;

    /** 答案展示 none / after_submit / after_exam */
    private String showAnswerMode;

    /** 客观题部分得分开关 0必须全对才给分 1启用部分得分 */
    private String partialScore;

    /** 部分正确时的得分比例(%)，100按命中比例 / 50一律半数 */
    private Integer partialScoreRate;

    /** 防作弊配置JSON */
    private String antiCheatConfig;

    /** 及格证书模板ID，为空表示本场考试不发证书 */
    private Long certId;

    /** not_start / ongoing / finished / archived */
    private String status;

    /** white白名单 / public公开链接 */
    private String participantType;

    /**
     * 考试创建人ID
     *
     * <p>答题服务用它判断「这场考试是我自己建的」：
     * 创建人不用走邀请链接，考试中心直接列出、开考也直接放行。
     */
    private Long creatorId;

}
