package cn.org.alan.exam.model.form.exam;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/5 11:15 PM
 */
@Data
public class ExamAddForm {
    private Integer id;
    //考试标题
    private String title;
    //考试状态
    private Integer status;
    //考试时长
    private Integer examDuration;
    //总分数
    private Integer grossScore;
    //及格分
    private Integer passedScore;
    //开始时间
    private LocalDateTime startTime;
    //结束时间
    private LocalDateTime endTime;
    //考试班级
    private String gradeids;
    //题库id
    private Integer repoId;
    //证书id
    private Integer certificateId;
    //单选题数量
    private Integer radioCount;
    //单选题每题分数
    private Integer radioScore;
    //多选题数量
    private Integer multiCount;
    //多选题分数
    private Integer multiScore;
    //判断题数量
    private Integer judgeCount;
    //判断题分数
    private Integer judgeScore;
    //简答题数量
    private Integer saqCount;
    //简答题分数
    private Integer saqScorre;



}
