package org.dromara.exam.cert.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 证书模板下拉选项（考试配置页选「及格证书」用）
 *
 * <p>只回启用中的模板，字段也只给下拉需要的几个，不带正文 / 背景这些大字段。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class CertificateOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long id;

    /** 证书名称 */
    private String certName;

    /** 证书编码 */
    private String certCode;

    /** 证书大标题 */
    private String title;

    /** 有效期类型 0永久 1按天 */
    private String validType;

    /** 有效期天数 */
    private Integer validDays;
}
