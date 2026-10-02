package org.dromara.exam.stat.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 首页用的单场考试
 *
 * <p>只带 Dashboard 上露出来的那几列：名字、时间段、参考人数。
 * 统计服务只认考试表里的这几列，不引入考试模块的实体，避免为了 JPA 之外的东西反向依赖。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class HomeExamVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 考试类型 1正式考试 / 2练习考试 */
    private String examType;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 考试状态 not_start未开始 / ongoing进行中 / finished已结束 / archived归档 */
    private String status;

}
