package org.dromara.system.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AI大模型配置对象 ai_model_config
 *
 * @author Alan
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model_config")
public class AiModelConfig extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID(雪花ID)
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 配置名称，如DeepSeek-考试主模型
     */
    private String configName;

    /**
     * 模型类型：OPENAI/DEEPSEEK/QWEN/DOUBAO/CUSTOM
     */
    private String modelType;

    /**
     * 模型名称，如deepseek-chat
     */
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

    /**
     * 删除标记 0正常 1删除
     */
    @TableLogic
    private String delFlag;


}
