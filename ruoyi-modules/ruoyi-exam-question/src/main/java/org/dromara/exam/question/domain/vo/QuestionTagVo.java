package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.QuestionTag;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 试题标签视图对象 question_tag
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = QuestionTag.class)
public class QuestionTagVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 标签名称
     */
    @ExcelProperty(value = "标签名称")
    private String tagName;

    /**
     * 创建人ID
     */
    @ExcelProperty(value = "创建人ID")
    private Long creatorId;


}
