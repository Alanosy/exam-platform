package org.dromara.exam.cert.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.domain.BaseEntity;
import org.dromara.exam.cert.domain.Certificate;

import java.io.Serial;

/**
 * 证书模板：查询条件 + 新增 / 编辑表单
 *
 * <p>沿用项目里 Bo 兼作查询与表单的惯例（见 ExamBo）：查询只用到 keyword / status，
 * 新增编辑用到其余字段，两边共用一个对象省一层转换。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Certificate.class, reverseConvertGenerate = false)
public class CertificateBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID，编辑时必填 */
    private Long id;

    /** 证书名称或编码，模糊匹配（仅查询用） */
    private String keyword;

    /** 证书名称 */
    private String certName;

    /** 证书编码 */
    private String certCode;

    /** 证书大标题 */
    private String title;

    /** 证书副标题 */
    private String subtitle;

    /** 证书正文模板 */
    private String content;

    /** 发证机构 */
    private String issuer;

    /** 印章图片 ossId */
    private String sealOssId;

    /** 背景图 ossId */
    private String bgOssId;

    /** 背景色 */
    private String bgColor;

    /** 版式 0横版 1竖版 */
    private String orientation;

    /** 有效期类型 0永久 1按天 */
    private String validType;

    /** 有效期天数 */
    private Integer validDays;

    /** 状态 0启用 1停用 */
    private String status;

    /** 备注 */
    private String remark;
}
