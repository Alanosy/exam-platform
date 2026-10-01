package org.dromara.exam.manage.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 保存考试白名单的业务对象
 *
 * @author LionLi
 * @date 2026-10-02
 */
@Data
public class ExamUserBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 考试ID
     */
    @NotNull(message = "考试ID不能为空")
    private Long examId;

    /**
     * 白名单考生用户ID，整体覆盖：不在这个列表里的原有考生会被移出
     */
    private List<Long> userIds;

}
