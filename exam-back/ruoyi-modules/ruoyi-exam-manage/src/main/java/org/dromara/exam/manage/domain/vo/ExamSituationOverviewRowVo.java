package org.dromara.exam.manage.domain.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导出的「成绩概览」页：一行一个指标
 *
 * <p>导出要的是「一眼看懂这场考试怎么样」，所以概览和明细分成两个 sheet。
 * 这里刻意做成两列的键值对而不是每个字段一列——指标会随业务增删，
 * 键值对加一行不影响表头，宽表加一个字段就得改列宽和表头。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class ExamSituationOverviewRowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "指标")
    private String label;

    @ExcelProperty(value = "数值")
    private String value;

    public ExamSituationOverviewRowVo() {
    }

    public ExamSituationOverviewRowVo(String label, String value) {
        this.label = label;
        this.value = value;
    }
}
