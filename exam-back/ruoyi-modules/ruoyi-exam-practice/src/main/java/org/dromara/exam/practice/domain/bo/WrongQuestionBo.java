package org.dromara.exam.practice.domain.bo;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 错题本查询条件
 *
 * <p>只放错题表本身有的字段，题型 / 难度这些在题库侧，
 * 跨服务过滤会把分页打乱，暂不支持按题型筛选。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WrongQuestionBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID，精确查某一道题的错题记录
     */
    private Long questionId;

    /**
     * 来源：EXAM正式考试 / PAPER_PRACTICE试卷练习
     */
    private String sourceType;

    /**
     * 来源ID：exam_id 或者 paper_id，用于「只看某场考试的错题」
     */
    private Long sourceId;

    /**
     * 掌握状态：NOT_MASTER / MASTERED / IGNORED
     */
    private String masterStatus;

    /**
     * 是否包含已忽略的错题，默认只看未掌握 + 已掌握
     */
    private Boolean includeIgnored;

}
