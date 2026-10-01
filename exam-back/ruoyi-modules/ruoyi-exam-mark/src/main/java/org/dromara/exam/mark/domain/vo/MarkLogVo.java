package org.dromara.exam.mark.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.mark.domain.MarkLog;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 阅卷操作日志
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@AutoMapper(target = MarkLog.class)
public class MarkLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 阅卷任务ID */
    private Long taskId;

    /** 阅卷明细ID */
    private Long itemId;

    /** 试题ID */
    private Long questionId;

    /** create建任务 / score打分 / rescore改分 / ai预评 / finish完成阅卷 */
    private String action;

    /** 改动前得分 */
    private BigDecimal oldScore;

    /** 改动后得分 */
    private BigDecimal newScore;

    /** manual / ai / auto */
    private String markType;

    /** 备注 */
    private String remark;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作时间 */
    private Date createTime;
}
