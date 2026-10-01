package org.dromara.exam.paper.domain.bo;

import org.dromara.exam.paper.domain.Paper;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

/**
 * 试卷主业务对象 paper
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Paper.class, reverseConvertGenerate = false)
public class PaperBo extends BaseEntity {

    /**
     * 试卷主键ID
     */
    @NotNull(message = "试卷主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 试卷名称
     */
    @NotBlank(message = "试卷名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String paperName;

    /**
     * 试卷描述
     */
    private String paperDesc;

    /**
     * 组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)
     */
    @NotBlank(message = "组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String paperType;

    /**
     * 试卷总分
     */
    private Long totalScore;

    /**
     * 及格分数
     */
    private Long passScore;

    /**
     * 可见性 private私有 / public公开
     */
    @NotBlank(message = "可见性 private私有 / public公开不能为空", groups = { AddGroup.class, EditGroup.class })
    private String visibility;

    /**
     * 公开分享密码，公开模式生效，空则无密码
     */
    private String sharePassword;

    /**
     * 随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}
     */
    private String randomRule;

    /**
     * draft草稿 / ready已组卷 / archived归档
     */
    @NotBlank(message = "draft草稿 / ready已组卷 / archived归档不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     * 创建人ID，不传时后端取当前登录用户填充
     */
    private Long creatorId;

    /**
     * 试卷分类，取字典 paper_category 的字典值
     */
    private String category;

    /**
     * 默认单题分值，手动选题未单独指定分值时使用
     */
    private Long defaultScore;

    /**
     * 是否开启题目乱序 0否 1是
     */
    private String questionShuffle;

    /**
     * 是否开启选项乱序 0否 1是
     */
    private String optionShuffle;

    /**
     * 客观题是否自动判分 0否 1是
     */
    private String autoJudge;

    /**
     * 主观题是否人工阅卷 0否 1是
     */
    private String manualReview;

    /**
     * 是否支持部分得分 0否 1是
     */
    private String partialScore;

    /**
     * 答错是否扣分 0否 1是
     */
    private String wrongDeduct;

    /**
     * 可见范围 SELF仅自己可编辑 / SHARED共享给其他管理员
     */
    private String shareScope;

    /**
     * 本试卷的试题明细，组卷接口 /paper/save 使用，按数组顺序落 sort
     */
    @Valid
    private List<PaperQuestionSaveBo> questions;


}
