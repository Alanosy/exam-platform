package org.dromara.exam.mark.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 题目选项（阅卷页展示用）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class MarkOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 选项标识 A/B/C/D */
    private String optionKey;

    /** 选项内容富文本 */
    private String optionContent;
}
