package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 阅卷结果回写（阅卷服务 → 答题服务）
 *
 * <p>阅卷服务只负责判分，成绩归属仍在答题库：
 * 这里把每道主观题的得分回写到 exam_answer，并重算 exam_record 的主观题分 / 总分 / 及格。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class RemoteMarkWriteBackBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷记录ID */
    private Long recordId;

    /** 主观题总分，由阅卷服务算好后直接落库，避免两边口径不一致 */
    private BigDecimal subjectiveScore;

    /** 每道主观题的得分 */
    private List<RemoteMarkScoreBo> scores;

    /** 是否阅卷完成，完成后才重算总分与及格 */
    private Boolean finished;
}
