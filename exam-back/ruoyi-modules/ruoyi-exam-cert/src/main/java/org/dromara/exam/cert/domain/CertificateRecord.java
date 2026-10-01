package org.dromara.exam.cert.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 证书颁发记录对象 exam_certificate_record
 *
 * <p>考生及格后生成一条。正文 / 标题 / 印章 / 背景在颁发时从模板快照，
 * 模板后续怎么改都不影响这张证书。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_certificate_record")
public class CertificateRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态：有效 */
    public static final String STATUS_VALID = "0";

    /** 状态：已吊销 */
    public static final String STATUS_REVOKED = "1";

    /** 颁发方式：及格自动颁发 */
    public static final String ISSUE_AUTO = "0";

    /** 颁发方式：手动补发 */
    public static final String ISSUE_MANUAL = "1";

    /** 主键ID */
    @TableId(value = "id")
    private Long id;

    /** 证书模板ID */
    private Long certId;

    /** 证书编号（全局唯一） */
    private String certNo;

    /** 考试ID */
    private Long examId;

    /** 考试名称快照 */
    private String examName;

    /** 答卷记录ID（答题库 exam_record 主键，跨库只存ID） */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号快照 */
    private String account;

    /** 考生姓名快照 */
    private String nickName;

    /** 第几次参加 */
    private Integer attemptNo;

    /** 得分快照 */
    private BigDecimal score;

    /** 及格分快照 */
    private BigDecimal passScore;

    /** 试卷总分快照 */
    private BigDecimal totalScore;

    /** 证书大标题快照 */
    private String title;

    /** 证书副标题快照 */
    private String subtitle;

    /** 证书正文快照（占位符已替换） */
    private String content;

    /** 发证机构快照 */
    private String issuer;

    /** 印章图片 ossId 快照 */
    private String sealOssId;

    /** 背景图 ossId 快照 */
    private String bgOssId;

    /** 背景色快照 */
    private String bgColor;

    /** 版式 0横版 1竖版 */
    private String orientation;

    /** 颁发方式 0及格自动颁发 1手动补发 */
    private String issueType;

    /** 颁发时间 */
    private Date issueTime;

    /** 失效时间，永久有效为 null */
    private Date expireTime;

    /** 状态 0有效 1已吊销 */
    private String status;

    /** 吊销原因 */
    private String revokeReason;

    /** 吊销时间 */
    private Date revokeTime;

    /** 备注 */
    private String remark;

    /**
     * 删除标志 0存在 1删除
     *
     * <p>BaseEntity 不含 delFlag，租户实体要自己声明。
     */
    @TableLogic
    private Long delFlag;
}
