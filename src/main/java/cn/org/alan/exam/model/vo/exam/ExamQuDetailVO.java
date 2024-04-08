package cn.org.alan.exam.model.vo.exam;

import cn.org.alan.exam.model.entity.Option;
import lombok.Data;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/1 3:39 PM
 */
@Data
public class ExamQuDetailVO {
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
    List<Option> answerList;

}
