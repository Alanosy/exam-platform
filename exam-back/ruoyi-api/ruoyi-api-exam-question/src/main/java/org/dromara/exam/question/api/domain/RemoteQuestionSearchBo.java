package org.dromara.exam.question.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试题检索条件（跨服务传输用）
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionSearchBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 关键词，匹配题干 */
    private String keyword;

    /** 题库ID，为空表示不限 */
    private Long bankId;

    /** 题型 code，为空表示不限 */
    private String questionType;

    /** 难度 easy / medium / hard，为空表示不限 */
    private String difficulty;

    /** 返回条数上限，默认 20 */
    private Integer limit = 20;
}
