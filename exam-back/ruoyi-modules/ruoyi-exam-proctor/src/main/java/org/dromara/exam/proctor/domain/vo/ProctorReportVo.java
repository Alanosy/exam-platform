package org.dromara.exam.proctor.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 事件上报结果（考生端据此决定要不要提示、要不要强制交卷）
 *
 * @author ruoyi
 */
@Data
public class ProctorReportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long sessionId;

    /** 当前切屏次数 */
    private Integer switchCount;

    /** 当前粘贴次数 */
    private Integer pasteCount;

    /** 当前退出全屏次数 */
    private Integer exitFullscreenCount;

    /** 当前抓拍张数 */
    private Integer cameraCount;

    /** 允许切屏次数（0 不限制） */
    private Integer maxSwitch;

    /** 允许粘贴次数（0 不限制） */
    private Integer maxPaste;

    /** 允许退出全屏次数（0 不限制） */
    private Integer maxExitFullscreen;

    /** 风险等级 */
    private String riskLevel;

    /** 是否已达到强制交卷条件 */
    private Boolean exceed;

    /** 达到条件的原因（前端直接提示给考生） */
    private String exceedReason;

    /** 本次实际入库的事件条数（去重后会小于上报条数） */
    private Integer savedCount;
}
