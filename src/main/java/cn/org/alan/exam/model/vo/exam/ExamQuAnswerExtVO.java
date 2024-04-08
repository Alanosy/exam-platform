package cn.org.alan.exam.model.vo.exam;

import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/7 11:10 PM
 */
@Data
public class ExamQuAnswerExtVO {
    private static final long serialVersionUID = 1L;
    private Integer id;

    /**
     * 试题图片
     */
    private String image;

    /**
     * 答案内容
     */
    private String content;
    /**
     * 顺序
     */
    private Integer sort;
}
