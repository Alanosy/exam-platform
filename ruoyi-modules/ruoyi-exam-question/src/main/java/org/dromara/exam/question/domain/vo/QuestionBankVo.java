package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.QuestionBank;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 题库视图对象 question_bank
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = QuestionBank.class)
public class QuestionBankVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 题库名称
     */
    @ExcelProperty(value = "题库名称")
    private String bankName;

    /**
     * 题库描述
     */
    @ExcelProperty(value = "题库描述")
    private String bankDesc;

    /**
     * 创建人用户ID
     */
    @ExcelProperty(value = "创建人用户ID")
    private Long creatorId;

    /**
     * 可见性 private私有 / public公开
     */
    @ExcelProperty(value = "可见性 private私有 / public公开")
    private String visibility;

    /**
     * 状态 0草稿 1正常 2归档
     */
    @ExcelProperty(value = "状态 0草稿 1正常 2归档")
    private Long status;


}
