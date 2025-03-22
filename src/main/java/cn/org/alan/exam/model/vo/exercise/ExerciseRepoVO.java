package cn.org.alan.exam.model.vo.exercise;

import lombok.Data;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/5/6 11:08
 */
@Data
public class ExerciseRepoVO {
    private Integer id;
    // 题库标题
    private String repoTitle;
    // 总题数
    private Integer totalCount;
    private Integer exerciseCount;
}
