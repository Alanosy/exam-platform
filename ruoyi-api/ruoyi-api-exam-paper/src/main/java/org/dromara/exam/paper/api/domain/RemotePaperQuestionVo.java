package org.dromara.exam.paper.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试卷内的题目（跨服务传输用）
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class RemotePaperQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 该题目在本试卷内的分值，为空表示用题目默认分值 */
    private Long score;

    /** 排序号 */
    private Long sort;

}
