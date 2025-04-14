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
    // 试题ID
    private Integer quId;
    // 用户ID
    private Integer userId;
    // 试卷ID
    private Integer examId;
    // 试题标题
    private String quTitle;
    // 试题图片
    private String quImg;
    private String answer;
    private String refAnswer;
    private Integer correctScore;
    private String aiReason;
    private Integer totalScore;

}
