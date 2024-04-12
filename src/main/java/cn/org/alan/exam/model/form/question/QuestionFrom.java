package cn.org.alan.exam.model.form.question;

import cn.org.alan.exam.common.group.QuestionGroup;
import cn.org.alan.exam.model.entity.Option;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/1 11:12
 */
@Data
public class QuestionFrom {

    private Integer id;

    /**
     * 试题类型
     */
    @NotNull(message = "试题类型(quType)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    @Min(value = 1, message = "试题类型(quType)只能是：1单选2多选3判断4简答", groups = QuestionGroup.QuestionAddGroup.class)
    @Max(value = 4, message = "试题类型(quType)只能是：1单选2多选3判断4简答", groups = QuestionGroup.QuestionAddGroup.class)
    private Integer quType;

    /**
     * 试题图片
     */
    private String image;
    private String analysis;

    /**
     * 题干
     */
    @NotBlank(message = "题干(content)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @NotNull(message = "题库id(repoId)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    private Integer repoId;

    /**
     * 选项列表
     */
//    @NotNull(message = "选项列表(options)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
//    @Size(min = 2, message = "选项列表(options)至少需要两个选项", groups = QuestionGroup.QuestionAddGroup.class)
//    @Valid
    private List<Option> options;

    /**
     * 简答题答案内容
     */
//    @NotBlank(message = "简答题答案不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    private String answer;


    /**
     * 校验简答题
     *
     * @return 是否通过检验
     */
//    @AssertTrue(message = "简答题答案不能为空，或题目类型不为简答题的试题选项不能少于两个",
//            groups = QuestionGroup.QuestionAddGroup.class)
//    public boolean judgeQuType() {
//        return (quType == 4 && StringUtils.isNotBlank(answer)) ||
//                (quType != 4 && Objects.nonNull(options) && options.size() > 2);
//    }


}
