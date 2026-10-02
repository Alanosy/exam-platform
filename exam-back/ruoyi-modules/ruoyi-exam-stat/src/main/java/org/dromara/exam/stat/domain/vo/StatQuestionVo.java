package org.dromara.exam.stat.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.stat.domain.StatExamQuestion;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 试题分析行
 *
 * <p>客观题看 correctRate（完全答对率），主观题看 scoreRate（得分率），
 * 前端按 questionCategory 决定显示哪一列。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = StatExamQuestion.class)
public class StatQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    private Long questionId;

    /** 题号 */
    private Integer sort;

    private String questionType;

    /** objective客观 / subjective主观 */
    private String questionCategory;

    private String difficulty;

    /** 题干（去 HTML 后的纯文本，列表用） */
    private String title;

    private BigDecimal fullScore;

    private Integer answerCount;

    private Integer blankCount;

    private Integer correctCount;

    private BigDecimal correctRate;

    /** 部分正确人数：开了部分得分才有，别把它算进「答错」 */
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

    /** 客观题的选项分布 */
    private List<StatOptionVo> options;

    /** 规则化诊断建议，如「过于简单，建议替换」 */
    private String suggestion;
}
