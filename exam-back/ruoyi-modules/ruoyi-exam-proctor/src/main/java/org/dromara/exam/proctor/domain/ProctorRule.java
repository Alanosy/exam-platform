package org.dromara.exam.proctor.domain;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
//import org.dromara.common.core.utils.JsonUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 防作弊规则（由 exam.anti_cheat_config 的 JSON 解析而来）
 *
 * <p>考试侧只存一份 JSON，这里负责把它翻译成监考端真正要执行的开关与阈值。
 * 老考试的 JSON 里没有后加的字段，所以每个字段都要给安全默认值：
 * 默认一律「不强制」，只记录不拦人，避免存量考试被新规则误伤。
 *
 * @author ruoyi
 */
@Data
public class ProctorRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 默认抓拍间隔（秒） */
    public static final int DEFAULT_CAMERA_INTERVAL = 60;

    /** 允许切屏次数，0 不限制，超过则强制交卷 */
    private Integer switchScreen;

    /** 禁止复制粘贴 0否 1是 */
    private Integer copyPaste;

    /** 摄像头抓拍 0否 1是 */
    private Integer camera;

    /** 强制全屏 0否 1是 */
    private Integer fullScreen;

    /** 摄像头抓拍间隔（秒） */
    private Integer cameraInterval;

    /** 允许粘贴次数，0 不限制 */
    private Integer maxPaste;

    /** 允许退出全屏次数，0 不限制 */
    private Integer maxExitFullscreen;

    /** 开发者工具检测 0关 1开（只记录并告警，不强制交卷） */
    private Integer devtool;

    /** 多标签页 / 多端检测 0关 1开 */
    private Integer multitab;

    /**
     * 解析考试里的防作弊配置 JSON
     *
     * @param json exam.anti_cheat_config，可能为空或字段不全
     * @return 规则，字段缺失时按「不限制」兜底
     */
    public static ProctorRule parse(String json) {
        ProctorRule rule = new ProctorRule();
        if (StringUtils.isNotBlank(json)) {
            try {
                ProctorRule parsed = JsonUtils.parseObject(json, ProctorRule.class);
                if (ObjectUtil.isNotNull(parsed)) {
                    rule = parsed;
                }
            } catch (Exception e) {
                // 配置被人手改坏了也不能让考生进不去考场，按不限制处理
            }
        }
        rule.setSwitchScreen(nvl(rule.getSwitchScreen(), 0));
        rule.setCopyPaste(nvl(rule.getCopyPaste(), 0));
        rule.setCamera(nvl(rule.getCamera(), 0));
        rule.setFullScreen(nvl(rule.getFullScreen(), 0));
        rule.setCameraInterval(nvl(rule.getCameraInterval(), DEFAULT_CAMERA_INTERVAL));
        rule.setMaxPaste(nvl(rule.getMaxPaste(), 0));
        rule.setMaxExitFullscreen(nvl(rule.getMaxExitFullscreen(), 0));
        rule.setDevtool(nvl(rule.getDevtool(), 1));
        rule.setMultitab(nvl(rule.getMultitab(), 1));
        return rule;
    }

    private static Integer nvl(Integer value, Integer defaultValue) {
        return ObjectUtil.isNull(value) || value < 0 ? defaultValue : value;
    }

    /** 是否开启摄像头抓拍 */
    public boolean cameraEnabled() {
        return Integer.valueOf(1).equals(getCamera());
    }

    /** 是否强制全屏 */
    public boolean fullScreenEnabled() {
        return Integer.valueOf(1).equals(getFullScreen());
    }

    /** 是否禁止复制粘贴 */
    public boolean copyPasteDisabled() {
        return Integer.valueOf(1).equals(getCopyPaste());
    }
}
