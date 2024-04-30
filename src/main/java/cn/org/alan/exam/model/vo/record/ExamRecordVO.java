package cn.org.alan.exam.model.vo.record;

import cn.org.alan.exam.model.entity.Option;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/30 11:17 AM
 */
@Data
public class ExamRecordVO {

    /**
     * id    考试表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试名称
     */
    private String title;

    /**
     * 考试时长
     */
    private Integer examDuration;

    /**
     * 及格分
     */
    private Integer passedScore;

    /**
     * 总分数
     */
    private Integer grossScore;

    /**
     * 最大切屏次数
     */
    private Integer maxCount;

    /**
     * 创建者id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    /**
     * 证书id
     */
    private Integer certificateId;

    /**
     * 单选题数量
     */
    private Integer radioCount;

    /**
     * 单选题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer radioScore;

    /**
     * 多选题数量
     */
    private Integer multiCount;

    /**
     * 多选题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer multiScore;

    /**
     * 判断题数量
     */
    private Integer judgeCount;

    /**
     * 判断题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer judgeScore;

    /**
     * 简答题数量
     */
    private Integer saqCount;

    /**
     * 简答题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer saqScore;

    /**
     * 开始时间     YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime startTime;

    /**
     * 结束时间     YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime endTime;

    /**
     * 创建时间     YYYY-MM-DD hh:mm:ss
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;



}
