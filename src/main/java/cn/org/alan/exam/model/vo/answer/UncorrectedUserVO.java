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

    private Integer userId;
    private String userName;
    private String examTitle;
    private Integer examId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime limitTime;
    private String corrected;

}
