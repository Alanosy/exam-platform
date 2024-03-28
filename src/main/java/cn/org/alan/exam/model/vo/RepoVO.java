package cn.org.alan.exam.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/28 19:57
 */
@Data
public class RepoVO {

    private Integer id;
    private Integer userId;
    private String title;
    private LocalDateTime createTime;
    private String realName;
}
