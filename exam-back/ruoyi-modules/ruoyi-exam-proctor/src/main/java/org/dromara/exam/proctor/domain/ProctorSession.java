package org.dromara.exam.proctor.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 监考会话对象 exam_proctor_session
 *
 * <p>一场考试 × 一份答卷 一条记录：进答题页时创建，交卷 / 强制交卷时结束。
 * 各类违规动作只在这里累加计数，明细流水看 exam_proctor_event。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_proctor_session")
public class ProctorSession extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 作答中：心跳正常 */
    public static final String STATUS_ONLINE = "online";

    /** 掉线：超过心跳时间没动静 */
    public static final String STATUS_OFFLINE = "offline";

    /** 已交卷 */
    public static final String STATUS_SUBMITTED = "submitted";

    /** 违规超限被强制交卷 */
    public static final String STATUS_FORCE_SUBMIT = "force_submit";

    /** 风险等级：正常 */
    public static final String RISK_NORMAL = "normal";

    /** 风险等级：可疑 */
    public static final String RISK_SUSPECT = "suspect";

    /** 风险等级：严重 */
    public static final String RISK_SERIOUS = "serious";

    /** 主键ID */
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 考试名称（快照，列表不用回查考试） */
    private String examName;

    /** 答卷记录ID（答题库 exam_record 主键，跨库只存ID） */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 考生姓名快照 */
    private String nickName;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 切屏 / 离屏次数 */
    private Integer switchCount;

    /** 窗口失焦次数 */
    private Integer blurCount;

    /** 复制次数 */
    private Integer copyCount;

    /** 粘贴次数 */
    private Integer pasteCount;

    /** 剪切次数 */
    private Integer cutCount;

    /** 右键菜单次数 */
    private Integer contextmenuCount;

    /** 退出全屏次数 */
    private Integer exitFullscreenCount;

    /** 摄像头抓拍张数 */
    private Integer cameraCount;

    /** 疑似打开开发者工具次数 */
    private Integer devtoolCount;

    /** 多标签页 / 多端同时作答次数 */
    private Integer multitabCount;

    /** 允许切屏次数（开卷快照），0 不限制 */
    private Integer maxSwitch;

    /** 允许退出全屏次数（开卷快照），0 不限制 */
    private Integer maxExitFullscreen;

    /** 允许粘贴次数（开卷快照），0 不限制 */
    private Integer maxPaste;

    /** 摄像头抓拍间隔（秒，开卷快照） */
    private Integer cameraInterval;

    /** 是否已触发强制交卷 0否 1是 */
    private Long forceSubmit;

    /** online作答中 / offline掉线 / submitted已交卷 / force_submit强制交卷 */
    private String status;

    /** normal正常 / suspect可疑 / serious严重 */
    private String riskLevel;

    /** 风险分 */
    private Integer riskScore;

    /** 进入答题页时间 */
    private Date startTime;

    /** 最近一次心跳 / 事件时间 */
    private Date lastActiveTime;

    /** 交卷 / 离开时间 */
    private Date endTime;

    /** 在线时长（秒） */
    private Integer durationSeconds;

    /** IP */
    private String ip;

    /** 浏览器 UA */
    private String userAgent;

    /** 设备信息 */
    private String device;

    /** 逻辑删除 0未删 1已删 */
    private Long delFlag;
}
