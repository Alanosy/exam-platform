package cn.org.alan.exam.model.form.exam;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 试卷更新请求体
 *
 * @Author Alan
 * @Version
 * @Date 2024/4/1 3:17 PM
 */
@Data
public class ExamUpdateForm {

    // 考试标题
    @NotBlank(message = "考试标题不能为空")
    @Size(min = 3, max = 20, message = "请输入3-20个字符的考试标题")
    private String title;

    // 考试时长
    @NotNull(message = "请设置考试时间,单位m")
    @Min(value=0,message = "请设置大于0的考试时长")
    private Integer examDuration;

    // 最大切屏次数
    private Integer maxCount;

    // 及格分
    @Min(value=0,message = "及格分数必须大于0")
    @NotNull(message = "及格分不能为空")
    private Integer passedScore;

    // 开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime startTime;

    // 结束时间
    @Future(message = "结束时间必须是一个必须是一个将来的日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime endTime;

    // 考试班级
    private String gradeIds;

    // 证书id
    private Integer certificateId;

    // 单选题分数
    @NotNull(message = "单选题分数不能为空")
    @Min(value = 0)
    private Integer radioScore;

    // 多选题分数
    @NotNull(message = "多选题分数不能为空")
    @Min(value = 0)
    private Integer multiScore;


    // 判断题分数
    @NotNull(message = "判断题分数不能为空")
    @Min(value = 0)
    private Integer judgeScore;

    // 简答题分数
    @NotNull(message = "简答题分数不能为空")
    @Min(value = 0)
    private Integer saqScore;
}
