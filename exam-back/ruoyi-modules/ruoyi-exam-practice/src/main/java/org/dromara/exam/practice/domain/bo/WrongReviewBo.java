package org.dromara.exam.practice.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 错题重做提交
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongReviewBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 考生作答JSON，与正式考试作答结构一致：
     * 客观题 {"choices":["A"]} / 填空 {"blanks":[{"text":".."}]} / 主观题 {"text":".."}
     */
    @NotBlank(message = "作答内容不能为空", groups = { AddGroup.class })
    private String answerContent;

}
