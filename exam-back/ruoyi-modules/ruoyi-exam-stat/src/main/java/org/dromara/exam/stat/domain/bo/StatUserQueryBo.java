package org.dromara.exam.stat.domain.bo;

import lombok.Data;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 考生成绩筛选条件
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class StatUserQueryBo extends PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long examId;

    /** 姓名 / 账号 */
    private String keyword;

    /** 部门ID，按部门筛 */
    private Long deptId;

    /** 是否及格 0否 1是 */
    private Integer passed;

    /** 分数区间下限 */
    private BigDecimal minScore;

    /** 分数区间上限 */
    private BigDecimal maxScore;

    /** COUNTED / PENDING_MARK / EXCLUDED */
    private String statStatus;
}
