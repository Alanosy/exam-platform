package org.dromara.exam.manage.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试情况：一行 = 一个考生的一场作答
 *
 * <p>这里的数全部实时来自答卷记录（exam_record），不读任何预计算汇总表，
 * 所以打开就有数据，不需要先跑统计任务。
 *
 * <p>同时充当导出的行对象：加了 {@code @ExcelProperty} 的字段才会出现在 Excel 里
 * （类上的 {@code @ExcelIgnoreUnannotated} 保证这点），同一个 VO 既给表格也给导出，
 * 避免出现「页面上看得见、导出却少一列」。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@ExcelIgnoreUnannotated
public class ExamSituationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷记录ID，前端「查看答卷」用 */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号（登录名） */
    @ExcelProperty(value = "考生账号")
    private String account;

    /** 考生姓名（昵称） */
    @ExcelProperty(value = "考生姓名")
    private String nickName;

    /** 所属部门 */
    @ExcelProperty(value = "所属部门")
    private String deptName;

    /** 第几次参加，从1开始 */
    @ExcelProperty(value = "第几次")
    private Integer attemptNo;

    /** answering答题中 / submitted已交卷 / expired超时作废，给前端 dict-tag 取值用 */
    private String status;

    /** 状态的中文名，导出用；前端不要用它做逻辑判断 */
    @ExcelProperty(value = "状态")
    private String statusLabel;

    /** 开考时间 */
    @ExcelProperty(value = "开考时间")
    private Date startTime;

    /** 交卷时间，答题中为空 */
    @ExcelProperty(value = "交卷时间")
    private Date submitTime;

    /** 用时（秒） */
    private Integer usedSeconds;

    /** 用时（mm:ss），导出用；答题中为空 */
    @ExcelProperty(value = "用时")
    private String usedTimeLabel;

    /** 题目总数 */
    @ExcelProperty(value = "题目数")
    private Integer questionCount;

    /** 已作答题目数 */
    @ExcelProperty(value = "已答题数")
    private Integer answeredCount;

    /** 客观题得分 */
    @ExcelProperty(value = "客观题分")
    private BigDecimal objectiveScore;

    /** 主观题得分 */
    private BigDecimal subjectiveScore;

    /** 主观题分的展示值：待阅时是「待阅」，否则是分数 */
    @ExcelProperty(value = "主观题分")
    private String subjectiveScoreLabel;

    /** 总分 */
    @ExcelProperty(value = "总分")
    private BigDecimal totalScore;

    /** 及格分 */
    @ExcelProperty(value = "及格分")
    private BigDecimal passScore;

    /** 是否及格 0否 1是；主观题没阅完时算不出来，为 null */
    private Long passed;

    /** 及格的中文展示：待阅时是「待阅」，否则是「及格 / 不及格」 */
    @ExcelProperty(value = "是否及格")
    private String passedLabel;

    /** 是否超时系统自动交卷 */
    private Long autoSubmit;

    /** 自动交卷的中文展示 */
    @ExcelProperty(value = "是否被自动交卷")
    private String autoSubmitLabel;

    /** 这场的主观题阅完没有：true 表示还有待阅，成绩没定 */
    private Boolean pendingMark;
}
