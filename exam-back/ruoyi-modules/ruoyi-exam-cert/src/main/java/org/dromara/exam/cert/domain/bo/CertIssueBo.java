package org.dromara.exam.cert.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 手工颁发证书入参
 *
 * <p>正常流程是及格自动颁发，这个接口给两种补漏场景用：
 *   1. 考试先没配证书、后来补配了，之前及格的人要补发
 *   2. 证书服务当时没起来，自动颁发失败，事后补发
 *
 * <p>成绩不在这里传，由证书服务拿 recordId 去答题服务查 —— 手工补发最容易出错的就是
 * 凭印象填分数，拉真实成绩才不会发出分数对不上的证书。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class CertIssueBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 答卷记录ID */
    private Long recordId;

    /** 备注 */
    private String remark;
}
