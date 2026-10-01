package org.dromara.exam.proctor.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 一场考试的监考汇总（监考中心列表的一行）
 *
 * <p>这一层只回答「哪场考试需要盯」，考生明细点进来再看。
 *
 * @author ruoyi
 */
@Data
public class ProctorExamGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 参加人数（会话数） */
    private Long totalCount;

    /** 正在作答（心跳正常） */
    private Long onlineCount;

    /** 掉线人数 */
    private Long offlineCount;

    /** 已交卷人数（含强制交卷） */
    private Long submittedCount;

    /** 可疑人数 */
    private Long suspectCount;

    /** 严重人数 */
    private Long seriousCount;

    /** 强制交卷人数 */
    private Long forceSubmitCount;

    /** 切屏总次数 */
    private Long switchTotal;

    /** 粘贴总次数 */
    private Long pasteTotal;

    /** 抓拍总张数 */
    private Long cameraTotal;

    /** 本场最高风险分，用于排序与标红 */
    private Integer maxRiskScore;

    /** 本场最高风险等级：normal / suspect / serious */
    private String riskLevel;

    /** 最近一次活跃时间 */
    private Date lastActiveTime;
}
