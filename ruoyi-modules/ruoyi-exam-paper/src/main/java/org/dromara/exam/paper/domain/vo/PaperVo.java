package org.dromara.exam.paper.domain.vo;

import java.util.Date;

import org.dromara.exam.paper.domain.Paper;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 试卷主视图对象 paper
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Paper.class)
public class PaperVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 试卷主键ID
     */
    @ExcelProperty(value = "试卷主键ID")
    private Long id;

    /**
     * 试卷名称
     */
    @ExcelProperty(value = "试卷名称")
    private String paperName;

    /**
     * 试卷描述
     */
    @ExcelProperty(value = "试卷描述")
    private String paperDesc;

    /**
     * 组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)
     */
    @ExcelProperty(value = "组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)")
    private String paperType;

    /**
     * 试卷总分
     */
    @ExcelProperty(value = "试卷总分")
    private Long totalScore;

    /**
     * 及格分数
     */
    @ExcelProperty(value = "及格分数")
    private Long passScore;

    /**
     * 考试时长(分钟)，0代表不限时
     */
    @ExcelProperty(value = "考试时长(分钟)，0代表不限时")
    private Long timeLimit;

    /**
     * 可见性 private私有 / public公开
     */
    @ExcelProperty(value = "可见性 private私有 / public公开")
    private String visibility;

    /**
     * 公开分享密码，公开模式生效，空则无密码
     */
    @ExcelProperty(value = "公开分享密码，公开模式生效，空则无密码")
    private String sharePassword;

    /**
     * 分享链接过期时间，NULL永久有效
     */
    @ExcelProperty(value = "分享链接过期时间，NULL永久有效")
    private Date shareExpireTime;

    /**
     * 随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}
     */
    @ExcelProperty(value = "随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}")
    private String randomRule;

    /**
     * draft草稿 / ready已组卷 / archived归档
     */
    @ExcelProperty(value = "draft草稿 / ready已组卷 / archived归档")
    private String status;

    /**
     * 创建人ID
     */
    @ExcelProperty(value = "创建人ID")
    private Long creatorId;


}
