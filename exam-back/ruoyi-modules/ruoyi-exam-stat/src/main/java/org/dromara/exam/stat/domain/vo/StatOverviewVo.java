package org.dromara.exam.stat.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 单场考试详情：概览 + 分数段 + 阅卷进度
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class StatOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试统计汇总（含考试状态、时间） */
    private StatExamRowVo summary;

    /** 分数段分布 */
    private List<StatSegmentVo> segments;

    /** 待阅卷的答卷，>0 时前端弹黄色提示条 */
    private List<StatMarkProgressVo> pendingMarks;

    /** 本场考试的开部分得分配置，前端提示统计口径 */
    private String partialScore;

    private Integer partialScoreRate;

    @Data
    public static class StatMarkProgressVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long recordId;

        private String account;

        private String userName;

        private Integer questionCount;

        private Integer markedCount;

        private String status;

        private String markerName;
    }
}
