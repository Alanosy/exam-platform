package cn.org.alan.exam.model.vo.record;

import cn.org.alan.exam.model.entity.Option;
import lombok.Data;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/30 12:32 PM
 */
@Data
public class ExerciseRecordDetailVO {
    // 1、题干 2、选项 3、自己的答案 4、正确的答案 5、是否正确 6、试题分析
    private String title;
    private List<Option> option;
    private String myOption;
    private String rightOption;
    private Integer isRight;
    private String analyse;
    /**
     * 题干图片
     */
    private String image;
    /**
     * 试题类型
     */
    private Integer quType;
}
