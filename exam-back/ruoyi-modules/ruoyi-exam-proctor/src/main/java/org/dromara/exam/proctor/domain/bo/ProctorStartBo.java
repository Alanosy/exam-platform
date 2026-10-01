package org.dromara.exam.proctor.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 开启监考会话参数（考生端进入答题页时调用）
 *
 * @author ruoyi
 */
@Data
public class ProctorStartBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    @NotNull(message = "考试ID不能为空")
    private Long examId;

    /** 答卷记录ID */
    @NotNull(message = "答卷ID不能为空")
    private Long recordId;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 设备信息（屏幕 / 系统 / 浏览器，前端拼接，便于事后核对是不是换了设备） */
    private String device;
}
