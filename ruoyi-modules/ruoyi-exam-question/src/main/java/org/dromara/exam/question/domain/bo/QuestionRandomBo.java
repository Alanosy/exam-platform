package org.dromara.exam.question.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 随机抽题条件（组卷使用）
 *
 * <p>只用于「随机抽题」接口的入参，不参与试题本身的增删改查，
 * 因此不标注 {@code @AutoMapper}。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
public class QuestionRandomBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 限定题库，为空表示全部题库
     */
    private Long bankId;

    /**
     * 限定题型 SINGLE/MULTIPLE/JUDGE/BLANK 等，为空表示不限题型
     */
    private String questionType;

    /**
     * 限定难度 easy/medium/hard，为空表示不限难度
     */
    private String difficulty;

    /**
     * 抽取数量
     */
    private Integer count;

    /**
     * 需要排除的试题ID，用于「换一批」时避免重复抽到已选题
     */
    private List<Long> excludeIds;

}
