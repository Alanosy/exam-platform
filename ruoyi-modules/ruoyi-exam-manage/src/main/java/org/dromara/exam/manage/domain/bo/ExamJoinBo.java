package org.dromara.exam.manage.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 加入公开考试的业务对象 exam_join
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamJoinBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参与密码，考试设置了参与密码时必填
     */
    private String password;

}
