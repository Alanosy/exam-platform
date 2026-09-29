package org.dromara.exam.question.listener;

import cn.hutool.core.util.StrUtil;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import cn.idev.excel.exception.ExcelAnalysisException;
import cn.idev.excel.exception.ExcelDataConvertException;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.excel.core.DefaultExcelResult;
import org.dromara.common.excel.core.ExcelListener;
import org.dromara.common.excel.core.ExcelResult;
import org.dromara.exam.question.domain.bo.QuestionImportRow;
import org.dromara.exam.question.domain.vo.QuestionImportVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 试题导入监听
 *
 * <p>只负责把 Excel 解析成「行号 + 数据」，业务校验统一放到 QuestionImportServiceImpl，
 * 这样一次可以把所有行的错误一起收集完再回给用户，而不是遇到第一行错误就中断。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Slf4j
public class QuestionImportListener extends AnalysisEventListener<QuestionImportVo> implements ExcelListener<QuestionImportVo> {

    private final List<QuestionImportRow> rows = new ArrayList<>();

    private final List<String> errorList = new ArrayList<>();

    @Override
    public void invoke(QuestionImportVo vo, AnalysisContext context) {
        if (isEmptyRow(vo)) {
            return;
        }
        rows.add(new QuestionImportRow(context.readRowHolder().getRowIndex() + 1, vo));
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        String errMsg;
        if (exception instanceof ExcelDataConvertException convertException) {
            // 单元格类型转换异常，能定位到具体行列
            errMsg = StrUtil.format("第{}行-第{}列-表头{}: 解析异常",
                convertException.getRowIndex() + 1,
                convertException.getColumnIndex() + 1,
                convertException.getCellData().getStringValue());
        } else {
            errMsg = StrUtil.format("第{}行解析异常：{}",
                context.readRowHolder().getRowIndex() + 1, exception.getMessage());
        }
        errorList.add(errMsg);
        log.error("试题导入解析失败 {}", errMsg, exception);
        throw new ExcelAnalysisException(errMsg);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.debug("试题导入解析完成，共 {} 行", rows.size());
    }

    @Override
    public ExcelResult<QuestionImportVo> getExcelResult() {
        List<QuestionImportVo> list = new ArrayList<>();
        for (QuestionImportRow row : rows) {
            list.add(row.getData());
        }
        return new DefaultExcelResult<>(list, errorList);
    }

    /**
     * 解析出来的数据行（带 Excel 行号）
     */
    public List<QuestionImportRow> getRows() {
        return rows;
    }

    /**
     * 整行空白的行直接跳过，避免用户在表格里留了空行就被判成必填项缺失
     */
    private boolean isEmptyRow(QuestionImportVo vo) {
        return StrUtil.isBlank(vo.getBankName())
            && StrUtil.isBlank(vo.getQuestionType())
            && StrUtil.isBlank(vo.getTitle())
            && StrUtil.isBlank(vo.getOptionA())
            && StrUtil.isBlank(vo.getOptionB())
            && StrUtil.isBlank(vo.getOptionC())
            && StrUtil.isBlank(vo.getOptionD())
            && StrUtil.isBlank(vo.getOptionE())
            && StrUtil.isBlank(vo.getOptionF())
            && StrUtil.isBlank(vo.getAnswer())
            && StrUtil.isBlank(vo.getDifficulty())
            && StrUtil.isBlank(vo.getScore())
            && StrUtil.isBlank(vo.getAnalysis())
            && StrUtil.isBlank(vo.getStatus())
            && StrUtil.isBlank(vo.getLanguage());
    }

}
