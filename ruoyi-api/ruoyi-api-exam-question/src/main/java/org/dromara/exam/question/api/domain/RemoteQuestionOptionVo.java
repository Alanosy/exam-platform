package org.dromara.exam.question.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试题选项（跨服务传输用）
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class RemoteQuestionOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 选项标识 A/B/C/D */
    private String optionKey;

    /** 选项内容富文本 */
    private String optionContent;

    /** 排序号 */
    private Long sort;

}
