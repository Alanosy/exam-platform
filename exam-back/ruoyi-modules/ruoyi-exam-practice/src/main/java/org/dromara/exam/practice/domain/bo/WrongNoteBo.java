package org.dromara.exam.practice.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 错题笔记
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongNoteBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户笔记，富文本，可写错题心得
     */
    @Size(max = 5000, message = "笔记长度不能超过5000个字符")
    private String userNote;

}
