package org.dromara.exam.question.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按ID批量查询试题的入参（组卷回显使用）
 *
 * <p>用 POST + JSON 数组直接提交，ids 可能很长，放在 query string 里容易被截断。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
public class QuestionIdsBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "试题ID不能为空")
    private List<Long> ids;

}
