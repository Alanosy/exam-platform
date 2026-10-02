package org.dromara.exam.stat.domain.ref;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * exam_mark_task 只读投影：判断一份答卷的主观题阅完没有
 *
 * <p>入统口径全靠它：有 mark 任务且 status 不是 finished 的答卷，
 * 主观题分还没定，不能进统计，否则平均分每阅一份卷就跳一次。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_mark_task")
public class MarkTaskRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 待阅 */
    public static final String STATUS_PENDING = "pending";
    /** 阅卷中 */
    public static final String STATUS_MARKING = "marking";
    /** 已阅完 */
    public static final String STATUS_FINISHED = "finished";

    private Long id;

    private Long examId;

    private Long recordId;

    private Long userId;

    private Integer questionCount;

    private Integer markedCount;

    private String status;

    private String markerName;

    @TableLogic
    private Long delFlag;
}
