package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.*;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@TableName("t_option")
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 是否正确
     */
    @NotNull(message = "选型是否正确(isRight)不能为空")
    @Min(value = 0,message = "选项是否正确(isRight)只能是：0错误1正确")
    @Max(value = 1,message = "选项是否正确(isRight)只能是：0错误1正确")
    private Integer isRight;

    /**
     * 图片地址   0错误 1正确
     */
    private String image;

    /**
     * 选项内容
     */
    @NotBlank(message = "选型内容(content)不能为空")
    private String content;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 逻辑删除字段
     */
    private Integer isDeleted;



    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getQuId() {
        return quId;
    }

    public void setQuId(Integer quId) {
        this.quId = quId;
    }
    public Integer getIsRight() {
        return isRight;
    }

    public void setIsRight(Integer isRight) {
        this.isRight = isRight;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id=" + id +
                ", quId=" + quId +
                ", isRight=" + isRight +
                ", image='" + image + '\'' +
                ", content='" + content + '\'' +
                ", sort=" + sort +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
