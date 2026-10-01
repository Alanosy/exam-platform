package org.dromara.exam.mark.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 主观题打分入参
 *
 * <p>得分上下限由后端按本题满分校验（0 ~ 满分），不在注解里写死，
 * 免得不同分值的题共用一个上限。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class MarkScoreBo {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 阅卷明细ID */
    @NotNull(message = "阅卷明细ID不能为空", groups = { AddGroup.class })
    private Long itemId;

    /** 本题得分 */
    @NotNull(message = "得分不能为空", groups = { AddGroup.class })
    private BigDecimal score;

    /** 是否正确：不传时按「得分是否等于满分」自动判定 */
    private Integer correct;

    /** 阅卷评语 */
    private String markComment;

    /** 是否采纳 AI 建议分，true 时得分取 AI 建议分 */
    private Boolean useAiScore;
}
