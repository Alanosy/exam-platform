package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 考试试题统计 stat_exam_question
 *
 * <p>一场考试 × 一道题 一行。随机组卷时每个考生题目不同，所以这里的
 * answerCount 是「实际作答过这道题的人数」，不是应考人数。
 *
 * <p>correctRate 统计的是「完全答对率」，开了部分得分后还有 partialCount
 * （半对人数）单独记录，两者不能混：把半对算成答错会让正确率偏低。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_exam_question")
public class StatExamQuestion extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 客观题 */
    public static final String CATEGORY_OBJECTIVE = "objective";
    /** 主观题 */
    public static final String CATEGORY_SUBJECTIVE = "subjective";

    private Long id;

    private Long examId;

    private Long questionId;

    private String questionType;

    private String questionCategory;

    private String difficulty;

    private Integer sort;

    private BigDecimal fullScore;

    private Integer answerCount;

    private Integer blankCount;

    /** 完全答对人数 */
    private Integer correctCount;

    private BigDecimal correctRate;

    /** 部分正确人数（部分得分模式下的半对） */
    private Integer partialCount;

    private Integer wrongCount;

    private BigDecimal avgScore;

    private BigDecimal scoreRate;

    private BigDecimal maxScore;

    private BigDecimal minScore;

    private Integer zeroCount;

    private Integer fullCount;

    private BigDecimal difficultyIndex;

    private BigDecimal discrimination;
}
