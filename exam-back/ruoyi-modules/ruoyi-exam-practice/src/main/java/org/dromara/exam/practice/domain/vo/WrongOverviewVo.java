package org.dromara.exam.practice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 错题本总览统计
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错题总数（不含已忽略与已删除）
     */
    private Long totalCount;

    /**
     * 未掌握数量
     */
    private Long notMasterCount;

    /**
     * 已掌握数量
     */
    private Long masteredCount;

    /**
     * 已忽略数量
     */
    private Long ignoredCount;

    /**
     * 今日新增错题数
     */
    private Long todayCount;

    /**
     * 近7天新增错题数
     */
    private Long weekCount;

    /**
     * 按题型分布，key 为题型编码，value 为数量
     */
    private Map<String, Long> typeDistribution = new LinkedHashMap<>();

    /**
     * 按难度分布，key 为 easy / medium / hard，value 为数量
     */
    private Map<String, Long> difficultyDistribution = new LinkedHashMap<>();

}
