package org.dromara.exam.mark.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 阅卷列表（按考试聚合）
 *
 * <p>一场考试一行：这场考试有多少份答卷要阅、阅了多少题、进度多少。
 * 只统计正式考试——练习刷题是即时判题，不进人工阅卷。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class MarkExamVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 试卷ID */
    private Long paperId;

    /** 试卷名称 */
    private String paperName;

    /** 待阅答卷数（没阅完的） */
    private Long pendingTaskCount;

    /** 已阅完答卷数 */
    private Long finishedTaskCount;

    /** 答卷总数 */
    private Long taskCount;

    /** 待阅题目数 */
    private Long pendingItemCount;

    /** 已阅题目数 */
    private Long markedItemCount;

    /** 主观题总题数 */
    private Long itemCount;

    /** 阅卷进度百分比，0 ~ 100 */
    private Integer progress;
}
