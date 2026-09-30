package cn.org.alan.exam.model.form.like;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/16 22:16
 */
@Data
public class LikeForm {
    @NotNull(message = "讨论id不能为空")
    private Integer discussionId;

    @NotNull(message = "回复id不能为空")
    private Integer replyId;
}
