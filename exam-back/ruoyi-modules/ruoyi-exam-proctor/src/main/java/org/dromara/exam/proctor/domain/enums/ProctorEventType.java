package org.dromara.exam.proctor.domain.enums;

import lombok.Getter;
import org.dromara.common.core.utils.StringUtils;

import java.util.Arrays;

/**
 * 防作弊事件类型
 *
 * <p>column 是该事件在 exam_proctor_session 上要累加的计数字段（为空表示只记流水不计数）；
 * level 决定监考端列表里的告警颜色。
 *
 * @author ruoyi
 */
@Getter
public enum ProctorEventType {

    /** 切屏 / 离屏：页面不可见或窗口失焦，同一动因去重后算一次 */
    SWITCH_SCREEN("switch_screen", "切屏/离屏", "warn", "switch_count"),

    /** 窗口失焦：只记录不单独算切屏 */
    BLUR("blur", "窗口失焦", "info", "blur_count"),

    /** 复制 */
    COPY("copy", "复制", "warn", "copy_count"),

    /** 粘贴：content 里带被粘贴内容的前 200 字 */
    PASTE("paste", "粘贴", "warn", "paste_count"),

    /** 剪切 */
    CUT("cut", "剪切", "warn", "cut_count"),

    /** 右键菜单：禁复制时它是「想绕过」的信号 */
    CONTEXTMENU("contextmenu", "右键菜单", "info", "contextmenu_count"),

    /** 退出全屏 */
    EXIT_FULLSCREEN("exit_fullscreen", "退出全屏", "warn", "exit_fullscreen_count"),

    /** 进入全屏：只记流水，配合退出全屏能看出「进去了多久又退出来」 */
    ENTER_FULLSCREEN("enter_fullscreen", "进入全屏", "info", null),

    /** 摄像头抓拍：定时或触发式，图片另存 exam_proctor_snapshot */
    CAMERA("camera", "摄像头抓拍", "info", "camera_count"),

    /** 摄像头被拒绝 / 设备不可用 */
    CAMERA_DENY("camera_deny", "摄像头不可用", "warn", null),

    /** 疑似打开开发者工具（F12 / 快捷键 / 窗口尺寸突变） */
    DEVTOOL("devtool", "打开开发者工具", "danger", "devtool_count"),

    /** 同账号多标签页 / 多端同时作答 */
    MULTITAB("multitab", "多标签页作答", "danger", "multitab_count"),

    /** 违规达到上限，系统强制交卷 */
    FORCE_SUBMIT("force_submit", "违规强制交卷", "danger", null),

    /** 进入答题页 */
    ENTER("enter", "进入答题", "info", null),

    /** 中途退出后重新进入（续答） */
    RESUME("resume", "重新进入答题", "info", null),

    /** 心跳：只更新 last_active_time，不落流水 */
    HEARTBEAT("heartbeat", "心跳", "info", null);

    private final String code;
    private final String name;
    private final String level;
    /** 对应 exam_proctor_session 上要 +1 的列，为空表示不计数 */
    private final String column;

    ProctorEventType(String code, String name, String level, String column) {
        this.code = code;
        this.name = name;
        this.level = level;
        this.column = column;
    }

    /**
     * 按编码取枚举，识别不了返回 null（前端传了不认识的类型时只记流水，不至于报错）
     */
    public static ProctorEventType of(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }
}
