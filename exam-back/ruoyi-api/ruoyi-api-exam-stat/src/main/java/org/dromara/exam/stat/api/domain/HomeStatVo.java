package org.dromara.exam.stat.api.domain;

import lombok.Data;
import org.dromara.exam.answer.api.domain.RecordTrendVo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页总览
 *
 * <p>一次请求把首页要显示的数字全部带回去，前端不必为了凑一张 Dashboard 打七八个接口。
 * 拆两块：考试侧的量（统计服务自己算）+ 答卷侧的量（向答题服务要）。
 *
 * <p>所有数字都是「已经算好」的：比例已经乘过 100 并量化到一位小数，
 * 前端只负责渲染，不参与计算，避免两套口径。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class HomeStatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试总数 */
    private Long examTotal = 0L;

    /** 进行中：当前时间落在考试时间段内 */
    private Long examOngoing = 0L;

    /** 今天有安排的考试场次 */
    private Long examToday = 0L;

    /** 已结束的考试数 */
    private Long examFinished = 0L;

    /** 累计答卷数（含答题中） */
    private Long recordTotal = 0L;

    /** 正在答题数 */
    private Long answering = 0L;

    /** 今日交卷数 */
    private Long todaySubmit = 0L;

    /** 参考人数（去重） */
    private Long examineeCount = 0L;

    /** 及格率，百分数，保留一位小数（只统计已交卷的卷子） */
    private BigDecimal passRate = BigDecimal.ZERO;

    /** 平均分，保留一位小数 */
    private BigDecimal avgScore = BigDecimal.ZERO;

    /** 近7天交卷趋势，按日期升序 */
    private List<RecordTrendVo> trend = new ArrayList<>();

    /** 今天的考试安排，按开始时间升序 */
    private List<HomeExamVo> todayExams = new ArrayList<>();

    /** 按交卷量倒序的考试热度榜 */
    private List<HomeExamRankVo> examRank = new ArrayList<>();

}
