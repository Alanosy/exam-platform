package org.dromara.exam.stat.domain.vo;

import lombok.Data;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 大盘概览
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class StatDashboardVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private StatKpiVo kpi;

    /** 全租户范围内的分数段总体分布 */
    private List<StatSegmentVo> scoreDistribution;

    /** 及格率 TOP10 */
    private List<StatExamRowVo> topPassRate;

    /** 考试统计列表（分页） */
    private TableDataInfo<StatExamRowVo> rows;

    @Data
    public static class StatKpiVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 考试场次 */
        private Integer examCount;

        /** 参与人次（已入统） */
        private Integer countedCount;

        /** 平均及格率% */
        private BigDecimal avgPassRate;

        /** 平均分 */
        private BigDecimal avgScore;

        /** 待阅卷份数：全局最该被看见的阻塞项 */
        private Integer pendingMarkCount;

        /** 待阅卷涉及的考试数 */
        private Integer pendingMarkExamCount;

        /** 平均用时（秒） */
        private Integer avgUsedSeconds;
    }
}
