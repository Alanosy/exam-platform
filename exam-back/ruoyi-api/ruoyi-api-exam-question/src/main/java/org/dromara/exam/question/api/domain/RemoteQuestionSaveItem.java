package org.dromara.exam.question.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 待保存的试题（跨服务传输用）
 *
 * <p>answer 是**字符串形式的 JSON**，结构与题型绑定（如 {@code {"rightKeys":["A"]}}），
 * 不在这里拆开：判分逻辑在答题服务，中间层只负责原样搬运。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionSaveItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 题型 SINGLE / MULTIPLE / JUDGE / BLANK / SHORT_ANSWER / ESSAY / CODE / UPLOAD_FILE / MATCH */
    private String questionType;

    /** 题干 */
    private String stem;

    /** 解析 */
    private String analysis;

    /** 参考答案 JSON 字符串 */
    private String answer;

    /** 难度 easy / medium / hard */
    private String difficulty;

    /** 默认分值 */
    private BigDecimal score;

    /** 选项，客观题才有 */
    private List<RemoteQuestionSaveOption> options;

    /** 知识点名称列表，能匹配到已有点时自动关联，匹配不到则忽略 */
    private List<String> knowledgePoints;
}
