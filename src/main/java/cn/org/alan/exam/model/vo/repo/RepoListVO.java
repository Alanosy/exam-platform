package cn.org.alan.exam.model.vo.repo;

import lombok.Data;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/6/3 21:38
 */
@Data
public class RepoListVO {
    private Integer id;
    private String title;
    private Integer radioNum;
    private Integer multiNum;
    private Integer judgeNum;
    private Integer saqNum;
}
