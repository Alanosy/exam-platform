package org.dromara.exam.question.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 题库（跨服务传输用）
 *
 * <p>AI 对话场景要用它做「题库模糊匹配」：用户说「写到计算机题库」时，
 * Agent 拿关键词查一次，唯一命中就直接用，多个命中才让人选。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionBankVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 题库ID */
    private Long id;

    /** 题库名称 */
    private String name;

    /** 题库下已有的题目数量（匹配展示用，让用户知道这个库里有多少题） */
    private Long questionCount;

    /** 所属分类名称，没有分类时为空 */
    private String categoryName;
}
