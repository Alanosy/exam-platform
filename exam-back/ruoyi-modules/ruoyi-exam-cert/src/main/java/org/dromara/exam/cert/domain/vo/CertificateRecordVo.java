package org.dromara.exam.cert.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.cert.domain.CertificateRecord;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 证书颁发记录（列表 / 详情 / 考生端我的证书）
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = CertificateRecord.class)
public class CertificateRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 证书模板ID */
    private Long certId;

    /** 证书模板名称（非表字段，服务层回填，模板被删时仍显示原名） */
    private String certName;

    /** 证书编号 */
    private String certNo;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 答卷记录ID */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 考生姓名 */
    private String nickName;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 得分 */
    private BigDecimal score;

    /** 及格分 */
    private BigDecimal passScore;

    /** 试卷总分 */
    private BigDecimal totalScore;

    /** 证书大标题 */
    private String title;

    /** 证书副标题 */
    private String subtitle;

    /** 证书正文（占位符已替换） */
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

    /** 颁发方式 0自动 1手动 */
    private String issueType;

    /** 颁发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date issueTime;

    /** 失效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 状态 0有效 1已吊销 */
    private String status;

    /** 吊销原因 */
    private String revokeReason;

    /** 吊销时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date revokeTime;

    /** 备注 */
    private String remark;

    /** 印章图片访问地址（非表字段，服务层回填） */
    private String sealUrl;

    /** 背景图访问地址（非表字段，服务层回填） */
    private String bgUrl;
}
