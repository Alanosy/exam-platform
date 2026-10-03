package org.dromara.exam.ai.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 中断卡上的候选项
 *
 * <p>value 一律是字符串：题库ID / 考试ID 都是雪花 ID，
 * 走数字在前端会丢精度（19 位超过 JS 安全整数）。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class AiChatOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 显示文案 */
    private String label;

    /** 取值 */
    private String value;
}
