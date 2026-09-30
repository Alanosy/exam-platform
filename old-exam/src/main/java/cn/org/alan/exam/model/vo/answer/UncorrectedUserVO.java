package cn.org.alan.exam.model.vo.answer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 未批改用户
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/29 9:25
 */
@Data
public class UncorrectedUserVO {
    // 用户ID
    private Integer userId;
    // 用户名称
    private String userName;
    // 试卷标题
    private String examTitle;
    // 试卷ID
    private Integer examId;
    // 考试时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime limitTime;
    private String corrected;

}
