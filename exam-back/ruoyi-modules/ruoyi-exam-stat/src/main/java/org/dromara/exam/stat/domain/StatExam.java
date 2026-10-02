package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 考试表：统计服务只读的那几列
 *
 * <p>考试的主实体在考试管理服务，这里不引入它的包（引入就等于把两个服务焊死），
 * 只按统计需要映射字段。能确定的是「统计只读不写」：任何写操作都应该回考试管理服务。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@TableName("exam")
public class StatExam extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    @TableId(value = "id")
    private Long id;

    /** 考试名称 */
    private String examName;

    /** 考试类型 1正式考试 / 2练习考试 */
    private String examType;

    /** 考试开始时间 */
    private Date startTime;

    /** 考试结束时间 */
    private Date endTime;

    /** not_start未开始 / ongoing进行中 / finished已结束 / archived归档 */
    private String status;

    /** 逻辑删除 0未删 1已删 */
    @TableLogic
    private Long delFlag;

}
