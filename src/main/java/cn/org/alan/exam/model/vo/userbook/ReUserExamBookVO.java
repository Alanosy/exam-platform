package cn.org.alan.exam.model.vo.userbook;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/25 2:33 PM
 */
@Data
public class ReUserExamBookVO {
    private static final long serialVersionUID = 1L;

    /**
     * ID   错题本
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 用户id    唯一
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 题目id    唯一
     */
    private Integer quId;

    /**
     * 创建时间    YYYY-MM-DD hh:mm:ss
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
