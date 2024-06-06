package cn.org.alan.exam.model.vo.certificate;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/26 7:06 PM
 */
@Data
public class MyCertificateVO {
    /**
     * 证书编号
     */
    private String code;
    /**
     * ID   证书
     */
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 考试名称
     */
    private String examName;
    /**
     * 考试id
     */
    private Integer examId;

}
