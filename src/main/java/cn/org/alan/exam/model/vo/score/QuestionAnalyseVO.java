package cn.org.alan.exam.model.vo.score;

import lombok.Data;


/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/22 9:23
 */

@Data
public class QuestionAnalyseVO {

    private Integer rightCount;
    private Integer totalCount;
    private Double accuracy;

}
