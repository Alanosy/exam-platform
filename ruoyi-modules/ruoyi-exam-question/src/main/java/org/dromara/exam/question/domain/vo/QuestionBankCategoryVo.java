package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.QuestionBankCategory;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 题库分类目录视图对象 question_bank_category
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = QuestionBankCategory.class)
public class QuestionBankCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类id
     */
    @ExcelProperty(value = "分类id")
    private Long id;

    /**
     * 父分类id，0根节点
     */
    @ExcelProperty(value = "父分类id，0根节点")
    private Long parentId;

    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    private String categoryName;

    /**
     * 排序
     */
    @ExcelProperty(value = "排序")
    private Long sort;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long isDeleted;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}
