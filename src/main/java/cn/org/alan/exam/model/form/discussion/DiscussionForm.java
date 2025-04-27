package cn.org.alan.exam.model.form.discussion;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 讨论入参
 *
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/3 9:32
 */
@Data
public class DiscussionForm {
    @NotNull(message = "接收班级id不能为空")
    private Integer gradeId;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;

}
