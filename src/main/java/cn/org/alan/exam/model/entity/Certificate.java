package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@TableName("t_certificate")
public class Certificate implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * ID   证书
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 证数名称
     */
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

//    public static Integer getId() {
//        return id;
//    }
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
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Certificate{" +
            "id=" + id +
            ", certificateName=" + certificateName +
            ", image=" + image +
            ", certificationNuit=" + certificationNuit +
            ", createTime=" + createTime +
        "}";
    }

}
