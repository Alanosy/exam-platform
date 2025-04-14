package cn.org.alan.exam.model.vo.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author 赵浩森
 * @Version
 * @Date 2024/4/13 10:44
 */
@Data
public class QuestionScoreVO {

    /**
     * 题目ID
     */
    @JsonProperty("题目ID")
    private String questionId;

    /**
     * 题目内容
     */
    @JsonProperty("题目内容")
    private String questionContent;

    /**
     * 题目总分
     */
    @JsonProperty("题目总分")
    private Integer totalScore;

    /**
     * 标准答案
     */
    @JsonProperty("标准答案")
    private String qusetionAnswer;

    /**
     * 待评分答案
     */
    @JsonProperty("待评分答案")
    private String userAnswer;


}