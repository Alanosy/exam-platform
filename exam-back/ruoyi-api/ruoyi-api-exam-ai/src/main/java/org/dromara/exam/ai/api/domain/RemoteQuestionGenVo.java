package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI 生成的题目
 *
 * <p>answer 是**字符串形式的 JSON**，结构与题库 question.answer 完全一致，
 * 落库前不要二次加工，交给题库服务的保存接口原样写入：
 * <pre>
 *   单选/多选 {"rightKeys":["A","C"]}
 *   判断     {"rightKeys":["A"]}      A=正确 B=错误
 *   填空     {"blanks":[{"answers":["北京","北平"]}]}
 *   简答论述 {"answer":"参考答案要点"}
 *   代码     {"language":"java","answer":"参考实现"}
 * </pre>
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionGenVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 题型 */
    private String questionType;

    /** 题干 */
    private String stem;

    /** 选项，客观题才有 */
    private List<RemoteAiOptionVo> options;

    /** 答案（字符串形式的 JSON） */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 知识点 */
    private List<String> knowledgePoints;

    /** 难度 */
    private String difficulty;

    /** 建议分值 */
    private BigDecimal score;

    /** 质检结论，withAudit=true 时才有 */
    private RemoteQuestionAuditVo audit;
}
