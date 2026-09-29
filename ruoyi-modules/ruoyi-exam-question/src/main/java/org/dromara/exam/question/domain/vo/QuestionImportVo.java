package org.dromara.exam.question.domain.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.common.excel.annotation.ExcelRequired;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试题批量导入模板对象
 *
 * <p>列头即用户看到的 Excel 表头，列顺序由 {@code @ExcelProperty} 的 index 固定，
 * 与 QuestionImportServiceImpl 里的 COL_* 常量一一对应（模板下拉框按列序号挂载）。
 *
 * <p>题型 / 难度 / 状态三列既支持填中文名称（如「单选题」）也支持填字典编码（如 SINGLE），
 * 由后端统一翻译成库里的编码，用户不用关心编码长什么样。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@NoArgsConstructor
public class QuestionImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题库名称
     *
     * <p>填中文名称，后端翻译成题库ID；留空则落到上传时选择的默认题库。
     * 每行可以填不同题库，因此一次导入可以覆盖多个题库。
     */
    @ExcelRequired
    @ExcelProperty(value = "题库名称", index = 0)
    private String bankName;

    /**
     * 题型（中文名称或编码均可）
     */
    @ExcelRequired
    @ExcelProperty(value = "题型", index = 1)
    private String questionType;

    /**
     * 题干
     */
    @ExcelRequired
    @ExcelProperty(value = "题干", index = 2)
    private String title;

    @ExcelProperty(value = "选项A", index = 3)
    private String optionA;

    @ExcelProperty(value = "选项B", index = 4)
    private String optionB;

    @ExcelProperty(value = "选项C", index = 5)
    private String optionC;

    @ExcelProperty(value = "选项D", index = 6)
    private String optionD;

    @ExcelProperty(value = "选项E", index = 7)
    private String optionE;

    @ExcelProperty(value = "选项F", index = 8)
    private String optionF;

    /**
     * 正确答案
     *
     * <p>单选 / 多选：选项标识，多个用英文逗号分隔，如 {@code A}、{@code A,C}
     * <p>判断题：正确 / 错误
     * <p>填空题：多个空用 {@code |} 分隔，同一个空的多种可接受写法用 {@code ;} 分隔
     * <p>匹配题：多组用 {@code ;} 分隔，每组按 {@code 左项=右项} 填写
     * <p>简答 / 论述 / 文件上传 / 代码题：直接填参考答案文本
     */
    @ExcelRequired
    @ExcelProperty(value = "正确答案", index = 9)
    private String answer;

    /**
     * 难度（中文名称或编码均可），留空默认中等
     */
    @ExcelProperty(value = "难度", index = 10)
    private String difficulty;

    /**
     * 默认分值，留空则不设置
     */
    @ExcelProperty(value = "分值", index = 11)
    private String score;

    /**
     * 试题解析
     */
    @ExcelProperty(value = "解析", index = 12)
    private String analysis;

    /**
     * 状态（中文名称或编码均可），留空默认启用
     */
    @ExcelProperty(value = "状态", index = 13)
    private String status;

    /**
     * 代码题的编程语言，仅代码题需要
     */
    @ExcelProperty(value = "代码语言", index = 14)
    private String language;

}
