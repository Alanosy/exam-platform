package org.dromara.exam.mark.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.mark.domain.MarkTask;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 阅卷任务（某场考试下的一份答卷）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@AutoMapper(target = MarkTask.class)
public class MarkTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 阅卷任务ID */
    private Long taskId;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 试卷ID */
    private Long paperId;

    /** 试卷名称 */
    private String paperName;

    /** 答卷记录ID */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 考生姓名，取不到时前端退回显示账号 */
    private String userName;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 主观题总题数 */
    private Integer questionCount;

    /** 已阅题数 */
    private Integer markedCount;

    /** pending待阅 / marking阅卷中 / finished已阅完 */
    private String status;

    /** 客观题得分 */
    private BigDecimal objectiveScore;

    /** 主观题得分 */
    private BigDecimal subjectiveScore;

    /** 总分 */
    private BigDecimal totalScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 是否及格 0否 1是 */
    private Long passed;

    /** 交卷时间 */
    private Date submitTime;

    /** 阅卷人姓名 */
    private String markerName;

    /** 最近一次阅卷时间 */
    private Date markTime;
}
