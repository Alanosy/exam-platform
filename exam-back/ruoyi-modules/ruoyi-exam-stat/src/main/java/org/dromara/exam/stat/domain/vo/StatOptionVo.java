package org.dromara.exam.stat.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.stat.domain.StatExamQuestionOption;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 客观题选项分布
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = StatExamQuestionOption.class)
public class StatOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    private Long questionId;

    private String optionKey;

    private String optionContent;

    private Integer selectCount;

    private BigDecimal selectRate;

    private String isCorrect;

    /** 被错选的人次占比高且不是正确项 → 易错项 */
    private Boolean trap;
}
