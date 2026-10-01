package org.dromara.exam.practice.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 错题同步入参（跨服务传输用）
 *
 * <p>答题服务交卷判分后，把答错的题目交给练习服务写入错题本；
 * 主观题人工 / AI 阅卷判定为错误时，由阅卷服务用同样的结构回写。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class RemoteWrongQuestionBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 考生用户ID（访客没有用户ID，由调用方过滤，不传即为 null）
     */
    private Long userId;

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 来源：EXAM正式考试 / PAPER_PRACTICE试卷练习
     */
    private String sourceType;

    /**
     * 来源ID：exam_id 或者 paper_id
     */
    private Long sourceId;

    /**
     * 首次答错对应的小题作答记录id（仅溯源）
     */
    private Long answerItemId;

}
