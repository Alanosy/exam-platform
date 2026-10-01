package org.dromara.exam.cert.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 证书模板对象 exam_certificate
 *
 * <p>一场考试选一个模板；模板改了不影响已经发出去的证书 —— 颁发时把标题 / 正文 /
 * 印章 / 背景全部快照进 exam_certificate_record，模板后续被改甚至被删都不影响旧证书。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_certificate")
public class Certificate extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态：启用 */
    public static final String STATUS_NORMAL = "0";

    /** 状态：停用 */
    public static final String STATUS_DISABLE = "1";

    /** 版式：横版 */
    public static final String ORIENTATION_LAND = "0";

    /** 有效期：永久 */
    public static final String VALID_FOREVER = "0";

    /** 有效期：按天 */
    public static final String VALID_DAYS = "1";

    /** 主键ID */
    @TableId(value = "id")
    private Long id;

    /** 证书名称（管理用） */
    private String certName;

    /** 证书编码（业务唯一标识） */
    private String certCode;

    /** 证书大标题 */
    private String title;

    /** 证书副标题 */
    private String subtitle;

    /**
     * 证书正文模板
     *
     * <p>支持占位符：{nickName} 姓名 / {account} 账号 / {examName} 考试名称 /
     * {score} 得分 / {totalScore} 总分 / {passScore} 及格分 / {certNo} 证书编号 /
     * {issueDate} 颁发日期 / {expireDate} 有效期至
     */
    private String content;

    /** 发证机构 / 签发人 */
    private String issuer;

    /** 印章图片 ossId */
    private String sealOssId;

    /** 背景图 ossId */
    private String bgOssId;

    /** 背景色（无背景图时生效） */
    private String bgColor;

    /** 版式 0横版 1竖版 */
    private String orientation;

    /** 有效期类型 0永久有效 1按天计算 */
    private String validType;

    /** 有效期天数 */
    private Integer validDays;

    /** 已颁发数量（冗余计数） */
    private Integer issueCount;

    /** 状态 0启用 1停用 */
    private String status;

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
