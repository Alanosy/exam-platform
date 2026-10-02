package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 答卷趋势（按天）
 *
 * <p>date 统一用 yyyy-MM-dd 字符串，跨服务序列化不会受时区影响。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class RecordTrendVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日期 yyyy-MM-dd */
    private String date;

    /** 当天交卷数 */
    private Long submitCount = 0L;

    /** 当天及格数 */
    private Long passCount = 0L;

    /** 当天平均分，保留一位小数 */
    private BigDecimal avgScore = BigDecimal.ZERO;

}
