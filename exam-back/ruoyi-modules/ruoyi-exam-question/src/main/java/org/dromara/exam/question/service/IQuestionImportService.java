package org.dromara.exam.question.service;

import org.dromara.common.excel.core.DropDownOptions;
import org.dromara.exam.question.domain.bo.QuestionImportRow;

import java.util.List;

/**
 * 试题批量导入Service接口
 *
 * @author LionLi
 * @date 2026-09-29
 */
public interface IQuestionImportService {

    /**
     * 批量导入试题
     *
     * <p>先整批校验再整批入库：任意一行不过校验就一条都不写，避免导入一半留下脏数据。
     *
     * @param rows          解析出来的数据行（带 Excel 行号）
     * @param defaultBankId 上传时选择的默认题库，Excel 未填写题库名称时落到该题库
     * @return 导入结果描述
     */
    String importQuestions(List<QuestionImportRow> rows, Long defaultBankId);

    /**
     * 构建导入模板的下拉可选项
     *
     * <p>题库名称、题型、难度、状态四列直接给出可选项，用户不用手打，
     * 也顺便避免因为名称写错而导入失败。
     *
     * @return 按列序号挂载的下拉选项
     */
    List<DropDownOptions> buildTemplateOptions();

}
