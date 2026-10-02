package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 统计导出任务 stat_export_task
 *
 * <p>大表格走异步：前端提交后拿任务号，去下载中心取文件，避免请求超时。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_export_task")
public class StatExportTask extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";

    private Long id;

    private Long examId;

    private String exportType;

    private String paramJson;

    private String status;

    private String fileOssId;

    private String fileName;

    private Integer rowCount;

    private Long operator;

    private String errorMsg;
}
