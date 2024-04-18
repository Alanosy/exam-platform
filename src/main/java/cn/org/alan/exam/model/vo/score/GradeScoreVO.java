package cn.org.alan.exam.model.vo.score;

import lombok.Data;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/15 9:28
 */
@Data
public class GradeScoreVO {
    /**
     * 平均分
     */
    private Integer avgScore;
    /**
     * 最高分
     */
    private Integer maxScore;
    /**
     * 最低分
     */
    private Integer minScore;
    /**
     * 参考人数
     */
    private Integer attendNum;
    /**
     * 缺考人数
     */
    private Integer absentNum;
    /**
     * 及格人数
     */
    private Integer passedNum;
    /**
     * 应参加人数
     */
    private Integer totalNum;
    /**
     * 通过率
     */
    private Double passingRate;
}
