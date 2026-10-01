package org.dromara.exam.proctor.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 某场考试的监考概览（监考中心顶部统计）
 *
 * @author ruoyi
 */
@Data
public class ProctorOverviewVo implements Serializable {

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

    /** 已交卷人数 */
    private Long submittedCount;

    /** 可疑人数 */
    private Long suspectCount;

    /** 严重人数 */
    private Long seriousCount;

    /** 切屏总次数 */
    private Long switchTotal;

    /** 粘贴总次数 */
    private Long pasteTotal;

    /** 抓拍总张数 */
    private Long cameraTotal;

    /** 强制交卷人数 */
    private Long forceSubmitCount;
}
