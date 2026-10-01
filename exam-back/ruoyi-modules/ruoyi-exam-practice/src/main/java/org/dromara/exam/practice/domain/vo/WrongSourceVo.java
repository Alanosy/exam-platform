package org.dromara.exam.practice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 错题来源分组（每场考试 / 每份练习的错题概览）
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongSourceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 来源：EXAM正式考试 / PAPER_PRACTICE试卷练习
     */
    private String sourceType;

    /**
     * 来源ID：exam_id 或者 paper_id
     */
    private Long sourceId;

    /**
     * 来源任务类型：1正式考试 / 2练习考试，非考试来源为 null
     */
    private String examType;

    /**
     * 来源名称：考试名或试卷名
     */
    private String sourceName;

    /**
     * 该来源下的错题数
     */
    private Long wrongCount;

    /**
     * 其中未掌握的题数
     */
    private Long notMasterCount;

    /**
     * 其中已掌握的题数
     */
    private Long masteredCount;

    /**
     * 该来源最近一次答错时间
     */
    private Date lastWrongTime;

}
