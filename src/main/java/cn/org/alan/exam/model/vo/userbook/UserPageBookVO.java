package cn.org.alan.exam.model.vo.userbook;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/25 2:10 PM
 */
@Data
public class UserPageBookVO {
    /**
     * 考试标题
     */
    private String title;
    /**
     * 错题数
     */
    private String numberOfErrors;
    /**
     * 考试id
     */
    private Integer examId;
}
