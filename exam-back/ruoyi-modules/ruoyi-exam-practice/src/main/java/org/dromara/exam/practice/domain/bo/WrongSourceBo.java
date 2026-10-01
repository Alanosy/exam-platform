package org.dromara.exam.practice.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 错题来源（一场考试 / 一份练习）查询条件
 *
 * <p>错题本首页先按「来源」分页展示，点进去才看具体错题，所以这里放的是来源维度的筛选条件。
 * 注意 examType 不在错题表里，是拿 sourceId 反查考试拿到的，
 * 只能先聚合完再过滤，分页条数仍然准确（只是把过滤放在内存里做）。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class WrongSourceBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 来源：EXAM正式考试 / PAPER_PRACTICE试卷练习
     */
    private String sourceType;

    /**
     * 任务类型：1正式考试 / 2练习考试，试卷练习来源没有这个维度
     */
    private String examType;

    /**
     * 来源名称关键字，模糊匹配考试名 / 试卷名
     */
    private String sourceName;

}
