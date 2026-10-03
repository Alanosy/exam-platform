package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用 Skill 执行结果
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteSkillRunVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否执行成功 */
    private Boolean success;

    /** Skill 编码 */
    private String skillCode;

    /** 结果数据（对象或数组，按需序列化） */
    private Object data;

    /** 实际使用的模型 */
    private String model;

    /** 本次调用走过的模型链（主备切换审计） */
    private List<String> attemptChain;

    /** 耗时（毫秒） */
    private Long latencyMs;

    /** 失败原因 */
    private String message;

    public static RemoteSkillRunVo fail(String message) {
        RemoteSkillRunVo vo = new RemoteSkillRunVo();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}
