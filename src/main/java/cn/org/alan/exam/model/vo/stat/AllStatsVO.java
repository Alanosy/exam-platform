package cn.org.alan.exam.model.vo.stat;

import lombok.Data;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/5/12 15:59
 */
@Data
public class AllStatsVO {
    // 班级数量
    private Integer classCount;
    // 试卷数量
    private Integer examCount;
    // 试题数量
    private Integer questionCount;


}
