package org.dromara.exam.manage.domain.vo;

import java.util.Date;

import org.dromara.exam.manage.domain.Exam;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 考试主视图对象 exam
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Exam.class)
public class ExamVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 考试ID
     */
    @ExcelProperty(value = "考试ID")
    private Long id;

    /**
     * 考试名称
     */
    @ExcelProperty(value = "考试名称")
    private String examName;

    /**
     * 考试描述说明
     */
    @ExcelProperty(value = "考试描述")
    private String examDesc;

    /**
     * 关联试卷ID
     */
    @ExcelProperty(value = "关联试卷ID")
    private Long paperId;

    /**
     * 任务类型：1正式考试 / 2练习考试
     */
    @ExcelProperty(value = "考试类型")
    private String examType;

    /**
     * 考试开始时间
     */
    @ExcelProperty(value = "考试开始时间")
    private Date startTime;

    /**
     * 考试结束时间
     */
    @ExcelProperty(value = "考试结束时间")
    private Date endTime;

    /**
     * 本场考试限时(分钟)，0不限时；限时属于活动规则，只在本场考试上配置
     */
    @ExcelProperty(value = "考试限时(分钟)")
    private Long duration;

    /**
     * 是否允许迟到入场 0否 1是
     */
    @ExcelProperty(value = "是否允许迟到入场")
    private Long allowLate;

    /**
     * 允许迟到多少分钟，超过无法进入
     */
    @ExcelProperty(value = "允许迟到分钟数")
    private Long lateMinute;

    /**
     * 是否允许重考 0否 1是
     */
    @ExcelProperty(value = "是否允许重考")
    private Long allowRetry;

    /**
     * 单个考生最大重考次数
     */
    @ExcelProperty(value = "单个考生最大重考次数")
    private Long maxRetryCount;

    /**
     * 答案展示 none不展示 / after_submit交卷后 / after_exam考试结束
     */
    @ExcelProperty(value = "答案展示方式")
    private String showAnswerMode;

    /**
     * 防作弊配置：切屏次数、禁止复制粘贴、摄像头抓拍、全屏限制等
     */
    @ExcelProperty(value = "防作弊配置")
    private String antiCheatConfig;

    /**
     * 考生准入类型 white白名单 / public公开链接
     */
    @ExcelProperty(value = "参加方式")
    private String participantType;

    /**
     * 公开考试参与密码，public模式生效，为空无密码
     */
    @ExcelProperty(value = "参与密码")
    private String joinPassword;

    /**
     * 公开考试链接有效期，NULL和考试结束时间一致
     */
    @ExcelProperty(value = "链接有效期")
    private Date joinExpireTime;

    /**
     * not_start未开始 / ongoing进行中 / finished已结束 / archived归档
     */
    @ExcelProperty(value = "状态")
    private String status;

    /**
     * 公开考试的加入码，用于拼加入链接，为空表示非公开考试
     */
    @ExcelProperty(value = "加入码")
    private String joinCode;

    /**
     * 考试创建人ID，新增时由后端取当前登录用户填充
     */
    @ExcelProperty(value = "创建人ID")
    private Long creatorId;

    /**
     * 创建人名称，由 creatorId 翻译得到，不落库
     */
    @ExcelProperty(value = "创建人")
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "creatorId")
    private String creatorName;


}
