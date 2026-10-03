package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模型配置（脱敏，不含密钥）
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiModelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 模型编码 */
    private String code;

    /** 模型名称 */
    private String name;

    /** 供应商 / 类型 */
    private String type;

    /** 配置来源 mysql / env / mock */
    private String source;

    /** 优先级，越小越优先 */
    private Integer priority;

    /** 熔断状态 HEALTHY / DEGRADED / FUSE_OPEN / HALF_OPEN */
    private String state;

    /** 是否当前主用 */
    private Boolean primary;
}
