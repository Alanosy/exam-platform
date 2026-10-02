package org.dromara.exam.stat.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 考生答卷明细：得分卡 + 逐题作答
 *
 * <p>result 的四种取值是前端要分色显示的：正确 / 部分正确 / 错误 / 未答；
 * 主观题没阅完时 result 为「待阅」，分数还没定。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class StatAnswerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long examId;

    private Long userId;

    private Integer attemptNo;

    private String account;

    private String userName;

    private String deptName;

    private BigDecimal totalScore;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    private BigDecimal passScore;

    private BigDecimal fullScore;

    private Integer passed;

    private Integer rankNo;

    private Integer countedCount;

    /** 超过百分之多少的考生 */
    private BigDecimal beatRate;

    private Integer usedSeconds;

    /** 本场平均用时，用来比较快/慢 */
    private Integer avgUsedSeconds;

    /** COUNTED / PENDING_MARK / EXCLUDED */
    private String statStatus;

    /** 历次参考，供前端切换「第几次」 */
    private List<AttemptVo> attempts;

    private List<StatAnswerItemVo> items;

    @Data
    public static class AttemptVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long recordId;
        private Integer attemptNo;
        private BigDecimal totalScore;
        /** COUNTED / PENDING_MARK / EXCLUDED */
        private String statStatus;
    }

    @Data
    public static class StatAnswerItemVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long questionId;

        private Integer sort;

        private String questionType;

        private String questionCategory;

        private String difficulty;

        private String title;

        /** 选项，客观题才有 */
        private List<StatOptionVo> options;

        /** 考生作答（客观题是选项，主观题是文本） */
        private String answerContent;

        /** 可展示的作答文本 */
        private String answerText;

        /** 标准答案，受 exam.show_answer_mode 控制，none 时不返回 */
        private String standardAnswerText;

        private BigDecimal fullScore;

        private BigDecimal score;

        /** correct：1正确 2错误 3部分正确 0未判 */
        private Integer correct;

        /** 正确 / 部分正确 / 错误 / 未答 / 待阅 */
        private String result;

        /** manual人工 / ai智能 / auto自动 */
        private String markType;

        private String markerName;

        private String markComment;

        private String analysis;
    }
}
