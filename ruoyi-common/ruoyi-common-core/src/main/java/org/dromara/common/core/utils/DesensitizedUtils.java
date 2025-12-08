package org.dromara.common.core.utils;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 脱敏工具类
 *
 * @author AprilWind
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DesensitizedUtils extends DesensitizedUtil {

    /**
     * 灵活脱敏方法
     *
     * @param value         原始字符串
     * @param prefixVisible 前面可见长度
     * @param suffixVisible 后面可见长度
     * @param maskLength    中间掩码长度（固定显示多少 *，如果总长度不足则自动缩减）
     * @return 脱敏后字符串
     */
    public static String mask(String value, int prefixVisible, int suffixVisible, int maskLength) {
        if (StrUtil.isBlank(value)) {
            return value;
        }

        int len = value.length();

        // 总长度小于等于前后可见长度 → 全部掩码
        if (len <= prefixVisible + suffixVisible) {
            return StrUtil.repeat('*', len);
        }

        // 可用长度 = 总长度 - 前后可见长度
        int available = len - prefixVisible - suffixVisible;

        // 中间掩码长度不能超过可用长度
        int actualMaskLength = Math.min(maskLength, available);

        // 剩余字符尽量显示在中间掩码旁
        int remaining = available - actualMaskLength;
        String middleChars = remaining > 0 ? value.substring(prefixVisible, prefixVisible + remaining) : "";
        String middleMask = StrUtil.repeat('*', actualMaskLength);

        String prefix = value.substring(0, prefixVisible);
        String suffix = value.substring(len - suffixVisible);

        return prefix + middleChars + middleMask + suffix;
    }

}
