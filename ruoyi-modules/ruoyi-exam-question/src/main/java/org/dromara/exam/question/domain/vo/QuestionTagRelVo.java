package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.QuestionTagRel;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 试题标签关联视图对象 question_tag_rel
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = QuestionTagRel.class)
public class QuestionTagRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 试题ID
     */
    @ExcelProperty(value = "试题ID")
    private Long questionId;

    /**
     * 标签ID
     */
    @ExcelProperty(value = "标签ID")
    private Long tagId;


}
