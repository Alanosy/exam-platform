package org.dromara.exam.proctor.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 考试分组查询条件（监考中心第一层）
 *
 * <p>监考中心先按考试分组看「哪场考试有问题」，点进去才是这场考试的考生明细。
 * 可见范围同样收敛到当前用户创建的考试。
 *
 * @author ruoyi
 */
@Data
public class ProctorExamGroupBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试名称关键字 */
    private String keyword;

    /** 只看有异常的考试（有人切屏 / 粘贴 / 开开发者工具 / 多开 / 退出全屏） */
    private Boolean onlyRisk;
}
