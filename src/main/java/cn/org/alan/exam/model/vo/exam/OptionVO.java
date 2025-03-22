package cn.org.alan.exam.model.vo.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/20 10:04 AM
 */
@Data
public class OptionVO {
    /**
     * id   选项答案表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 试题id
     */
    private Integer quId;


    /**
     * 图片地址   0错误 1正确
     */
    private String image;

    /**
     * 选项内容
     */
    @NotBlank(message = "选型内容(content)不能为空")
    private String content;

    private Boolean checkout;
    /**
     * 排序
     */
    private Integer sort;
}
