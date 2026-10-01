package org.dromara.exam.mark.domain.bo;

import lombok.Data;

import java.io.Serial;

/**
 * 阅卷列表查询条件（按考试聚合）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class MarkExamBo {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试名称关键字（考试库字段，只能在取回考试后匹配） */
    private String examName;

    /** 试卷名称关键字（试卷库字段，同理） */
    private String paperName;

    /** 只看未完成的考试：pending / marking */
    private Boolean onlyUnfinished;
}
