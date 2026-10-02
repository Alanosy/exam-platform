package org.dromara.exam.stat.domain.bo;

import lombok.Data;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 考试统计列表 / 大盘筛选条件
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class StatExamQueryBo extends PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试名称关键词 */
    private String keyword;

    /** 考试类型：1正式 2练习 */
    private String examType;

    /** 考试状态 not_start / ongoing / finished / archived */
    private String examStatus;

    /** 创建人ID，管理端按创建人过滤 */
    private Long creatorId;

    /** 考试开始时间起 */
    private Date beginTime;

    /** 考试开始时间止 */
    private Date endTime;

    /** 只看有待阅卷的 */
    private Boolean onlyPendingMark;

    /** 只看我创建的 */
    private Boolean onlyMine;
}
