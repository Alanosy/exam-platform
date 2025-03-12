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
    /**
     * 题目ID
     */
    private Integer id;
    /**
     * 图片
     */
    private String image;
    /**
     * 题干
     */
    private String title;
    /**
     * 选项
     */
    private List<Option> option;
    /**
     * 我的答案
     */
    private String myOption;

    /**
     * 试题类型
     */
    private Integer quType;
}
