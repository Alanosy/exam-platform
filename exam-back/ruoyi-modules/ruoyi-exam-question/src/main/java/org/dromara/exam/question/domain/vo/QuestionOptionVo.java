package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.QuestionOption;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 试题选项视图对象 question_option
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = QuestionOption.class)
public class QuestionOptionVo implements Serializable {

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
     * 选项标识 A/B/C/D
     */
    @ExcelProperty(value = "选项标识 A/B/C/D")
    private String optionKey;

    /**
     * 选项内容富文本
     */
    @ExcelProperty(value = "选项内容富文本")
    private String optionContent;

    /**
     * 排序号
     */
    @ExcelProperty(value = "排序号")
    private Long sort;


}
