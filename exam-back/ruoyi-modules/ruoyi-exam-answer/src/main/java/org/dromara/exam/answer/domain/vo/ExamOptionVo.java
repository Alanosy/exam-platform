package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 答题页的选项
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 选项标识 A/B/C/D */
    private String optionKey;

    /** 选项内容富文本 */
    private String optionContent;

}
