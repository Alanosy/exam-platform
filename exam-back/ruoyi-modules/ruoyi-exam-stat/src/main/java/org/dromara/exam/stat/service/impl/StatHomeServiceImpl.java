package org.dromara.exam.stat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RecordExamStatVo;
import org.dromara.exam.answer.api.domain.RecordStatVo;
import org.dromara.exam.answer.api.domain.RecordTrendVo;
import org.dromara.exam.stat.api.domain.HomeExamRankVo;
import org.dromara.exam.stat.api.domain.HomeExamVo;
import org.dromara.exam.stat.api.domain.HomeStatVo;
import org.dromara.exam.stat.domain.StatExam;
import org.dromara.exam.stat.mapper.StatExamMapper;
import org.dromara.exam.stat.service.IStatHomeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页统计实现
 *
 * <p>数据来源天然分两边：考试表在本服务能直接查，答卷表在答题服务里隔着 Dubbo。
 * 原则是**答卷侧的量允许降级，挂了就按 0 显示** —— 首页是每天打开的第一个页面，
 * 不能因为答题服务重启就整页打不开，剩下的模块（今日考试安排、系统说明）还得照常显示。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatHomeServiceImpl implements IStatHomeService {

    /** 今日考试安排最多显示几条，够看就行，多了要滚动反而记不住 */
    private static final int TODAY_EXAM_LIMIT = 6;

    /** 热度榜条数 */
    private static final int EXAM_RANK_LIMIT = 5;

    private final StatExamMapper statExamMapper;

    @DubboReference
    private RemoteExamAnswerService remoteExamAnswerService;

    @Override
    public HomeStatVo homeOverview() {
        HomeStatVo vo = new HomeStatVo();
        fillExamStat(vo, new Date());
        fillRecordStat(vo);
        return vo;
    }

    /** 考试侧：规模、状态分布、今日安排 */
    private void fillExamStat(HomeStatVo vo, Date now) {
        vo.setExamTotal(statExamMapper.selectCount(Wrappers.lambdaQuery(StatExam.class)));
        vo.setExamOngoing(statExamMapper.selectCount(Wrappers.lambdaQuery(StatExam.class)
            .le(StatExam::getStartTime, now)
            .ge(StatExam::getEndTime, now)));
        vo.setExamFinished(statExamMapper.selectCount(Wrappers.lambdaQuery(StatExam.class)
            .lt(StatExam::getEndTime, now)));
        vo.setExamToday(statExamMapper.selectCount(Wrappers.lambdaQuery(StatExam.class)
            .between(StatExam::getStartTime, DateUtil.beginOfDay(now), DateUtil.endOfDay(now))));

        List<StatExam> todayList = statExamMapper.selectList(Wrappers.lambdaQuery(StatExam.class)
            .between(StatExam::getStartTime, DateUtil.beginOfDay(now), DateUtil.endOfDay(now))
            .orderByAsc(StatExam::getStartTime));
        List<HomeExamVo> exams = new ArrayList<>(Math.min(TODAY_EXAM_LIMIT, todayList.size()));
        for (StatExam exam : todayList.stream().limit(TODAY_EXAM_LIMIT).toList()) {
            HomeExamVo item = new HomeExamVo();
            item.setExamId(exam.getId());
            item.setExamName(exam.getExamName());
            item.setExamType(exam.getExamType());
            item.setStartTime(exam.getStartTime());
            item.setEndTime(exam.getEndTime());
            item.setStatus(exam.getStatus());
            exams.add(item);
        }
        vo.setTodayExams(exams);
    }

    /** 答卷侧：总量、趋势、热度榜，全部走 Dubbo，异常降级为 0 / 空列表 */
    private void fillRecordStat(HomeStatVo vo) {
        RecordStatVo record;
        try {
            record = remoteExamAnswerService.statRecords();
        } catch (Exception e) {
            log.warn("查询答卷统计失败，首页该项降级为 0：{}", e.getMessage());
            record = new RecordStatVo();
        }
        if (ObjectUtil.isNull(record)) {
            record = new RecordStatVo();
        }
        vo.setRecordTotal(record.getTotal());
        vo.setAnswering(record.getAnswering());
        vo.setTodaySubmit(record.getTodaySubmit());
        vo.setExamineeCount(record.getExamineeCount());
        vo.setPassRate(record.getPassRate());
        vo.setAvgScore(record.getAvgScore());

        vo.setTrend(safeTrend());
        vo.setExamRank(fillExamName(safeRank()));
    }

    /** 热度榜只有考试ID，名字要回考试表补；考试被删了就留空，不整条丢掉统计 */
    private List<HomeExamRankVo> fillExamName(List<RecordExamStatVo> rank) {
        if (CollUtil.isEmpty(rank)) {
            return new ArrayList<>();
        }
        List<Long> examIds = rank.stream().map(RecordExamStatVo::getExamId).filter(ObjectUtil::isNotNull).toList();
        Map<Long, String> nameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(examIds)) {
            List<StatExam> exams = statExamMapper.selectList(Wrappers.lambdaQuery(StatExam.class).in(StatExam::getId, examIds));
            for (StatExam exam : exams) {
                nameMap.put(exam.getId(), exam.getExamName());
            }
        }
        List<HomeExamRankVo> list = new ArrayList<>(rank.size());
        for (RecordExamStatVo item : rank) {
            HomeExamRankVo vo = new HomeExamRankVo();
            vo.setExamId(item.getExamId());
            vo.setExamName(nameMap.getOrDefault(item.getExamId(), "已删除的考试"));
            vo.setSubmitCount(item.getSubmitCount());
            vo.setPassCount(item.getPassCount());
            vo.setPassRate(item.getPassRate());
            vo.setAvgScore(item.getAvgScore());
            list.add(vo);
        }
        return list;
    }

    private List<RecordTrendVo> safeTrend() {
        try {
            List<RecordTrendVo> trend = remoteExamAnswerService.trendSubmit(7);
            return CollUtil.isEmpty(trend) ? new ArrayList<>() : trend;
        } catch (Exception e) {
            log.warn("查询交卷趋势失败，首页趋势图降级为空：{}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<RecordExamStatVo> safeRank() {
        try {
            List<RecordExamStatVo> rank = remoteExamAnswerService.rankByExam(EXAM_RANK_LIMIT);
            return CollUtil.isEmpty(rank) ? new ArrayList<>() : rank;
        } catch (Exception e) {
            log.warn("查询考试热度榜失败，首页该项降级为空：{}", e.getMessage());
            return new ArrayList<>();
        }
    }

}
