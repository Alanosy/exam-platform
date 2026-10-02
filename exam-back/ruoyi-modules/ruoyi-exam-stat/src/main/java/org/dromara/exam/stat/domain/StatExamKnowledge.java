package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 考试知识点统计 stat_exam_knowledge
 *
 * <p>一期知识点复用题库分类树（question.bank_id → question_bank.category_id），
 * 一个题目只归一个分类。二期换成独立知识点表时只改取值来源，统计逻辑不动。
 *
 * <p>掌握度按「失分加权」算，不是各题得分率的简单平均——
 * 否则 2 分的选择题会和 20 分的论述题有一样的话语权。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_exam_knowledge")
public class StatExamKnowledge extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 掌握良好 */
    public static final String WEAK_GOOD = "good";
    /** 一般 */
    public static final String WEAK_NORMAL = "normal";
    /** 薄弱 */
    public static final String WEAK_WEAK = "weak";

    private Long id;

    private Long examId;

    private Long knowledgeId;

    private String knowledgeName;

    private String knowledgePath;

    private Integer questionCount;

    private BigDecimal fullScore;

    private BigDecimal avgScore;

    private BigDecimal scoreRate;

    private Integer wrongCount;

    private BigDecimal wrongRate;

    private BigDecimal mastery;

    private String weakLevel;
}
