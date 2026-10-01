package org.dromara.exam.cert.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 考试表轻量引用（只读）
 *
 * <p>证书服务与 exam 表同库（都在 ry-exam），判断「这场考试配没配证书」直接读 exam.cert_id，
 * 没必要绕一圈 Dubbo 去问考试服务 —— 少一次远程调用，也少一个失败点。
 *
 * <p>只声明用到的字段，不复制整个 Exam 实体，免得考试表加字段时这里跟着动。
 * 租户条件由 TenantEntity + 租户拦截器自动追加。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class ExamRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 考试名称 */
    private String examName;

    /** 及格证书模板ID，为空表示不发证书 */
    private Long certId;
}
