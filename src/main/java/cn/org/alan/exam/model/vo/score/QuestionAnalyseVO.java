package cn.org.alan.exam.model.vo.score;

import lombok.Data;


/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/22 9:23
 */

@Data
public class QuestionAnalyseVO {
    // 正确数量
    private Integer rightCount;
    // 总题数
    private Integer totalCount;
    private Double accuracy;

}
