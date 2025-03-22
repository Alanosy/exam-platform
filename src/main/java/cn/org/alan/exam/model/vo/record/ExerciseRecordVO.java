package cn.org.alan.exam.model.vo.record;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/30 12:29 PM
 */
@Data
public class ExerciseRecordVO {
    /**
     * id   题库表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 题库标题
     */
    @NotNull(message = "题库名不能为空")
    private String title;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
