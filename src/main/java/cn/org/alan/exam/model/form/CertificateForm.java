package cn.org.alan.exam.model.form;


import cn.org.alan.exam.common.group.CertificateGroup;
import jakarta.validation.constraints.NotBlank;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/4/11 15:15
 */
public class CertificateForm {


    /**
     * ID   证书
     */
    private Integer id;
    @NotBlank(message = "证书名称不能为空",groups = CertificateGroup.CertificateInsertGroup.class)
    private String certificateName;

    /**
     * 背景图片
     */
    private String image;

    /**
     * 认证单位
     */
    @NotBlank(message = "发证单位不能为空",groups = CertificateGroup.CertificateInsertGroup.class)
    private String certificationNuit;





    public Integer getId(){
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCertificationNuit() {
        return certificationNuit;
    }

    public void setCertificationNuit(String certificationNuit) {
        this.certificationNuit = certificationNuit;
    }

}
