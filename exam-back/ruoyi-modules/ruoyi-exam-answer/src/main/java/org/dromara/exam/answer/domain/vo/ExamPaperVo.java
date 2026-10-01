package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 答题页数据
 *
 * <p>只包含考生需要看到的内容，标准答案与解析不在下发范围内。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamPaperVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷记录ID */
    private Long recordId;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 任务类型：1正式考试 / 2练习考试 */
    private String examType;

    /** 是否刷题即时判题：做完一题立刻给答案与解析 */
    private Boolean realTimeJudge;

    /** 试卷名称 */
    private String paperName;

    /** 试卷总分 */
    private BigDecimal totalScore;

    /** 题目列表（已按试卷配置决定是否乱序） */
    private List<ExamQuestionVo> questions;

    /** 剩余秒数，-1 表示不限时 */
    private Long remainingSeconds;

    /** 开考时间 */
    private String startTime;

}
