package org.dromara.exam.paper.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试卷信息（跨服务传输用）
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class RemotePaperVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试卷ID */
    private Long paperId;

    /** 试卷名称 */
    private String paperName;

    /** 试卷描述 */
    private String paperDesc;

    /** 试卷总分 */
    private Long totalScore;

    /** 及格分 */
    private Long passScore;

    /** 考试时长(分钟)，0不限时 */
    private Long timeLimit;

    /** 客观题是否自动判分 0否 1是 */
    private String autoJudge;

    /** 主观题是否人工阅卷 0否 1是 */
    private String manualReview;

    /** 是否支持部分得分 0否 1是 */
    private String partialScore;

    /** 是否题目乱序 0否 1是 */
    private String questionShuffle;

    /** 是否选项乱序 0否 1是 */
    private String optionShuffle;

    /** 默认单题分值 */
    private Long defaultScore;

}
