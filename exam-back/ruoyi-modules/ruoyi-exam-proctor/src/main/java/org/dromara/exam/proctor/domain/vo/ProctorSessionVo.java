package org.dromara.exam.proctor.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.proctor.domain.ProctorRule;
import org.dromara.exam.proctor.domain.ProctorSession;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 监考会话（监考中心列表 / 详情）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@AutoMapper(target = ProctorSession.class)
public class ProctorSessionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 答卷记录ID */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 考生姓名 */
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

    /** 多标签页作答次数 */
    private Integer multitabCount;

    /** 允许切屏次数（0 不限制） */
    private Integer maxSwitch;

    /** 允许退出全屏次数（0 不限制） */
    private Integer maxExitFullscreen;

    /** 允许粘贴次数（0 不限制） */
    private Integer maxPaste;

    /** 摄像头抓拍间隔（秒） */
    private Integer cameraInterval;

    /** 是否已触发强制交卷 */
    private Long forceSubmit;

    /** online / offline / submitted / force_submit */
    private String status;

    /** normal / suspect / serious */
    private String riskLevel;

    /** 风险分 */
    private Integer riskScore;

    /** 进入答题时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 最近活跃时间，监考端据此判断掉线 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastActiveTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 在线时长（秒） */
    private Integer durationSeconds;

    /** IP */
    private String ip;

    /** 设备信息 */
    private String device;

    /**
     * 开卷时的防作弊规则快照
     *
     * <p>只在「开启会话」接口返回：前端拿到后才知道要不要开摄像头、多久抓一次、禁不禁复制。
     */
    private ProctorRule rule;
}
