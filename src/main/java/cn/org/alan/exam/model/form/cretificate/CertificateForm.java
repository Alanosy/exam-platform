package cn.org.alan.exam.model.form.cretificate;


import cn.org.alan.exam.common.group.CertificateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 证书请求体
 *
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/4/11 15:15
 */
@Data
public class CertificateForm {

    // 证书ID
    private Integer id;

    // 证书名称
    @NotBlank(message = "证书名称不能为空",groups = CertificateGroup.CertificateInsertGroup.class)
    private String certificateName;

    // 背景图片
    private String image;

    // 认证单位
    @NotBlank(message = "发证单位不能为空",groups = CertificateGroup.CertificateInsertGroup.class)
    private String certificationNuit;

}
