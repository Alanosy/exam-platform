package org.dromara.exam.ai.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 中断卡上的一个字段
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiChatFieldVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字段键，回传时作为 answers 的 key */
    private String key;

    /** 显示名 */
    private String label;

    /** text / number / select / multi / switch */
    private String type;

    /** 选项，select / multi 时用 */
    private List<AiChatOptionVo> options;

    /** 默认值 */
    private Object value;

    /** 是否必填 */
    private Boolean required;

    /** 输入提示 */
    private String placeholder;

    /** 补充说明（如「改了会重新生成」） */
    private String tip;
}
