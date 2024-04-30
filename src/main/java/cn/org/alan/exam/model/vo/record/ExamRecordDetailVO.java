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
    private String title;
    private List<Option> option;
    private String myOption;
    private String rightOption;
    private Integer isRight;
    private String analyse;
}
