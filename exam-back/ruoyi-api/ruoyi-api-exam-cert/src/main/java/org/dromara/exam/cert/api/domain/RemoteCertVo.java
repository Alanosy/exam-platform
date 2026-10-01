package org.dromara.exam.cert.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 证书（跨服务传输对象）
 *
 * <p>字段类型与 CertificateRecord 实体保持一致（Long / BigDecimal / Date），
 * MapStruct 不做隐式类型转换，类型写错会直接编译失败。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class RemoteCertVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 证书模板ID */
    private Long certId;

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

    /** 考生姓名 */
    private String nickName;

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

    /** 印章图片访问地址 */
    private String sealUrl;

    /** 背景图访问地址 */
    private String bgUrl;

    /** 背景色 */
    private String bgColor;

    /** 版式 0横版 1竖版 */
    private String orientation;

    /** 颁发时间 */
    private Date issueTime;

    /** 失效时间，永久有效为 null */
    private Date expireTime;

    /** 状态 0有效 1已吊销 */
    private String status;
}
