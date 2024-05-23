package cn.org.alan.exam.model.vo.answer;

import lombok.Data;

/**
 * 用户作答信息
 *
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/29 10:29
 */
@Data
public class UserAnswerDetailVO {
    private Integer quId;
    private Integer userId;
    private Integer examId;
    private String quTitle;
    private String quImg;
    private String answer;
    private String refAnswer;
    private Integer totalScore;

}
