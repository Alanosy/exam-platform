package org.dromara.exam.practice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 错题题目选项
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 选项键 A/B/C/D
     */
    private String optionKey;

    /**
     * 选项内容
     */
    private String optionContent;

}
