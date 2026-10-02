package org.dromara.exam.manage.domain.bo;

import org.dromara.exam.manage.domain.Exam;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;

/**
 * 考试主业务对象 exam
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Exam.class, reverseConvertGenerate = false)
public class ExamBo extends BaseEntity {

    /**
     * 考试ID
     */
    @NotNull(message = "考试ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 考试名称
     */
    @NotBlank(message = "考试名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String examName;

    /**
     * 考试描述说明
     */
    private String examDesc;

    /**
     * 关联试卷ID，paper表主键
     */
    @NotNull(message = "关联试卷不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long paperId;

    /**
     * 任务类型：1正式考试 / 2练习考试，取字典 exam_type
     *
     * <p>决定后面整套规则该怎么填，所以放在第一步让用户先选
     */
    @NotBlank(message = "考试类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String examType;

    /**
     * 考试开始时间
     *
     * <p>练习考试允许不填（长期有效），必填校验放在 Service 里按考试类型判断
     */
    private Date startTime;

    /**
     * 考试结束时间
     *
     * <p>同开始时间，练习考试允许不填
     */
    private Date endTime;

    /**
     * 本场考试限时(分钟)，0不限时；限时属于活动规则，只在本场考试上配置
     */
    private Long duration;

    /**
     * 是否允许迟到入场 0否 1是
     */
    @NotNull(message = "是否允许迟到入场不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long allowLate;

    /**
     * 允许迟到多少分钟，超过无法进入
     */
    private Long lateMinute;

    /**
     * 是否允许重考 0否 1是
     */
    @NotNull(message = "是否允许重考不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long allowRetry;

    /**
     * 单个考生最大重考次数
     */
    private Long maxRetryCount;

    /**
     * 答案展示 none不展示 / after_submit交卷后 / after_exam考试结束
     */
    @NotBlank(message = "答案展示方式不能为空", groups = { AddGroup.class, EditGroup.class })
    private String showAnswerMode;

    /**
     * 客观题部分得分开关 0必须全对才给分 1启用部分得分（多选漏选 / 填空部分空）
     */
    private String partialScore;

    /**
     * 部分正确时的得分比例（%），100按命中比例 / 50一律半数
     */
    private Integer partialScoreRate;

    /**
     * 防作弊配置：切屏次数、禁止复制粘贴、摄像头抓拍、全屏限制等
     */
    private String antiCheatConfig;

    /**
     * 及格证书模板ID，为空表示本场考试不发证书
     */
    private Long certId;

    /**
     * 考生准入类型 white白名单 / public公开链接
     */
    @NotBlank(message = "参加方式不能为空", groups = { AddGroup.class, EditGroup.class })
    private String participantType;

    /**
     * 公开考试参与密码，public模式生效，为空无密码
     */
    private String joinPassword;

    /**
     * 公开考试链接有效期，NULL和考试结束时间一致
     */
    private Date joinExpireTime;

    /**
     * not_start未开始 / ongoing进行中 / finished已结束 / archived归档
     */
    @NotBlank(message = "状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     * 公开考试的加入码，留空时由后端自动生成
     */
    private String joinCode;

    /**
     * 创建人ID，不传时后端取当前登录用户填充
     */
    private Long creatorId;


}
