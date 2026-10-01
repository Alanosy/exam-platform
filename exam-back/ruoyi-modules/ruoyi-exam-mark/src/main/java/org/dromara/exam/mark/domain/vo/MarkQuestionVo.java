package org.dromara.exam.mark.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.mark.domain.MarkItem;
import org.dromara.exam.mark.domain.vo.MarkOptionVo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 阅卷页：一道主观题的完整信息
 *
 * <p>题干 / 参考答案 / 解析来自题库服务，作答与分值来自阅卷明细，
 * 教师在这一屏就能完成打分，不用再来回切页面。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@AutoMapper(target = MarkItem.class)
public class MarkQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 阅卷明细ID */
    private Long itemId;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 题型名称 */
    private String questionTypeName;

    /** 难度 */
    private String difficulty;

    /** 题号顺序 */
    private Integer sort;

    /** 题干富文本 */
    private String title;

    /** 选项，主观题一般为 null */
    private List<MarkOptionVo> options;

    /** 本题满分 */
    private BigDecimal fullScore;

    /** 考生作答原文（JSON） */
    private String answerContent;

    /** 考生作答，人话版 */
    private String answerText;

    /** 参考答案原文 */
    private String standardAnswer;

    /** 参考答案，人话版 */
    private String standardAnswerText;

    /** 试题解析富文本 */
    private String analysis;

    /** 当前得分 */
    private BigDecimal score;

    /** 0未判 1正确 2错误 */
    private Integer correct;

    /** pending待阅 / marked已阅 */
    private String status;

    /** 定稿方式 manual人工 / ai智能预评后确认 */
    private String markType;

    /** 阅卷评语 */
    private String markComment;

    /** AI 建议分 */
    private BigDecimal aiScore;

    /** AI 评分理由 */
    private String aiReason;

    /** none未调用 / running评估中 / success成功 / fail失败 */
    private String aiStatus;
}
