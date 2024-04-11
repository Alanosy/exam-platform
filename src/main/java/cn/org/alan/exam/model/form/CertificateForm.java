package cn.org.alan.exam.model.form;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/4/11 15:15
 */
public class CertificateForm {
    private String certificateName;

    /**
     * 背景图片
     */
    private String image;

    /**
     * 认证单位
     */
    private String certificationNuit;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
