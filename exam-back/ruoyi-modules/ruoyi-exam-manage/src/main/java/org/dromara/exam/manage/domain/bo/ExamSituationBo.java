package org.dromara.exam.manage.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 考试情况查询条件
 *
 * <p>数据本身在答题库，这里只传过滤条件，分页交给框架的 PageQuery。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class ExamSituationBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    @NotNull(message = "考试ID不能为空")
    private Long examId;

    /** 关键词：匹配考生姓名 / 账号 */
    private String keyword;

    /** 答卷状态 answering / submitted / expired，为空查全部 */
    private String status;

    /** 是否及格：1及格 0不及格，为空查全部 */
    private Integer passed;

    /** 只看待阅：1是 */
    private Integer pendingMark;
}
