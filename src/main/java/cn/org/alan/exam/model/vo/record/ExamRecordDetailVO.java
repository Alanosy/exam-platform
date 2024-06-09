package cn.org.alan.exam.model.vo.record;

import cn.org.alan.exam.model.entity.Option;
import lombok.Data;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/30 11:44 AM
 */
@Data
public class ExamRecordDetailVO {
    // 1、题干 2、选项 3、自己的答案 4、正确的答案 5、是否正确 6、试题分析
    /**
     * 题干
     */
    private String title;
    /**
     * 题干图片
     */
    private String image;
    /**
     * 选项
     */
    private List<Option> option;
    /**
     * 我的答案
     */
    private String myOption;
    /**
     * 正确答案
     */
    private String rightOption;
    /**
     * 是否正确
     */
    private Integer isRight;
    /**
     * 试题分析
     */
    private String analyse;
    /**
     * 试题类型
     */
    private Integer quType;

}
