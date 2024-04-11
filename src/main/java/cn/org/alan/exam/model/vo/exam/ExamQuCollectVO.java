package cn.org.alan.exam.model.vo.exam;

import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.Option;
import lombok.Data;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/10 9:54 AM
 */
@Data
public class ExamQuCollectVO {
    private static final long serialVersionUID = 1L;

    /**
     * 图片
     */
    private String image;

    /**
     * 题目内容
     */
    private String content;
    /**
     * 答案内容
     */
    private List<Option> answerList;
    /**
     * 回答内容
     */
    private ExamQuAnswer examQuAnswer;
}
