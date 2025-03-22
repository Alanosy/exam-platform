package cn.org.alan.exam.model.form.question;

import cn.org.alan.exam.common.group.QuestionGroup;
import cn.org.alan.exam.model.entity.Option;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 试卷请求体
 *
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

    /**
     * 题库ID
     */
    @NotNull(message = "题库id(repoId)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    private Integer repoId;

    /**
     * 选项列表
     */
    private List<Option> options;

}
