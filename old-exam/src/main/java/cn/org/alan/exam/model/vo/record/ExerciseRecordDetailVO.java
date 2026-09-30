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
    // 题干
    private String title;

    // 选项
    private List<Option> option;

    // 自己的答案
    private String myOption;

    // 正确的答案
    private String rightOption;

    // 是否正确
    private Integer isRight;

    // 试题分析
    private String analyse;

    // 题干图片
    private String image;

    // 试题类型
    private Integer quType;
}
