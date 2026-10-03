package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 通用 Skill 执行入参
 *
 * <p>给「新能力」留的口子：Python 侧加了新 Skill 后，Java 侧不用改接口就能调。
 * 业务代码优先用 RemoteAiService 里的语义化方法，这里只用于临时调用和运维自测。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteSkillRunBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Skill 编码，见 Python 侧 prompts/*.yaml 的 code */
    private String skillCode;

    /** 入参，键与提示词模板里的占位符同名 */
    private Map<String, Object> input;

    /** 强制指定模型（调试用，一般留空走主备链） */
    private String modelCode;

    /** 租户ID */
    private String tenantId;
}
