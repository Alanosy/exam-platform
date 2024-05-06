package cn.org.alan.exam.model.vo.exercise;

import lombok.Data;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/5/6 11:08
 */
@Data
public class RepoVO {
    private Integer id;
    private String repoTitle;
    private Integer totalCount;
    private Integer exerciseCount;
}
