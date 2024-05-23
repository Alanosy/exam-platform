package cn.org.alan.exam.model.vo.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
