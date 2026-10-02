package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 客观题选项分布 stat_exam_question_option
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_exam_question_option")
public class StatExamQuestionOption extends TenantEntity {

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
}
