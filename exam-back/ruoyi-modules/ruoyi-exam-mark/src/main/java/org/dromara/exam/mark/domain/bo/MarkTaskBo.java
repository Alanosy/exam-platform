package org.dromara.exam.mark.domain.bo;

import lombok.Data;

import java.io.Serial;

/**
 * 阅卷任务查询条件（某场考试下的答卷列表）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class MarkTaskBo {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 试卷ID */
    private Long paperId;

    /** 考生账号 / 姓名关键字 */
    private String account;

    /** pending待阅 / marking阅卷中 / finished已阅完 */
    private String status;
}
