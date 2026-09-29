package org.dromara.exam.paper.domain.bo;

import org.dromara.exam.paper.domain.Paper;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;

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
     * 考试时长(分钟)，0代表不限时
     */
    private Long timeLimit;

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
     * 分享链接过期时间，NULL永久有效
     */
    private Date shareExpireTime;

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
     * 创建人ID
     */
    @NotNull(message = "创建人ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creatorId;


}
