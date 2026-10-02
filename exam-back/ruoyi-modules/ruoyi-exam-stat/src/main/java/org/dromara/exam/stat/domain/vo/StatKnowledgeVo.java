package org.dromara.exam.stat.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.stat.domain.StatExamKnowledge;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 知识点薄弱分析行
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = StatExamKnowledge.class)
public class StatKnowledgeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    /** 一期就是题库分类ID */
    private Long knowledgeId;

    private String knowledgeName;

    private String knowledgePath;

    private Integer questionCount;

    private BigDecimal fullScore;

    private BigDecimal avgScore;

    private BigDecimal scoreRate;

    private Integer wrongCount;

    private BigDecimal wrongRate;

    /** 掌握度%：1 − 失分 / 满分，按分值加权 */
    private BigDecimal mastery;

    /** good / normal / weak */
    private String weakLevel;

    /** 在这个知识点上至少错一题的考生数：谁最该补 */
    private Integer affectedUserCount;
}
