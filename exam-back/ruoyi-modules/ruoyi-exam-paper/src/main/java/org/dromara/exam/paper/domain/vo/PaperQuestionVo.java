package org.dromara.exam.paper.domain.vo;

import org.dromara.exam.paper.domain.PaperQuestion;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 试卷-试题中间视图对象 paper_question
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PaperQuestion.class)
public class PaperQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 试卷ID
     */
    @ExcelProperty(value = "试卷ID")
    private Long paperId;

    /**
     * 试题ID，关联question表
     */
    @ExcelProperty(value = "试题ID，关联question表")
    private Long questionId;

    /**
     * 该题目在本试卷内分值，null使用question表默认score
     */
    @ExcelProperty(value = "该题目在本试卷内分值，null使用question表默认score")
    private Long paperScore;

    /**
     * 题目在试卷中的排序
     */
    @ExcelProperty(value = "题目在试卷中的排序")
    private Long sort;


}
