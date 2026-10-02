package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 统计计算任务 stat_calc_task
 *
 * <p>每次重算留一条，便于排查「为什么这个数不对」——
 * 能看到是谁触发的、什么时候算的、算了多少毫秒、有没有报错。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@TableName("stat_calc_task")
public class StatCalcTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 交卷触发 */
    public static final String TRIGGER_AUTO = "auto";
    /** 阅卷完成触发 */
    public static final String TRIGGER_MARK = "mark";
    /** 定时任务 */
    public static final String TRIGGER_JOB = "job";
    /** 管理端手动 */
    public static final String TRIGGER_MANUAL = "manual";

    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";

    private Long id;

    private Long examId;

    private String triggerType;

    private String calcType;

    private String status;

    private Date startTime;

    private Date endTime;

    private Long costMs;

    private String errorMsg;

    private String tenantId;
}
