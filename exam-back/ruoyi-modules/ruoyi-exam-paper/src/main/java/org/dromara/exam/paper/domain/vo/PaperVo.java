package org.dromara.exam.paper.domain.vo;

import java.util.Date;
import java.util.List;

import org.dromara.exam.paper.domain.Paper;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import cn.idev.excel.annotation.ExcelIgnore;
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
     * 创建人ID，新增时由后端取当前登录用户填充
     */
    @ExcelProperty(value = "创建人ID")
    private Long creatorId;

    /**
     * 创建人名称，由 creatorId 翻译得到，不落库
     */
    @ExcelProperty(value = "创建人")
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "creatorId")
    private String creatorName;

    /**
     * 试卷分类，取字典 paper_category 的字典值
     */
    @ExcelProperty(value = "试卷分类")
    private String category;

    /**
     * 默认单题分值
     */
    @ExcelProperty(value = "默认单题分值")
    private Long defaultScore;

    /**
     * 是否开启题目乱序 0否 1是
     */
    @ExcelProperty(value = "题目乱序")
    private String questionShuffle;

    /**
     * 是否开启选项乱序 0否 1是
     */
    @ExcelProperty(value = "选项乱序")
    private String optionShuffle;

    /**
     * 客观题是否自动判分 0否 1是
     */
    @ExcelProperty(value = "客观题自动判分")
    private String autoJudge;

    /**
     * 主观题是否人工阅卷 0否 1是
     */
    @ExcelProperty(value = "主观题人工阅卷")
    private String manualReview;

    /**
     * 是否支持部分得分 0否 1是
     */
    @ExcelProperty(value = "部分得分")
    private String partialScore;

    /**
     * 答错是否扣分 0否 1是
     */
    @ExcelProperty(value = "答错扣分")
    private String wrongDeduct;

    /**
     * 可见范围 SELF仅自己可编辑 / SHARED共享给其他管理员
     */
    @ExcelProperty(value = "可见范围")
    private String shareScope;

    /**
     * 本试卷已选试题明细，详情接口随试卷一并返回，按 sort 升序
     */
    @ExcelIgnore
    private List<PaperQuestionVo> questions;


}
