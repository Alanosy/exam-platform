package org.dromara.system.domain.vo;

import org.dromara.system.domain.AiModelConfig;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * AI大模型配置视图对象 ai_model_config
 *
 * @author Alan
 * @date 2026-10-01
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AiModelConfig.class)
public class AiModelConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID(雪花ID)
     */
    @ExcelProperty(value = "主键ID(雪花ID)")
    private Long id;

    /**
     * 配置名称，如DeepSeek-考试主模型
     */
    @ExcelProperty(value = "配置名称，如DeepSeek-考试主模型")
    private String configName;

    /**
     * 模型类型：OPENAI/DEEPSEEK/QWEN/DOUBAO/CUSTOM
     */
    @ExcelProperty(value = "模型类型：OPENAI/DEEPSEEK/QWEN/DOUBAO/CUSTOM", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "model_type")
    private String modelType;

    /**
     * 模型名称，如deepseek-chat
     */
    @ExcelProperty(value = "模型名称，如deepseek-chat", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "model_name")
    private String modelName;

    /**
     * API接口地址
     */
    @ExcelProperty(value = "API接口地址")
    private String apiBase;

    /**
     * API密钥（加密存储！不要明文）
     */
    @ExcelProperty(value = "API密钥", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "加=密存储！不要明文")
    private String apiKey;

    /**
     * 温度，0~1
     */
    @ExcelProperty(value = "温度，0~1")
    private Long temperature;

    /**
     * 最大输出token
     */
    @ExcelProperty(value = "最大输出token")
    private Long maxTokens;

    /**
     * 请求超时时间(ms)
     */
    @ExcelProperty(value = "请求超时时间(ms)")
    private Long timeout;

    /**
     * 失败重试次数（单个实例重试）
     */
    @ExcelProperty(value = "失败重试次数", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "单=个实例重试")
    private Long retryCount;

    /**
     * 优先级，数字越小优先级越高，故障切换优先选高优先级
     */
    @ExcelProperty(value = "优先级，数字越小优先级越高，故障切换优先选高优先级")
    private Long priority;

    /**
     * 权重，同优先级下负载均衡权重
     */
    @ExcelProperty(value = "权重，同优先级下负载均衡权重")
    private Long weight;

    /**
     * 状态：0禁用，1启用
     */
    @ExcelProperty(value = "状态：0禁用，1启用")
    private String status;

    /**
     * 备注说明
     */
    @ExcelProperty(value = "备注说明")
    private String remark;


}
