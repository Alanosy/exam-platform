package org.dromara.exam.practice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 错题本列表 / 详情
 *
 * <p>题目正文、选项、解析、参考答案都在题库库里，由练习服务调题库服务补齐后一起返回，
 * 前端拿这一份数据就能同时渲染列表、重做页和解析。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错题记录ID
     */
    private Long id;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 题型 SINGLE单选 / MULTIPLE多选 / JUDGE判断 / BLANK填空 / SHORT_ANSWER简答 等
     */
    private String questionType;

    /**
     * 难度 easy / medium / hard
     */
    private String difficulty;

    /**
     * 题目分值
     */
    private BigDecimal score;

    /**
     * 题干富文本
     */
    private String title;

    /**
     * 选项，客观题才有
     */
    private List<WrongOptionVo> options;

    /**
     * 试题解析富文本
     */
    private String analysis;

    /**
     * 参考答案JSON
     */
    private String standardAnswer;

    /**
     * 参考答案（人话，前端直接展示）
     */
    private String standardAnswerText;

    /**
     * 来源：EXAM正式考试 / PAPER_PRACTICE试卷练习
     */
    private String sourceType;

    /**
     * 来源ID：exam_id 或者 paper_id
     */
    private Long sourceId;

    /**
     * 来源任务类型：1正式考试 / 2练习考试
     *
     * <p>只有来源为 EXAM 时才有值，用来区分「正式考试的错题」与「练习考试的错题」；
     * 试卷练习（PAPER_PRACTICE）来源为 null，前端按 sourceType 兜底展示。
     */
    private String examType;

    /**
     * 来源名称：考试名或试卷名，查不到时由前端兜底显示
     */
    private String sourceName;

    /**
     * 错误次数
     */
    private Integer wrongCount;

    /**
     * 连续答对次数
     */
    private Integer rightCount;

    /**
     * 掌握状态：NOT_MASTER / MASTERED / IGNORED
     */
    private String masterStatus;

    /**
     * 用户笔记
     */
    private String userNote;

    /**
     * 最近一次答错时间
     */
    private Date lastWrongTime;

    /**
     * 最近一次复习时间
     */
    private Date lastReviewTime;

    /**
     * 进入错题本的时间
     */
    private Date createTime;

}
