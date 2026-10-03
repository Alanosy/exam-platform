package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.Question;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.math.BigDecimal;

/**
 * 试题主业务对象 question
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Question.class, reverseConvertGenerate = false)
public class QuestionBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 所属题库ID
     */
    @NotNull(message = "所属题库ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long bankId;

    /**
     * 题干富文本
     */
    @NotBlank(message = "题干富文本不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题
     */
    @NotBlank(message = "题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String questionType;

    /**
     * 难度 easy简单 medium中等 hard困难
     */
    private String difficulty;

    /**
     * 题目默认分值
     *
     * <p>数据库为 decimal(5,2)，支持 5.5 这类带小数的分值
     */
    private BigDecimal score;

    /**
     * 试题解析富文本
     */
    private String analysis;

    /**
     * 参考答案JSON，不同题型结构不同
     */
    private String answer;

    /**
     * 题目创建人ID
     *
     * <p>不强制前端传递，为空时由后端取当前登录用户填充
     */
    private Long createUser;

    /**
     * 0草稿 1启用 2废弃
     */
    @NotNull(message = "0草稿 1启用 2废弃不能为空", groups = { AddGroup.class, EditGroup.class })
    private String  status;

    /**
     * 选项列表，新增试题时随试题一并提交（仅客观题需要）
     *
     * <p>富文本（题干 / 选项 / 解析）里的图片以 {@code <img src="url">} 的形式直接存在各自的 HTML 里，
     * 不需要额外提交媒体列表。
     */
    @Valid
    private List<QuestionOptionSaveBo> options;

    /**
     * 知识点ID列表（两级：章节 → 知识点）
     *
     * <p>新增 / 编辑时随试题一并提交，全量覆盖 exam_question_knowledge。
     * 传 null 表示不动关联（局部更新接口用），传空数组表示清空。
     */
    private List<Long> knowledgeIds;

}
