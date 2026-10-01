package org.dromara.exam.cert.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.cert.domain.Certificate;

import java.io.Serial;
import java.io.Serializable;

/**
 * 证书模板（列表 / 详情 / 表单回显）
 *
 * <p>sealUrl / bgUrl 不在表里（表上只存 ossId），由服务层查文件服务后回填，
 * 前端拿到就能直接 img src 用。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = Certificate.class)
public class CertificateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

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

    /** 已颁发数量 */
    private Integer issueCount;

    /** 状态 0启用 1停用 */
    private String status;

    /** 备注 */
    private String remark;

    /** 印章图片访问地址（非表字段，服务层回填） */
    private String sealUrl;

    /** 背景图访问地址（非表字段，服务层回填） */
    private String bgUrl;

    /** 创建时间 */
    private java.util.Date createTime;
}
