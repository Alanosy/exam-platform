package org.dromara.system.domain.bo;

import org.dromara.system.domain.AiModelConfig;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * AI大模型配置业务对象 ai_model_config
 *
 * @author Alan
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiModelConfig.class, reverseConvertGenerate = false)
public class AiModelConfigBo extends BaseEntity {

    /**
     * 主键ID(雪花ID)
     */
    @NotNull(message = "主键ID(雪花ID)不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 配置名称，如DeepSeek-考试主模型
     */
    @NotBlank(message = "配置名称，如DeepSeek-考试主模型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String configName;

    /**
     * 模型类型：自由输入，如 OPENAI / DEEPSEEK / QWEN / DOUBAO / CUSTOM
     */
    @NotBlank(message = "模型类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelType;

    /**
     * 模型名称：自由输入，如 deepseek-chat
     */
    @NotBlank(message = "模型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelName;

    /**
     * API接口地址
     */
    private String apiBase;

    /**
     * API密钥（加密存储！不要明文）
     */
    private String apiKey;

    /**
     * 温度，0~1
     */
    private Long temperature;

    /**
     * 最大输出token
     */
    private Long maxTokens;

    /**
     * 请求超时时间(ms)
     */
    private Long timeout;

    /**
     * 失败重试次数（单个实例重试）
     */
    private Long retryCount;

    /**
     * 优先级，数字越小优先级越高，故障切换优先选高优先级
     */
    private Long priority;

    /**
     * 权重，同优先级下负载均衡权重
     */
    private Long weight;

    /**
     * 状态：0禁用，1启用
     */
    private String status;

    /**
     * 备注说明
     */
    private String remark;


}
