package org.dromara.exam.cert.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 证书颁发记录查询条件
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateRecordBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 证书模板ID */
    private Long certId;

    /** 考试ID */
    private Long examId;

    /** 考生姓名 / 账号 / 证书编号，模糊匹配 */
    private String keyword;

    /** 状态 0有效 1已吊销，为空表示全部 */
    private String status;
}
