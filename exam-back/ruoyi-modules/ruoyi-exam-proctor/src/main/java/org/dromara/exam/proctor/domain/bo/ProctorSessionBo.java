package org.dromara.exam.proctor.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 监考会话查询条件
 *
 * <p>examId 为空时自动收敛到「当前用户创建的考试」，发布者只能看自己考试的监考数据。
 *
 * @author ruoyi
 */
@Data
public class ProctorSessionBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 答卷记录ID */
    private Long recordId;

    /** 考生账号 / 姓名关键字 */
    private String keyword;

    /** online / offline / submitted / force_submit */
    private String status;

    /** normal / suspect / serious */
    private String riskLevel;

    /** 只看有异常的（切屏 / 粘贴 / 抓拍异常大于 0） */
    private Boolean onlyRisk;
}
