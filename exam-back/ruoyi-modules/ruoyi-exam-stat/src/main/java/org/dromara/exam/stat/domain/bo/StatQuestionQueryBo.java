package org.dromara.exam.stat.domain.bo;

import lombok.Data;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试题分析筛选条件
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class StatQuestionQueryBo extends PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long examId;

    /** 题型 SINGLE / MULTIPLE ... */
    private String questionType;

    /** 难度 easy / medium / hard */
    private String difficulty;

    /** objective / subjective */
    private String questionCategory;

    /** 只看异常题：正确率低于 60% 或区分度低于 0.2 */
    private Boolean onlyAbnormal;

    /** 题干关键词 */
    private String keyword;
}
