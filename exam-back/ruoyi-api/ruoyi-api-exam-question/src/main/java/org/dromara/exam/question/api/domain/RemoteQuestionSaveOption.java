package org.dromara.exam.question.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 待保存试题的选项（跨服务传输用）
 *
 * <p>字段名用 key / content 而不是 optionKey / optionContent，
 * 是为了和 AI 生成结果的结构对齐，Agent 侧不用做映射。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionSaveOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 选项标识 A/B/C/D */
    private String key;

    /** 选项内容 */
    private String content;
}
