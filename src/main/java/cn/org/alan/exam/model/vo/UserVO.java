package cn.org.alan.exam.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/31 13:14
 */
@Data
public class UserVO {

    private Integer id;
    private String userName;
    private String realName;
    private String roleName;
    private String password;
    private String avatar;
    private String gradeName;
    private Integer gradeId;
    /**
     * 班级创建人Id
     */
    private Integer userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String status;

}
