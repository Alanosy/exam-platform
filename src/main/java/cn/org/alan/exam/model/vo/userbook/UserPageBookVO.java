package cn.org.alan.exam.model.vo.userbook;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/25 2:10 PM
 */
@Data
public class UserPageBookVO {
    private String id;
    /**
     * 考试标题
     */
    private String title;

    /**
     * 错题数
     */
    private String numberOfErrors;

    /**
     * 考试id
     */
    private Integer examId;

    /**
     * 创建时间    YYYY-MM-DD hh:mm:ss
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
