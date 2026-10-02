package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 答卷统计（跨服务传输）
 *
 * <p>统计服务不连答题库，只能隔着 Dubbo 要数据，所以把「算」的动作留在拥有
 * exam_record 表的答题服务里，这里只装结果。
 *
 * <p>所有计数都已是 Long，比例 / 分数用 BigDecimal 且已经量化到一位小数，
 * 调用方（首页仪表盘）拿到即可直接展示，不必再自己除一遍。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class RecordStatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷总数，含答题中 */
    private Long total = 0L;

    /** 已交卷数 */
    private Long submitted = 0L;

    /** 正在答题数 */
    private Long answering = 0L;

    /** 今日交卷数 */
    private Long todaySubmit = 0L;

    /** 及格答卷数 */
    private Long passed = 0L;

    /** 参考人数（去重后的考生数） */
    private Long examineeCount = 0L;

    /** 及格率，百分数，0 ~ 100，保留一位小数 */
    private BigDecimal passRate = BigDecimal.ZERO;

    /** 平均分，保留一位小数 */
    private BigDecimal avgScore = BigDecimal.ZERO;

}
