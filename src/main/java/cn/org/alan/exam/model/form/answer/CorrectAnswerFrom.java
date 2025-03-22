package cn.org.alan.exam.model.form.answer;

import cn.org.alan.exam.common.group.AnswerGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/15 1:37 PM
 */
@Data
public class CorrectAnswerFrom {
    // 被批改人Id
    @NotBlank(message = "被批改人Id不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer userId;
    // 试卷ID
    @NotBlank(message = "试卷Id不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer examId;
    // 试题ID
    @NotBlank(message = "试题Id不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer questionId;
    // 分数
    @NotBlank(message = "分数不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer score;
}
