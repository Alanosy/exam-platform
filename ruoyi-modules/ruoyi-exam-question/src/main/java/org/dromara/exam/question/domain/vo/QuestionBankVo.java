package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.QuestionBank;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.exam.question.translation.ExamTransConstant;

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
     * 创建人名称（由 creatorId 翻译成用户昵称，仅用于接口返回展示；与试卷/考试模块保持一致）
     */
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "creatorId")
    private String creatorName;

    /**
     * 所属分类ID
     */
    @ExcelProperty(value = "所属分类ID")
    private Long categoryId;

    /**
     * 所属分类名称（由 categoryId 翻译，仅用于接口返回展示）
     */
    @Translation(type = ExamTransConstant.BANK_CATEGORY_ID_TO_NAME, mapper = "categoryId")
    private String categoryName;

    /**
     * 可见性 private私有 / public公开
     */
    @ExcelProperty(value = "可见性 private私有 / public公开")
    private String visibility;

    /**
     * 状态 0草稿 1正常 2归档
     */
    @ExcelProperty(value = "状态 0草稿 1正常 2归档")
    private String  status;


}
