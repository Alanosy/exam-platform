package cn.org.alan.exam.model.vo.repo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/28 19:57
 */
@Data
public class RepoVO {
    // 题库ID
    private Integer id;
    // 用户ID
    private Integer userId;
    // 题库标题
    private String title;
    // 是否可以刷题
    private Integer isExercise;
    // 创建试卷
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    // 创建人真实姓名
    private String realName;
}
