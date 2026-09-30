package org.dromara.exam.answer.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 考试记录查询条件
 *
 * <p>只查自己的记录，账号由服务端从登录态取，不信任前端传入。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamRecordBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID，为空查全部 */
    private Long examId;

    /** 状态 answering / submitted / expired */
    private String status;

    /** 是否及格 */
    private Boolean passed;

}
