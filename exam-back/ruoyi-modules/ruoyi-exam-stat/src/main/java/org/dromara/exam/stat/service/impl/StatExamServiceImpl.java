package org.dromara.exam.stat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RemoteAnswerVo;
import org.dromara.exam.answer.api.domain.RemoteRecordVo;
import org.dromara.exam.stat.domain.StatExamKnowledge;
import org.dromara.exam.stat.domain.StatExamQuestion;
import org.dromara.exam.stat.domain.StatExamQuestionOption;
import org.dromara.exam.stat.domain.StatExamScoreSegment;
import org.dromara.exam.stat.domain.StatExamSummary;
import org.dromara.exam.stat.domain.StatExamUser;
import org.dromara.exam.stat.domain.ref.ExamRef;
import org.dromara.exam.stat.domain.ref.MarkTaskRef;
import org.dromara.exam.stat.domain.ref.PaperQuestionRef;
import org.dromara.exam.stat.domain.ref.QuestionBankRef;
import org.dromara.exam.stat.domain.ref.QuestionCategoryRef;
import org.dromara.exam.stat.domain.ref.QuestionOptionRef;
import org.dromara.exam.stat.domain.ref.QuestionRef;
import org.dromara.exam.stat.domain.bo.StatExamQueryBo;
import org.dromara.exam.stat.domain.bo.StatQuestionQueryBo;
import org.dromara.exam.stat.domain.bo.StatUserQueryBo;
import org.dromara.exam.stat.domain.vo.StatAnswerVo;
import org.dromara.exam.stat.domain.vo.StatDashboardVo;
import org.dromara.exam.stat.domain.vo.StatExamRowVo;
import org.dromara.exam.stat.domain.vo.StatKnowledgeVo;
import org.dromara.exam.stat.domain.vo.StatOptionVo;
import org.dromara.exam.stat.domain.vo.StatOverviewVo;
import org.dromara.exam.stat.domain.vo.StatQuestionVo;
import org.dromara.exam.stat.domain.vo.StatSegmentVo;
import org.dromara.exam.stat.domain.vo.StatUserVo;
import org.dromara.exam.stat.mapper.ExamRefMapper;
import org.dromara.exam.stat.mapper.ExamUserRefMapper;
import org.dromara.exam.stat.mapper.MarkTaskRefMapper;
import org.dromara.exam.stat.mapper.PaperQuestionRefMapper;
import org.dromara.exam.stat.mapper.QuestionBankRefMapper;
import org.dromara.exam.stat.mapper.QuestionCategoryRefMapper;
import org.dromara.exam.stat.mapper.QuestionOptionRefMapper;
import org.dromara.exam.stat.mapper.QuestionRefMapper;
import org.dromara.exam.stat.mapper.StatExamKnowledgeMapper;
import org.dromara.exam.stat.mapper.StatExamQuestionMapper;
import org.dromara.exam.stat.mapper.StatExamQuestionOptionMapper;
import org.dromara.exam.stat.mapper.StatExamScoreSegmentMapper;
import org.dromara.exam.stat.mapper.StatExamSummaryMapper;
import org.dromara.exam.stat.mapper.StatExamUserMapper;
import org.dromara.exam.stat.service.IStatExamService;
import org.dromara.system.api.RemoteDeptService;
import org.dromara.system.api.RemoteUserService;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 考试统计
 *
 * <p>三条铁律，改这个类的任何逻辑前先读一遍：
 * <ol>
 *     <li><b>待阅卷不算数</b>：主观题没阅完的答卷一律不计入任何平均分、及格率、排名。
 *         交卷那一刻总分只有客观题分，早算进去数字会一直跳，业务方就不信这个系统了。</li>
 *     <li><b>重考只算最后一次</b>：一个考生在多份答卷里取 attemptNo 最大的那份。</li>
 *     <li><b>半对不算答对</b>：开了部分得分后 correct=3 是「部分正确」，
 *         既不能进 correctCount 也不能进 wrongCount，单独记 partialCount。</li>
 * </ol>
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StatExamServiceImpl implements IStatExamService {

    /** 客观题题型：这些能自动判分，才有所谓「正确率」 */
    private static final Set<String> OBJECTIVE_TYPES = Set.of("SINGLE", "MULTIPLE", "JUDGE", "BLANK", "MATCH");

    /** 区分度：高/低分组各取总人数的前 27% */
    private static final double GROUP_27 = 0.27;

    private final StatExamSummaryMapper summaryMapper;
    private final StatExamScoreSegmentMapper segmentMapper;
    private final StatExamQuestionMapper questionMapper;
    private final StatExamQuestionOptionMapper optionMapper;
    private final StatExamUserMapper userMapper;
    private final StatExamKnowledgeMapper knowledgeMapper;
    private final ExamRefMapper examRefMapper;
    private final ExamUserRefMapper examUserRefMapper;
    private final PaperQuestionRefMapper paperQuestionRefMapper;
    private final QuestionRefMapper questionRefMapper;
    private final QuestionOptionRefMapper questionOptionRefMapper;
    private final QuestionBankRefMapper questionBankRefMapper;
    private final QuestionCategoryRefMapper questionCategoryRefMapper;
    private final MarkTaskRefMapper markTaskRefMapper;

    @DubboReference
    private RemoteExamAnswerService remoteExamAnswerService;
    @DubboReference
    private RemoteUserService remoteUserService;
    @DubboReference
    private RemoteDeptService remoteDeptService;

    // ------------------------------------------------------------------ 查询

    @Override
    public StatDashboardVo dashboard(StatExamQueryBo bo) {
        StatDashboardVo vo = new StatDashboardVo();
        TableDataInfo<StatExamRowVo> rows = listExamPage(bo);
        vo.setRows(rows);

        StatDashboardVo.StatKpiVo kpi = new StatDashboardVo.StatKpiVo();
        kpi.setExamCount((int) rows.getTotal());
        int counted = 0;
        int pending = 0;
        int pendingExam = 0;
        BigDecimal passRateSum = BigDecimal.ZERO;
        BigDecimal scoreSum = BigDecimal.ZERO;
        long usedSum = 0L;
        int rateCount = 0;
        for (StatExamRowVo row : rows.getRows()) {
            counted += ObjectUtil.defaultIfNull(row.getCountedCount(), 0);
            int p = ObjectUtil.defaultIfNull(row.getPendingMarkCount(), 0);
            pending += p;
            if (p > 0) {
                pendingExam++;
            }
            if (ObjectUtil.isNotNull(row.getPassRate())) {
                passRateSum = passRateSum.add(row.getPassRate());
                rateCount++;
            }
            if (ObjectUtil.isNotNull(row.getAvgScore())) {
                scoreSum = scoreSum.add(row.getAvgScore());
            }
            usedSum += ObjectUtil.defaultIfNull(row.getAvgUsedSeconds(), 0);
        }
        kpi.setCountedCount(counted);
        kpi.setPendingMarkCount(pending);
        kpi.setPendingMarkExamCount(pendingExam);
        kpi.setAvgPassRate(rateCount == 0 ? BigDecimal.ZERO
            : passRateSum.divide(BigDecimal.valueOf(rateCount), 2, RoundingMode.HALF_UP));
        kpi.setAvgScore(rateCount == 0 ? BigDecimal.ZERO
            : scoreSum.divide(BigDecimal.valueOf(rateCount), 2, RoundingMode.HALF_UP));
        kpi.setAvgUsedSeconds(rateCount == 0 ? 0 : (int) (usedSum / rateCount));
        vo.setKpi(kpi);

        // 全范围的分数段总体分布：把各场考试的段加起来
        Map<String, StatSegmentVo> bucket = new LinkedHashMap<>();
        for (StatExamRowVo row : rows.getRows()) {
            for (StatSegmentVo seg : segmentMapper.selectVoList(
                Wrappers.lambdaQuery(StatExamScoreSegment.class)
                    .eq(StatExamScoreSegment::getExamId, row.getExamId())
                    .orderByAsc(StatExamScoreSegment::getSort))) {
                StatSegmentVo target = bucket.computeIfAbsent(seg.getSegmentLabel(), k -> {
                    StatSegmentVo n = new StatSegmentVo();
                    n.setSegmentLabel(seg.getSegmentLabel());
                    n.setSegmentMin(seg.getSegmentMin());
                    n.setSegmentMax(seg.getSegmentMax());
                    n.setSort(seg.getSort());
                    n.setPersonCount(0);
                    return n;
                });
                target.setPersonCount(target.getPersonCount() + ObjectUtil.defaultIfNull(seg.getPersonCount(), 0));
            }
        }
        List<StatSegmentVo> dist = new ArrayList<>(bucket.values());
        dist.sort(Comparator.comparingInt(StatSegmentVo::getSort));
        int total = dist.stream().mapToInt(s -> ObjectUtil.defaultIfNull(s.getPersonCount(), 0)).sum();
        int cum = 0;
        for (StatSegmentVo s : dist) {
            BigDecimal rate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(s.getPersonCount()).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            s.setRate(rate);
            cum += ObjectUtil.defaultIfNull(s.getPersonCount(), 0);
            s.setCumulativeRate(total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(cum).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        }
        vo.setScoreDistribution(dist);

        List<StatExamRowVo> top = rows.getRows().stream()
            .filter(r -> ObjectUtil.isNotNull(r.getPassRate()))
            .sorted(Comparator.comparing(StatExamRowVo::getPassRate, Comparator.reverseOrder()))
            .limit(10)
            .toList();
        vo.setTopPassRate(top);
        return vo;
    }

    @Override
    public TableDataInfo<StatExamRowVo> listExamPage(StatExamQueryBo bo) {
        // 统计列表的主语是「考试」，而考试名/状态/时间在 exam 表，
        // 所以先在 exam 上按条件分页，再拿这一页的 examId 去关联汇总表
        LambdaQueryWrapper<ExamRef> eq = Wrappers.lambdaQuery();
        eq.like(StringUtils.isNotBlank(bo.getKeyword()), ExamRef::getExamName, bo.getKeyword())
            .eq(StringUtils.isNotBlank(bo.getExamType()), ExamRef::getExamType, bo.getExamType())
            .eq(StringUtils.isNotBlank(bo.getExamStatus()), ExamRef::getStatus, bo.getExamStatus())
            .ge(ObjectUtil.isNotNull(bo.getBeginTime()), ExamRef::getStartTime, bo.getBeginTime())
            .le(ObjectUtil.isNotNull(bo.getEndTime()), ExamRef::getStartTime, bo.getEndTime())
            .eq(Boolean.TRUE.equals(bo.getOnlyMine()), ExamRef::getCreatorId, LoginHelper.getUserId())
            .orderByDesc(ExamRef::getStartTime)
            .orderByDesc(ExamRef::getId);
        Page<ExamRef> examPage = examRefMapper.selectPage(bo.build(), eq);

        List<Long> examIds = examPage.getRecords().stream().map(ExamRef::getId).toList();
        Map<Long, StatExamRowVo> statMap = new HashMap<>();
        if (CollUtil.isNotEmpty(examIds)) {
            List<StatExamRowVo> stats = summaryMapper.selectVoList(
                Wrappers.lambdaQuery(StatExamSummary.class).in(StatExamSummary::getExamId, examIds));
            for (StatExamRowVo row : stats) {
                statMap.put(row.getExamId(), row);
            }
        }
        List<StatExamRowVo> rows = new ArrayList<>();
        for (ExamRef exam : examPage.getRecords()) {
            StatExamRowVo row = statMap.get(exam.getId());
            if (ObjectUtil.isNull(row)) {
                // 还没算过（没人交卷或还没触发重算）：给一行占位，前端显示「暂无数据」而不是整行消失
                row = new StatExamRowVo();
                row.setExamId(exam.getId());
                row.setCountedCount(0);
                row.setSubmittedCount(0);
                row.setPendingMarkCount(0);
                row.setInvitedCount(0);
                row.setExcludedCount(0);
            }
            row.setPaperId(exam.getPaperId());
            row.setExamName(exam.getExamName());
            row.setExamStatus(exam.getStatus());
            row.setExamType(exam.getExamType());
            row.setStartTime(exam.getStartTime());
            row.setEndTime(exam.getEndTime());
            row.setPartialScore(exam.getPartialScore());
            row.setPartialScoreRate(exam.getPartialScoreRate());
            row.setStale(Boolean.TRUE.equals(row.getStale())
                || StatExamSummary.CALC_FAIL.equals(row.getCalcStatus())
                || ObjectUtil.isNull(row.getCalcTime()));
            if (Boolean.TRUE.equals(bo.getOnlyPendingMark()) && ObjectUtil.defaultIfNull(row.getPendingMarkCount(), 0) <= 0) {
                continue;
            }
            rows.add(row);
        }
        Page<StatExamRowVo> page = new Page<>(examPage.getCurrent(), examPage.getSize(), examPage.getTotal());
        page.setRecords(rows);
        return TableDataInfo.build(page);
    }

    @Override
    public StatOverviewVo overview(Long examId) {
        StatOverviewVo vo = new StatOverviewVo();
        ExamRef exam = examRefMapper.selectById(examId);
        List<StatExamRowVo> statList = summaryMapper.selectVoList(
            Wrappers.lambdaQuery(StatExamSummary.class).eq(StatExamSummary::getExamId, examId).last("limit 1"));
        StatExamRowVo row = CollUtil.isNotEmpty(statList) ? statList.get(0) : null;
        if (ObjectUtil.isNull(row)) {
            row = new StatExamRowVo();
            row.setExamId(examId);
        }
        if (ObjectUtil.isNotNull(exam)) {
            row.setExamName(exam.getExamName());
            row.setExamStatus(exam.getStatus());
            row.setExamType(exam.getExamType());
            row.setStartTime(exam.getStartTime());
            row.setEndTime(exam.getEndTime());
            row.setPartialScore(exam.getPartialScore());
            row.setPartialScoreRate(exam.getPartialScoreRate());
        }
        vo.setSummary(row);
        vo.setPartialScore(row.getPartialScore());
        vo.setPartialScoreRate(row.getPartialScoreRate());
        vo.setSegments(listSegment(examId));
        vo.setPendingMarks(listPendingMark(examId));
        return vo;
    }

    @Override
    public List<StatSegmentVo> listSegment(Long examId) {
        List<StatSegmentVo> list = segmentMapper.selectVoList(
            Wrappers.lambdaQuery(StatExamScoreSegment.class)
                .eq(StatExamScoreSegment::getExamId, examId)
                .orderByAsc(StatExamScoreSegment::getSort));
        int total = list.stream().mapToInt(s -> ObjectUtil.defaultIfNull(s.getPersonCount(), 0)).sum();
        int cum = 0;
        for (StatSegmentVo s : list) {
            BigDecimal rate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(s.getPersonCount()).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            s.setRate(rate);
            cum += ObjectUtil.defaultIfNull(s.getPersonCount(), 0);
            s.setCumulativeRate(total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(cum).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        }
        return list;
    }

    @Override
    public TableDataInfo<StatUserVo> listUserPage(StatUserQueryBo bo) {
        LambdaQueryWrapper<StatExamUser> lqw = Wrappers.lambdaQuery();
        lqw.eq(ObjectUtil.isNotNull(bo.getExamId()), StatExamUser::getExamId, bo.getExamId())
            .and(StringUtils.isNotBlank(bo.getKeyword()), w -> w
                .like(StatExamUser::getUserName, bo.getKeyword())
                .or().like(StatExamUser::getAccount, bo.getKeyword()))
            .eq(ObjectUtil.isNotNull(bo.getPassed()), StatExamUser::getPassed, bo.getPassed())
            .ge(ObjectUtil.isNotNull(bo.getMinScore()), StatExamUser::getTotalScore, bo.getMinScore())
            .le(ObjectUtil.isNotNull(bo.getMaxScore()), StatExamUser::getTotalScore, bo.getMaxScore())
            .eq(StringUtils.isNotBlank(bo.getStatStatus()), StatExamUser::getStatStatus, bo.getStatStatus())
            .orderByAsc(StatExamUser::getRankNo)
            .orderByDesc(StatExamUser::getTotalScore);
        Page<StatUserVo> page = userMapper.selectVoPage(bo.build(), lqw);
        int counted = (int) userMapper.selectCount(Wrappers.lambdaQuery(StatExamUser.class)
            .eq(StatExamUser::getExamId, bo.getExamId())
            .eq(StatExamUser::getStatStatus, StatExamUser.STAT_COUNTED));
        BigDecimal full = fullScoreOf(bo.getExamId());
        for (StatUserVo vo : page.getRecords()) {
            vo.setFullScore(full);
            vo.setBeatRate(calcBeatRate(vo.getRankNo(), counted));
        }
        return TableDataInfo.build(page);
    }

    @Override
    public StatAnswerVo userDetail(Long examId, Long userId, Integer attemptNo) {
        List<StatExamUser> userRows = userMapper.selectList(
            Wrappers.lambdaQuery(StatExamUser.class)
                .eq(StatExamUser::getExamId, examId)
                .eq(StatExamUser::getUserId, userId)
                .orderByAsc(StatExamUser::getAttemptNo));
        if (CollUtil.isEmpty(userRows)) {
            return null;
        }
        StatExamUser target;
        if (ObjectUtil.isNotNull(attemptNo)) {
            target = userRows.stream().filter(u -> attemptNo.equals(u.getAttemptNo())).findFirst().orElse(null);
        } else {
            target = userRows.get(userRows.size() - 1);
        }
        if (ObjectUtil.isNull(target)) {
            target = userRows.get(userRows.size() - 1);
        }

        StatAnswerVo vo = new StatAnswerVo();
        vo.setRecordId(target.getRecordId());
        vo.setExamId(examId);
        vo.setUserId(userId);
        vo.setAttemptNo(target.getAttemptNo());
        vo.setAccount(target.getAccount());
        vo.setUserName(target.getUserName());
        vo.setDeptName(target.getDeptName());
        vo.setTotalScore(target.getTotalScore());
        vo.setObjectiveScore(target.getObjectiveScore());
        vo.setSubjectiveScore(target.getSubjectiveScore());
        vo.setPassed(target.getPassed());
        vo.setRankNo(target.getRankNo());
        vo.setUsedSeconds(target.getUsedSeconds());
        vo.setStatStatus(target.getStatStatus());
        vo.setSubmitTime(target.getSubmitTime());

        StatExamSummary summary = summaryMapper.selectOne(
            Wrappers.lambdaQuery(StatExamSummary.class).eq(StatExamSummary::getExamId, examId).last("limit 1"));
        if (ObjectUtil.isNotNull(summary)) {
            vo.setFullScore(summary.getFullScore());
            vo.setPassScore(summary.getPassScore());
            vo.setCountedCount(summary.getCountedCount());
            vo.setAvgUsedSeconds(summary.getAvgUsedSeconds());
        }
        int counted = (int) userMapper.selectCount(Wrappers.lambdaQuery(StatExamUser.class)
            .eq(StatExamUser::getExamId, examId)
            .eq(StatExamUser::getStatStatus, StatExamUser.STAT_COUNTED));
        vo.setBeatRate(calcBeatRate(target.getRankNo(), counted));

        List<StatAnswerVo.AttemptVo> attempts = new ArrayList<>();
        for (StatExamUser u : userRows) {
            StatAnswerVo.AttemptVo a = new StatAnswerVo.AttemptVo();
            a.setRecordId(u.getRecordId());
            a.setAttemptNo(u.getAttemptNo());
            a.setTotalScore(u.getTotalScore());
            a.setStatStatus(u.getStatStatus());
            attempts.add(a);
        }
        vo.setAttempts(attempts);
        vo.setItems(buildAnswerItems(examId, target.getRecordId()));
        return vo;
    }

    @Override
    public TableDataInfo<StatQuestionVo> listQuestionPage(StatQuestionQueryBo bo) {
        LambdaQueryWrapper<StatExamQuestion> lqw = Wrappers.lambdaQuery();
        lqw.eq(ObjectUtil.isNotNull(bo.getExamId()), StatExamQuestion::getExamId, bo.getExamId())
            .eq(StringUtils.isNotBlank(bo.getQuestionType()), StatExamQuestion::getQuestionType, bo.getQuestionType())
            .eq(StringUtils.isNotBlank(bo.getDifficulty()), StatExamQuestion::getDifficulty, bo.getDifficulty())
            .eq(StringUtils.isNotBlank(bo.getQuestionCategory()), StatExamQuestion::getQuestionCategory, bo.getQuestionCategory())
            .orderByAsc(StatExamQuestion::getSort)
            .orderByAsc(StatExamQuestion::getId);
        Page<StatQuestionVo> page = questionMapper.selectVoPage(bo.build(), lqw);
        // 「只看异常题」是算出來的（正确率过低 / 区分度过低），SQL 不好表达，翻页后在内存里过滤
        List<StatQuestionVo> records = new ArrayList<>(page.getRecords());
        if (Boolean.TRUE.equals(bo.getOnlyAbnormal())) {
            records = records.stream().filter(q -> isAbnormal(q)).toList();
        }
        if (StringUtils.isNotBlank(bo.getKeyword())) {
            records = records.stream()
                .filter(q -> StringUtils.isNotBlank(q.getTitle()) && q.getTitle().contains(bo.getKeyword()))
                .toList();
        }
        for (StatQuestionVo q : records) {
            q.setOptions(optionMapper.selectVoList(Wrappers.lambdaQuery(StatExamQuestionOption.class)
                .eq(StatExamQuestionOption::getExamId, bo.getExamId())
                .eq(StatExamQuestionOption::getQuestionId, q.getQuestionId())
                .orderByAsc(StatExamQuestionOption::getOptionKey)));
            q.setSuggestion(suggestionOf(q));
        }
        Page<StatQuestionVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return TableDataInfo.build(result);
    }

    @Override
    public List<StatKnowledgeVo> listKnowledge(Long examId) {
        return knowledgeMapper.selectVoList(Wrappers.lambdaQuery(StatExamKnowledge.class)
            .eq(StatExamKnowledge::getExamId, examId)
            .orderByAsc(StatExamKnowledge::getMastery));
    }

    @Override
    public void markExcluded(Long examId, Long recordId, boolean excluded) {
        StatExamUser row = userMapper.selectOne(Wrappers.lambdaQuery(StatExamUser.class)
            .eq(StatExamUser::getRecordId, recordId).last("limit 1"));
        if (ObjectUtil.isNull(row)) {
            return;
        }
        StatExamUser update = new StatExamUser();
        update.setId(row.getId());
        update.setStatStatus(excluded ? StatExamUser.STAT_EXCLUDED : StatExamUser.STAT_COUNTED);
        userMapper.updateById(update);
        recalc(examId, "manual");
    }

    // ------------------------------------------------------------------ 重算

    @Override
    public void recalc(Long examId, String trigger) {
        long t0 = System.currentTimeMillis();
        log.info("统计重算开始 examId={} trigger={}", examId, trigger);
        try {
            doRecalc(examId);
            log.info("统计重算完成 examId={} cost={}ms", examId, System.currentTimeMillis() - t0);
        } catch (Exception e) {
            // 统计算挂了不能把调用方（交卷/阅卷）带崩，记日志等下次定时重算自愈
            log.error("统计重算失败 examId={} cost={}ms", examId, System.currentTimeMillis() - t0, e);
            markFail(examId);
        }
    }

    private void doRecalc(Long examId) {
        ExamRef exam = examRefMapper.selectById(examId);
        if (ObjectUtil.isNull(exam)) {
            return;
        }
        // 题目与分值：随机组卷时每个人题目不同，这里取的是「卷面结构」，
        // 实际作答情况以 exam_answer 为准，两边取并集
        Map<Long, PaperQuestionRef> paperQuestionMap = new HashMap<>();
        if (ObjectUtil.isNotNull(exam.getPaperId())) {
            for (PaperQuestionRef pq : paperQuestionRefMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestionRef.class)
                    .eq(PaperQuestionRef::getPaperId, exam.getPaperId())
                    .orderByAsc(PaperQuestionRef::getSort))) {
                paperQuestionMap.put(pq.getQuestionId(), pq);
            }
        }

        // 已交卷的答卷（答题库，跨库走 Dubbo）
        List<RemoteRecordVo> records = remoteExamAnswerService.listRecordsByExam(examId, "submitted");
        if (CollUtil.isEmpty(records)) {
            records = List.of();
        }
        // 阅卷进度：决定谁能入统
        Map<Long, MarkTaskRef> markMap = new HashMap<>();
        for (MarkTaskRef task : markTaskRefMapper.selectList(
            Wrappers.lambdaQuery(MarkTaskRef.class).eq(MarkTaskRef::getExamId, examId))) {
            markMap.put(task.getRecordId(), task);
        }
        // 之前被管理员标记作废的答卷，重算时保留标记
        Set<Long> excludedRecords = new HashSet<>();
        for (StatExamUser old : userMapper.selectList(
            Wrappers.lambdaQuery(StatExamUser.class).eq(StatExamUser::getExamId, examId)
                .eq(StatExamUser::getStatStatus, StatExamUser.STAT_EXCLUDED))) {
            if (ObjectUtil.isNotNull(old.getRecordId())) {
                excludedRecords.add(old.getRecordId());
            }
        }

        // 重考只算最后一次：按考生取 attemptNo 最大的那份
        Map<Long, RemoteRecordVo> latest = new HashMap<>();
        for (RemoteRecordVo r : records) {
            RemoteRecordVo cur = latest.get(r.getUserId());
            if (ObjectUtil.isNull(cur) || ObjectUtil.defaultIfNull(r.getAttemptNo(), 1) > ObjectUtil.defaultIfNull(cur.getAttemptNo(), 1)) {
                latest.put(r.getUserId(), r);
            }
        }

        List<RemoteRecordVo> countedRecords = new ArrayList<>();
        List<RemoteRecordVo> pendingRecords = new ArrayList<>();
        for (RemoteRecordVo r : latest.values()) {
            if (excludedRecords.contains(r.getRecordId())) {
                continue;
            }
            MarkTaskRef task = markMap.get(r.getRecordId());
            // 入统判定：没有阅卷任务（全是客观题）或主观题已阅完
            if (ObjectUtil.isNull(task) || MarkTaskRef.STATUS_FINISHED.equals(task.getStatus())) {
                countedRecords.add(r);
            } else {
                pendingRecords.add(r);
            }
        }

        // 逐份拉作答明细
        Map<Long, List<RemoteAnswerVo>> answerMap = new HashMap<>();
        Set<Long> questionIdSet = new HashSet<>(paperQuestionMap.keySet());
        for (RemoteRecordVo r : countedRecords) {
            List<RemoteAnswerVo> answers = remoteExamAnswerService.listAnswers(r.getRecordId());
            if (CollUtil.isEmpty(answers)) {
                answers = List.of();
            }
            answerMap.put(r.getRecordId(), answers);
            for (RemoteAnswerVo a : answers) {
                questionIdSet.add(a.getQuestionId());
            }
        }
        Map<Long, QuestionRef> questionMap = new HashMap<>();
        if (CollUtil.isNotEmpty(questionIdSet)) {
            for (QuestionRef q : questionRefMapper.selectByIds(questionIdSet)) {
                questionMap.put(q.getId(), q);
            }
        }

        // 排序后的入统考生，用于排名与高低分组
        countedRecords.sort(Comparator.comparing(
            (RemoteRecordVo r) -> ObjectUtil.defaultIfNull(r.getTotalScore(), BigDecimal.ZERO)).reversed());
        BigDecimal fullScore = calcFullScore(paperQuestionMap, questionMap);
        BigDecimal passScore = CollUtil.isNotEmpty(records)
            ? ObjectUtil.defaultIfNull(records.get(0).getPassScore(), BigDecimal.ZERO)
            : BigDecimal.ZERO;

        // 高/低分组（各 27%），算区分度用
        int groupSize = Math.max(1, (int) Math.ceil(countedRecords.size() * GROUP_27));
        Set<Long> highGroup = new HashSet<>();
        Set<Long> lowGroup = new HashSet<>();
        for (int i = 0; i < groupSize && i < countedRecords.size(); i++) {
            highGroup.add(countedRecords.get(i).getRecordId());
        }
        for (int i = 0; i < groupSize && i < countedRecords.size(); i++) {
            lowGroup.add(countedRecords.get(countedRecords.size() - 1 - i).getRecordId());
        }

        // 题目维度累计
        Map<Long, QuestionAgg> aggMap = new LinkedHashMap<>();
        for (RemoteRecordVo r : countedRecords) {
            for (RemoteAnswerVo a : answerMap.getOrDefault(r.getRecordId(), List.of())) {
                QuestionAgg agg = aggMap.computeIfAbsent(a.getQuestionId(), k -> new QuestionAgg());
                QuestionRef q = questionMap.get(a.getQuestionId());
                BigDecimal qFull = fullScoreOfQuestion(paperQuestionMap.get(a.getQuestionId()), q);
                agg.fullScore = qFull;
                agg.answerCount++;
                BigDecimal score = ObjectUtil.defaultIfNull(a.getScore(), BigDecimal.ZERO);
                agg.scoreSum = agg.scoreSum.add(score);
                agg.maxScore = agg.maxScore.max(score);
                agg.minScore = agg.minScore.min(score);
                if (score.compareTo(BigDecimal.ZERO) == 0) {
                    agg.zeroCount++;
                }
                if (qFull.compareTo(BigDecimal.ZERO) > 0 && score.compareTo(qFull) >= 0) {
                    agg.fullCount++;
                }
                if (StringUtils.isBlank(a.getAnswerContent())) {
                    agg.blankCount++;
                }
                Integer correct = a.getCorrect();
                if (Integer.valueOf(1).equals(correct)) {
                    agg.correctCount++;
                    if (highGroup.contains(r.getRecordId())) {
                        agg.highCorrect++;
                    }
                    if (lowGroup.contains(r.getRecordId())) {
                        agg.lowCorrect++;
                    }
                } else if (Integer.valueOf(3).equals(correct)) {
                    // 部分正确：既不算答对也不算答错，单独记
                    agg.partialCount++;
                } else if (Integer.valueOf(2).equals(correct)) {
                    agg.wrongCount++;
                }
                if (Integer.valueOf(1).equals(correct)) {
                    agg.correctUsers.add(r.getRecordId());
                }
                // 选项分布：只统计客观题
                if (isObjective(a.getQuestionType()) && StringUtils.isNotBlank(a.getAnswerContent())) {
                    for (String key : parseChoiceKeys(a.getAnswerContent())) {
                        agg.optionCount.merge(key, 1, Integer::sum);
                    }
                }
            }
        }

        // 写库：先清旧的再写新的，统计是派生数据，物理删不用留痕
        clearExamStat(examId);
        saveQuestions(examId, aggMap, questionMap, paperQuestionMap, groupSize);
        saveKnowledge(examId, aggMap, questionMap);
        List<StatExamUser> userRows = saveUsers(examId, countedRecords, pendingRecords, excludedRecords,
            answerMap, questionMap, records, fullScore, passScore);
        saveSummary(examId, exam, userRows, countedRecords, pendingRecords, records,
            aggMap, fullScore, passScore, questionMap);
    }

    private void saveSummary(Long examId, ExamRef exam, List<StatExamUser> userRows,
                             List<RemoteRecordVo> countedRecords, List<RemoteRecordVo> pendingRecords,
                             List<RemoteRecordVo> allRecords, Map<Long, QuestionAgg> aggMap,
                             BigDecimal fullScore, BigDecimal passScore, Map<Long, QuestionRef> questionMap) {
        StatExamSummary old = summaryMapper.selectOne(
            Wrappers.lambdaQuery(StatExamSummary.class).eq(StatExamSummary::getExamId, examId).last("limit 1"));
        StatExamSummary s = ObjectUtil.isNotNull(old) ? old : new StatExamSummary();
        s.setExamId(examId);
        s.setPaperId(exam.getPaperId());
        s.setExamName(exam.getExamName());
        s.setSubmittedCount(allRecords.size());
        s.setCountedCount(countedRecords.size());
        s.setPendingMarkCount(pendingRecords.size());
        s.setExcludedCount(Math.max(0, allRecords.size() - countedRecords.size() - pendingRecords.size()));
        s.setInvitedCount(countInvited(exam, allRecords));
        s.setFullScore(fullScore);
        s.setPassScore(passScore);
        s.setPartialScore(exam.getPartialScore());
        s.setPartialScoreRate(exam.getPartialScoreRate());

        List<BigDecimal> scores = countedRecords.stream()
            .map(r -> ObjectUtil.defaultIfNull(r.getTotalScore(), BigDecimal.ZERO))
            .sorted()
            .toList();
        int n = scores.size();
        if (n > 0) {
            BigDecimal sum = BigDecimal.ZERO;
            for (BigDecimal v : scores) {
                sum = sum.add(v);
            }
            s.setAvgScore(sum.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP));
            s.setMaxScore(scores.get(n - 1));
            s.setMinScore(scores.get(0));
            s.setMedianScore(n % 2 == 1 ? scores.get(n / 2)
                : scores.get(n / 2 - 1).add(scores.get(n / 2)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            s.setStdDev(calcStdDev(scores, s.getAvgScore()));
            long passCount = countedRecords.stream()
                .filter(r -> ObjectUtil.defaultIfNull(r.getPassed(), 0L) == 1L).count();
            s.setPassCount((int) passCount);
            s.setPassRate(rate(passCount, n));
            BigDecimal excellentLine = fullScore.multiply(BigDecimal.valueOf(0.9));
            long excellent = countedRecords.stream()
                .filter(r -> ObjectUtil.defaultIfNull(r.getTotalScore(), BigDecimal.ZERO).compareTo(excellentLine) >= 0)
                .count();
            s.setExcellentCount((int) excellent);
            s.setExcellentRate(rate(excellent, n));
            long usedSum = countedRecords.stream()
                .mapToLong(r -> ObjectUtil.defaultIfNull(r.getUsedSeconds(), 0)).sum();
            s.setAvgUsedSeconds((int) (usedSum / n));
            BigDecimal objSum = BigDecimal.ZERO;
            BigDecimal subSum = BigDecimal.ZERO;
            for (RemoteRecordVo r : countedRecords) {
                objSum = objSum.add(ObjectUtil.defaultIfNull(r.getObjectiveScore(), BigDecimal.ZERO));
                subSum = subSum.add(ObjectUtil.defaultIfNull(r.getSubjectiveScore(), BigDecimal.ZERO));
            }
            s.setAvgObjectiveScore(objSum.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP));
            s.setAvgSubjectiveScore(subSum.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP));
            s.setDifficulty(fullScore.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : s.getAvgScore().multiply(BigDecimal.valueOf(100))
                .divide(fullScore, 2, RoundingMode.HALF_UP));
            BigDecimal discSum = BigDecimal.ZERO;
            int discCount = 0;
            for (QuestionAgg agg : aggMap.values()) {
                if (ObjectUtil.isNotNull(agg.discrimination)) {
                    discSum = discSum.add(agg.discrimination);
                    discCount++;
                }
            }
            s.setDiscrimination(discCount == 0 ? BigDecimal.ZERO
                : discSum.divide(BigDecimal.valueOf(discCount), 2, RoundingMode.HALF_UP));
        } else {
            s.setAvgScore(BigDecimal.ZERO);
            s.setMaxScore(BigDecimal.ZERO);
            s.setMinScore(BigDecimal.ZERO);
            s.setMedianScore(BigDecimal.ZERO);
            s.setStdDev(BigDecimal.ZERO);
            s.setPassCount(0);
            s.setPassRate(BigDecimal.ZERO);
            s.setExcellentCount(0);
            s.setExcellentRate(BigDecimal.ZERO);
            s.setAvgUsedSeconds(0);
            s.setAvgObjectiveScore(BigDecimal.ZERO);
            s.setAvgSubjectiveScore(BigDecimal.ZERO);
            s.setDifficulty(BigDecimal.ZERO);
            s.setDiscrimination(BigDecimal.ZERO);
        }
        s.setAttendanceRate(s.getInvitedCount() == null || s.getInvitedCount() == 0 ? BigDecimal.ZERO
            : rate(s.getSubmittedCount(), s.getInvitedCount()));
        boolean hasSubjective = questionMap.values().stream()
            .anyMatch(q -> !isObjective(q.getQuestionType()));
        s.setHasSubjective(hasSubjective ? "1" : "0");
        s.setCalcStatus(StatExamSummary.CALC_SUCCESS);
        s.setCalcVersion(ObjectUtil.defaultIfNull(s.getCalcVersion(), 0) + 1);
        s.setCalcTime(new Date());

        if (ObjectUtil.isNotNull(old)) {
            summaryMapper.updateById(s);
        } else {
            summaryMapper.insert(s);
        }
        saveSegments(examId, scores, fullScore);
    }

    private void saveSegments(Long examId, List<BigDecimal> scores, BigDecimal fullScore) {
        List<StatExamScoreSegment> list = new ArrayList<>();
        double[][] ratio = {{0, 0.6}, {0.6, 0.7}, {0.7, 0.8}, {0.8, 0.9}, {0.9, 1.01}};
        for (int i = 0; i < ratio.length; i++) {
            BigDecimal min = fullScore.multiply(BigDecimal.valueOf(ratio[i][0])).setScale(2, RoundingMode.HALF_UP);
            BigDecimal max = fullScore.multiply(BigDecimal.valueOf(ratio[i][1])).setScale(2, RoundingMode.HALF_UP);
            long count = scores.stream()
                .filter(v -> v.compareTo(min) >= 0 && (i == ratio.length - 1 ? v.compareTo(fullScore) <= 0 : v.compareTo(max) < 0))
                .count();
            StatExamScoreSegment seg = new StatExamScoreSegment();
            seg.setExamId(examId);
            seg.setSegmentLabel(min.stripTrailingZeros().toPlainString() + "-" + max.stripTrailingZeros().toPlainString());
            seg.setSegmentMin(min);
            seg.setSegmentMax(max);
            seg.setPersonCount((int) count);
            seg.setSort(i);
            list.add(seg);
        }
        for (StatExamScoreSegment seg : list) {
            segmentMapper.insert(seg);
        }
    }

    private void saveQuestions(Long examId, Map<Long, QuestionAgg> aggMap, Map<Long, QuestionRef> questionMap,
                               Map<Long, PaperQuestionRef> paperQuestionMap, int groupSize) {
        for (Map.Entry<Long, QuestionAgg> entry : aggMap.entrySet()) {
            Long questionId = entry.getKey();
            QuestionAgg agg = entry.getValue();
            QuestionRef q = questionMap.get(questionId);
            PaperQuestionRef pq = paperQuestionMap.get(questionId);
            StatExamQuestion row = new StatExamQuestion();
            row.setExamId(examId);
            row.setQuestionId(questionId);
            row.setQuestionType(ObjectUtil.isNull(q) ? "" : ObjectUtil.defaultIfNull(q.getQuestionType(), ""));
            row.setQuestionCategory(isObjective(row.getQuestionType())
                ? StatExamQuestion.CATEGORY_OBJECTIVE : StatExamQuestion.CATEGORY_SUBJECTIVE);
            row.setDifficulty(ObjectUtil.isNull(q) ? "" : ObjectUtil.defaultIfNull(q.getDifficulty(), ""));
            row.setSort(ObjectUtil.isNull(pq) ? 0 : ObjectUtil.defaultIfNull(pq.getSort(), 0));
            row.setFullScore(agg.fullScore);
            row.setAnswerCount(agg.answerCount);
            row.setBlankCount(agg.blankCount);
            row.setCorrectCount(agg.correctCount);
            row.setPartialCount(agg.partialCount);
            row.setWrongCount(agg.wrongCount);
            row.setCorrectRate(rate(agg.correctCount, agg.answerCount));
            row.setAvgScore(agg.answerCount == 0 ? BigDecimal.ZERO
                : agg.scoreSum.divide(BigDecimal.valueOf(agg.answerCount), 2, RoundingMode.HALF_UP));
            row.setScoreRate(agg.fullScore.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : row.getAvgScore().multiply(BigDecimal.valueOf(100))
                .divide(agg.fullScore, 2, RoundingMode.HALF_UP));
            row.setMaxScore(agg.maxScore);
            row.setMinScore(agg.minScore);
            row.setZeroCount(agg.zeroCount);
            row.setFullCount(agg.fullCount);
            row.setDifficultyIndex(agg.fullScore.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : StatExamQuestion.CATEGORY_OBJECTIVE.equals(row.getQuestionCategory())
                ? row.getCorrectRate() : row.getScoreRate());
            // 区分度 = 高分组正确率 − 低分组正确率
            BigDecimal high = rate(agg.highCorrect, Math.min(groupSize, agg.answerCount));
            BigDecimal low = rate(agg.lowCorrect, Math.min(groupSize, agg.answerCount));
            agg.discrimination = high.subtract(low);
            row.setDiscrimination(agg.discrimination);
            questionMapper.insert(row);

            if (isObjective(row.getQuestionType())) {
                saveOptions(examId, questionId, agg);
            }
        }
    }

    private void saveOptions(Long examId, Long questionId, QuestionAgg agg) {
        Set<String> rightKeys = parseRightKeys(questionId);
        Map<String, String> contentMap = new HashMap<>();
        for (QuestionOptionRef opt : questionOptionRefMapper.selectList(
            Wrappers.lambdaQuery(QuestionOptionRef.class).eq(QuestionOptionRef::getQuestionId, questionId)
                .orderByAsc(QuestionOptionRef::getSort))) {
            contentMap.put(opt.getOptionKey(), opt.getOptionContent());
        }
        for (Map.Entry<String, Integer> e : agg.optionCount.entrySet()) {
            StatExamQuestionOption row = new StatExamQuestionOption();
            row.setExamId(examId);
            row.setQuestionId(questionId);
            row.setOptionKey(e.getKey());
            row.setOptionContent(contentMap.getOrDefault(e.getKey(), ""));
            row.setSelectCount(e.getValue());
            row.setSelectRate(rate(e.getValue(), agg.answerCount));
            boolean correct = rightKeys.contains(e.getKey());
            row.setIsCorrect(correct ? "1" : "0");
            // 易错项：不是正确选项，却有超过三成的人选了它 —— 说明这个干扰项编得太像了，
            // 或者知识点本身就没讲清楚，是要重点讲评的那一档
            row.setTrap(!correct && row.getSelectRate().compareTo(BigDecimal.valueOf(30)) >= 0);
            optionMapper.insert(row);
        }
    }

    private void saveKnowledge(Long examId, Map<Long, QuestionAgg> aggMap, Map<Long, QuestionRef> questionMap) {
        // 一期知识点 = 题库分类树：question → question_bank → question_bank_category
        Map<Long, Long> bankCategory = new HashMap<>();
        Map<Long, String> categoryName = new HashMap<>();
        Map<Long, Long> categoryParent = new HashMap<>();
        for (QuestionCategoryRef c : questionCategoryRefMapper.selectList(
            Wrappers.lambdaQuery(QuestionCategoryRef.class).eq(QuestionCategoryRef::getIsDeleted, 0))) {
            categoryName.put(c.getId(), c.getCategoryName());
            categoryParent.put(c.getId(), c.getParentId());
        }
        Set<Long> bankIds = questionMap.values().stream().map(QuestionRef::getBankId)
            .filter(ObjectUtil::isNotNull).collect(java.util.stream.Collectors.toSet());
        if (CollUtil.isNotEmpty(bankIds)) {
            for (QuestionBankRef b : questionBankRefMapper.selectByIds(bankIds)) {
                if (ObjectUtil.isNotNull(b.getCategoryId())) {
                    bankCategory.put(b.getId(), b.getCategoryId());
                }
            }
        }
        Map<Long, KnowledgeAgg> kMap = new LinkedHashMap<>();
        for (Map.Entry<Long, QuestionAgg> entry : aggMap.entrySet()) {
            QuestionRef q = questionMap.get(entry.getKey());
            if (ObjectUtil.isNull(q) || ObjectUtil.isNull(q.getBankId())) {
                continue;
            }
            Long categoryId = bankCategory.get(q.getBankId());
            if (ObjectUtil.isNull(categoryId)) {
                continue;
            }
            KnowledgeAgg k = kMap.computeIfAbsent(categoryId, id -> new KnowledgeAgg());
            QuestionAgg agg = entry.getValue();
            k.questionCount++;
            k.fullScore = k.fullScore.add(agg.fullScore.multiply(BigDecimal.valueOf(agg.answerCount)));
            // 失分 = 满分 × 作答人数 − 实际得分之和
            k.lostScore = k.lostScore.add(agg.fullScore.multiply(BigDecimal.valueOf(agg.answerCount)).subtract(agg.scoreSum));
            k.wrongCount += agg.wrongCount;
            k.answerCount += agg.answerCount;
            k.userSet.addAll(agg.wrongUsers);
        }
        for (Map.Entry<Long, KnowledgeAgg> entry : kMap.entrySet()) {
            KnowledgeAgg k = entry.getValue();
            StatExamKnowledge row = new StatExamKnowledge();
            row.setExamId(examId);
            row.setKnowledgeId(entry.getKey());
            row.setKnowledgeName(categoryName.getOrDefault(entry.getKey(), "未分类"));
            row.setKnowledgePath(buildCategoryPath(entry.getKey(), categoryName, categoryParent));
            row.setQuestionCount(k.questionCount);
            row.setFullScore(k.fullScore);
            row.setAvgScore(k.answerCount == 0 ? BigDecimal.ZERO
                : k.fullScore.subtract(k.lostScore).divide(BigDecimal.valueOf(Math.max(1, k.answerCount)), 2, RoundingMode.HALF_UP));
            row.setScoreRate(k.fullScore.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : k.fullScore.subtract(k.lostScore).multiply(BigDecimal.valueOf(100))
                .divide(k.fullScore, 2, RoundingMode.HALF_UP));
            row.setWrongCount(k.wrongCount);
            row.setWrongRate(rate(k.wrongCount, k.answerCount));
            // 掌握度按失分加权：2 分的选择题不该和 20 分的论述题有一样的话语权
            row.setMastery(k.fullScore.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : k.fullScore.subtract(k.lostScore).multiply(BigDecimal.valueOf(100))
                .divide(k.fullScore, 2, RoundingMode.HALF_UP));
            row.setWeakLevel(row.getMastery().compareTo(BigDecimal.valueOf(60)) < 0
                ? StatExamKnowledge.WEAK_WEAK
                : (row.getMastery().compareTo(BigDecimal.valueOf(80)) < 0
                ? StatExamKnowledge.WEAK_NORMAL : StatExamKnowledge.WEAK_GOOD));
            knowledgeMapper.insert(row);
        }
    }

    private List<StatExamUser> saveUsers(Long examId, List<RemoteRecordVo> countedRecords,
                                         List<RemoteRecordVo> pendingRecords, Set<Long> excludedRecords,
                                         Map<Long, List<RemoteAnswerVo>> answerMap, Map<Long, QuestionRef> questionMap,
                                         List<RemoteRecordVo> allRecords, BigDecimal fullScore, BigDecimal passScore) {
        Map<Long, String> userNameMap = new HashMap<>();
        Map<Long, String> deptNameMap = new HashMap<>();
        Set<Long> userIds = allRecords.stream().map(RemoteRecordVo::getUserId)
            .filter(ObjectUtil::isNotNull).collect(java.util.stream.Collectors.toSet());
        if (CollUtil.isNotEmpty(userIds)) {
            try {
                List<RemoteUserVo> users = remoteUserService.selectListByIds(new ArrayList<>(userIds));
                if (CollUtil.isNotEmpty(users)) {
                    Set<Long> deptIds = users.stream().map(RemoteUserVo::getDeptId)
                        .filter(ObjectUtil::isNotNull).collect(java.util.stream.Collectors.toSet());
                    Map<Long, String> depts = CollUtil.isNotEmpty(deptIds)
                        ? remoteDeptService.selectDeptNamesByIds(new ArrayList<>(deptIds)) : Map.of();
                    for (RemoteUserVo u : users) {
                        userNameMap.put(u.getUserId(), ObjectUtil.defaultIfNull(u.getNickName(), u.getUserName()));
                        deptNameMap.put(u.getUserId(), depts.getOrDefault(u.getDeptId(), ""));
                    }
                }
            } catch (Exception e) {
                // 用户服务查不到也不能让统计算不出来，姓名留空即可
                log.warn("补全考生姓名失败 examId={}", examId, e);
            }
        }

        List<StatExamUser> rows = new ArrayList<>();
        int rank = 0;
        BigDecimal lastScore = null;
        int sameRankCount = 0;
        for (RemoteRecordVo r : countedRecords) {
            BigDecimal score = ObjectUtil.defaultIfNull(r.getTotalScore(), BigDecimal.ZERO);
            if (ObjectUtil.isNull(lastScore) || score.compareTo(lastScore) != 0) {
                rank = rank + sameRankCount + 1;
                sameRankCount = 0;
                lastScore = score;
            } else {
                sameRankCount++;
            }
            rows.add(buildUserRow(examId, r, StatExamUser.STAT_COUNTED, rank,
                answerMap.getOrDefault(r.getRecordId(), List.of()), questionMap, userNameMap, deptNameMap, passScore));
        }
        for (RemoteRecordVo r : pendingRecords) {
            rows.add(buildUserRow(examId, r, StatExamUser.STAT_PENDING_MARK, 0,
                List.of(), questionMap, userNameMap, deptNameMap, passScore));
        }
        // 作废的答卷单独成行保留，管理员能看到自己排除了哪些人
        for (RemoteRecordVo r : allRecords) {
            if (excludedRecords.contains(r.getRecordId())) {
                rows.add(buildUserRow(examId, r, StatExamUser.STAT_EXCLUDED, 0,
                    List.of(), questionMap, userNameMap, deptNameMap, passScore));
            }
        }
        for (StatExamUser row : rows) {
            userMapper.insert(row);
        }
        return rows;
    }

    private StatExamUser buildUserRow(Long examId, RemoteRecordVo r, String statStatus, int rank,
                                      List<RemoteAnswerVo> answers, Map<Long, QuestionRef> questionMap,
                                      Map<Long, String> userNameMap, Map<Long, String> deptNameMap, BigDecimal passScore) {
        StatExamUser row = new StatExamUser();
        row.setExamId(examId);
        row.setUserId(r.getUserId());
        row.setRecordId(r.getRecordId());
        row.setAttemptNo(ObjectUtil.defaultIfNull(r.getAttemptNo(), 1));
        row.setAccount(r.getAccount());
        row.setUserName(userNameMap.getOrDefault(r.getUserId(), ""));
        row.setDeptName(deptNameMap.getOrDefault(r.getUserId(), ""));
        row.setTotalScore(ObjectUtil.defaultIfNull(r.getTotalScore(), BigDecimal.ZERO));
        row.setObjectiveScore(ObjectUtil.defaultIfNull(r.getObjectiveScore(), BigDecimal.ZERO));
        row.setSubjectiveScore(ObjectUtil.defaultIfNull(r.getSubjectiveScore(), BigDecimal.ZERO));
        row.setPassed(ObjectUtil.defaultIfNull(r.getPassed(), 0L).intValue());
        row.setRankNo(rank);
        row.setUsedSeconds(ObjectUtil.defaultIfNull(r.getUsedSeconds(), 0));
        row.setSubmitTime(r.getSubmitTime());
        row.setStatStatus(statStatus);
        int correct = 0;
        int wrong = 0;
        int blank = 0;
        for (RemoteAnswerVo a : answers) {
            if (StringUtils.isBlank(a.getAnswerContent())) {
                blank++;
            } else if (Integer.valueOf(1).equals(a.getCorrect())) {
                correct++;
            } else if (Integer.valueOf(2).equals(a.getCorrect())) {
                wrong++;
            } else if (Integer.valueOf(3).equals(a.getCorrect())) {
                // 半对：不进答对也不进答错，但要从未答里扣出来
                blank = Math.max(0, blank);
            }
        }
        row.setCorrectCount(correct);
        row.setWrongCount(wrong);
        row.setBlankCount(blank);
        return row;
    }

    private List<StatAnswerVo.StatAnswerItemVo> buildAnswerItems(Long examId, Long recordId) {
        List<RemoteAnswerVo> answers = remoteExamAnswerService.listAnswers(recordId);
        if (CollUtil.isEmpty(answers)) {
            return List.of();
        }
        Set<Long> ids = answers.stream().map(RemoteAnswerVo::getQuestionId).collect(java.util.stream.Collectors.toSet());
        Map<Long, QuestionRef> questionMap = new HashMap<>();
        for (QuestionRef q : questionRefMapper.selectByIds(ids)) {
            questionMap.put(q.getId(), q);
        }
        Map<Long, List<StatOptionVo>> optionMap = new HashMap<>();
        for (StatOptionVo opt : optionMapper.selectVoList(Wrappers.lambdaQuery(StatExamQuestionOption.class)
            .eq(StatExamQuestionOption::getExamId, examId))) {
            optionMap.computeIfAbsent(opt.getQuestionId(), k -> new ArrayList<>()).add(opt);
        }
        Map<Long, PaperQuestionRef> pqMap = new HashMap<>();
        StatExamSummary summary = summaryMapper.selectOne(
            Wrappers.lambdaQuery(StatExamSummary.class).eq(StatExamSummary::getExamId, examId).last("limit 1"));
        if (ObjectUtil.isNotNull(summary) && ObjectUtil.isNotNull(summary.getPaperId())) {
            for (PaperQuestionRef pq : paperQuestionRefMapper.selectList(
                Wrappers.lambdaQuery(PaperQuestionRef.class).eq(PaperQuestionRef::getPaperId, summary.getPaperId()))) {
                pqMap.put(pq.getQuestionId(), pq);
            }
        }
        List<StatAnswerVo.StatAnswerItemVo> items = new ArrayList<>();
        for (RemoteAnswerVo a : answers) {
            QuestionRef q = questionMap.get(a.getQuestionId());
            StatAnswerVo.StatAnswerItemVo item = new StatAnswerVo.StatAnswerItemVo();
            item.setQuestionId(a.getQuestionId());
            item.setSort(ObjectUtil.defaultIfNull(a.getSort(), 0));
            item.setQuestionType(ObjectUtil.isNull(q) ? ObjectUtil.defaultIfNull(a.getQuestionType(), "")
                : ObjectUtil.defaultIfNull(q.getQuestionType(), ""));
            item.setQuestionCategory(isObjective(item.getQuestionType())
                ? StatExamQuestion.CATEGORY_OBJECTIVE : StatExamQuestion.CATEGORY_SUBJECTIVE);
            item.setDifficulty(ObjectUtil.isNull(q) ? "" : ObjectUtil.defaultIfNull(q.getDifficulty(), ""));
            item.setTitle(ObjectUtil.isNull(q) ? "" : plainText(q.getTitle()));
            item.setOptions(optionMap.getOrDefault(a.getQuestionId(), List.of()));
            item.setAnswerContent(a.getAnswerContent());
            item.setAnswerText(plainAnswer(a.getAnswerContent()));
            item.setFullScore(fullScoreOfQuestion(pqMap.get(a.getQuestionId()), q));
            item.setScore(ObjectUtil.defaultIfNull(a.getScore(), BigDecimal.ZERO));
            item.setCorrect(a.getCorrect());
            item.setResult(resultText(a.getCorrect(), a.getAnswerContent()));
            item.setAnalysis(ObjectUtil.isNull(q) ? "" : q.getAnalysis());
            items.add(item);
        }
        items.sort(Comparator.comparingInt(StatAnswerVo.StatAnswerItemVo::getSort));
        return items;
    }

    private List<StatOverviewVo.StatMarkProgressVo> listPendingMark(Long examId) {
        List<MarkTaskRef> tasks = markTaskRefMapper.selectList(Wrappers.lambdaQuery(MarkTaskRef.class)
            .eq(MarkTaskRef::getExamId, examId)
            .ne(MarkTaskRef::getStatus, MarkTaskRef.STATUS_FINISHED)
            .orderByDesc(MarkTaskRef::getId));
        List<StatOverviewVo.StatMarkProgressVo> list = new ArrayList<>();
        for (MarkTaskRef t : tasks) {
            StatOverviewVo.StatMarkProgressVo vo = new StatOverviewVo.StatMarkProgressVo();
            vo.setRecordId(t.getRecordId());
            vo.setQuestionCount(t.getQuestionCount());
            vo.setMarkedCount(t.getMarkedCount());
            vo.setStatus(t.getStatus());
            vo.setMarkerName(t.getMarkerName());
            list.add(vo);
        }
        return list;
    }

    // ------------------------------------------------------------------ 工具

    private void clearExamStat(Long examId) {
        segmentMapper.delete(Wrappers.lambdaQuery(StatExamScoreSegment.class)
            .eq(StatExamScoreSegment::getExamId, examId));
        questionMapper.delete(Wrappers.lambdaQuery(StatExamQuestion.class)
            .eq(StatExamQuestion::getExamId, examId));
        optionMapper.delete(Wrappers.lambdaQuery(StatExamQuestionOption.class)
            .eq(StatExamQuestionOption::getExamId, examId));
        knowledgeMapper.delete(Wrappers.lambdaQuery(StatExamKnowledge.class)
            .eq(StatExamKnowledge::getExamId, examId));
        userMapper.delete(Wrappers.lambdaQuery(StatExamUser.class)
            .eq(StatExamUser::getExamId, examId));
    }

    private void markFail(Long examId) {
        StatExamSummary old = summaryMapper.selectOne(
            Wrappers.lambdaQuery(StatExamSummary.class).eq(StatExamSummary::getExamId, examId).last("limit 1"));
        if (ObjectUtil.isNull(old)) {
            return;
        }
        StatExamSummary update = new StatExamSummary();
        update.setId(old.getId());
        update.setCalcStatus(StatExamSummary.CALC_FAIL);
        summaryMapper.updateById(update);
    }

    private int countInvited(ExamRef exam, List<RemoteRecordVo> records) {
        if ("white".equals(exam.getParticipantType())) {
            return (int) examUserRefMapper.selectCount(
                Wrappers.lambdaQuery(org.dromara.exam.stat.domain.ref.ExamUserRef.class)
                    .eq(org.dromara.exam.stat.domain.ref.ExamUserRef::getExamId, exam.getId()));
        }
        // 公开链接的考试没有固定名单，应考人数只能以实际参加人数为准
        return records.size();
    }

    private BigDecimal calcFullScore(Map<Long, PaperQuestionRef> paperQuestionMap, Map<Long, QuestionRef> questionMap) {
        if (CollUtil.isNotEmpty(paperQuestionMap)) {
            BigDecimal sum = BigDecimal.ZERO;
            for (Map.Entry<Long, PaperQuestionRef> e : paperQuestionMap.entrySet()) {
                sum = sum.add(fullScoreOfQuestion(e.getValue(), questionMap.get(e.getKey())));
            }
            return sum;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (QuestionRef q : questionMap.values()) {
            sum = sum.add(ObjectUtil.defaultIfNull(q.getScore(), BigDecimal.ZERO));
        }
        return sum;
    }

    private BigDecimal fullScoreOfQuestion(PaperQuestionRef pq, QuestionRef q) {
        if (ObjectUtil.isNotNull(pq) && ObjectUtil.isNotNull(pq.getPaperScore()) && pq.getPaperScore().compareTo(BigDecimal.ZERO) > 0) {
            return pq.getPaperScore();
        }
        return ObjectUtil.isNull(q) ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(q.getScore(), BigDecimal.ZERO);
    }

    private BigDecimal fullScoreOf(Long examId) {
        StatExamSummary s = summaryMapper.selectOne(
            Wrappers.lambdaQuery(StatExamSummary.class).eq(StatExamSummary::getExamId, examId).last("limit 1"));
        return ObjectUtil.isNull(s) ? BigDecimal.ZERO : ObjectUtil.defaultIfNull(s.getFullScore(), BigDecimal.ZERO);
    }

    private BigDecimal rate(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(part).multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcStdDev(List<BigDecimal> sorted, BigDecimal avg) {
        if (sorted.size() < 2) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : sorted) {
            BigDecimal d = v.subtract(avg);
            sum = sum.add(d.multiply(d));
        }
        BigDecimal variance = sum.divide(BigDecimal.valueOf(sorted.size()), 6, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcBeatRate(Integer rankNo, int counted) {
        if (counted <= 0 || ObjectUtil.isNull(rankNo) || rankNo <= 0) {
            return BigDecimal.ZERO;
        }
        return rate(Math.max(0, counted - rankNo), counted);
    }

    private boolean isObjective(String questionType) {
        return StringUtils.isNotBlank(questionType) && OBJECTIVE_TYPES.contains(questionType);
    }

    private boolean isAbnormal(StatQuestionVo q) {
        boolean lowCorrect = ObjectUtil.isNotNull(q.getCorrectRate()) && q.getCorrectRate().compareTo(BigDecimal.valueOf(60)) < 0;
        boolean lowScore = ObjectUtil.isNotNull(q.getScoreRate()) && q.getScoreRate().compareTo(BigDecimal.valueOf(60)) < 0;
        boolean lowDisc = ObjectUtil.isNotNull(q.getDiscrimination()) && q.getDiscrimination().compareTo(BigDecimal.valueOf(0.2)) < 0;
        return lowDisc || lowCorrect || lowScore;
    }

    private String suggestionOf(StatQuestionVo q) {
        if (StatExamQuestion.CATEGORY_SUBJECTIVE.equals(q.getQuestionCategory())) {
            return null;
        }
        if (ObjectUtil.isNotNull(q.getCorrectRate()) && q.getCorrectRate().compareTo(BigDecimal.valueOf(30)) < 0
            && ObjectUtil.isNotNull(q.getDiscrimination()) && q.getDiscrimination().compareTo(BigDecimal.valueOf(0.2)) < 0) {
            return "正确率与区分度双低，疑似题目表述有误或超纲，建议复核";
        }
        if (ObjectUtil.isNotNull(q.getCorrectRate()) && q.getCorrectRate().compareTo(BigDecimal.valueOf(95)) >= 0) {
            return "过于简单，区分不出水平，建议提高难度或替换";
        }
        if (ObjectUtil.isNotNull(q.getDiscrimination()) && q.getDiscrimination().compareTo(BigDecimal.valueOf(0.2)) < 0) {
            return "区分度偏低，建议淘汰或改写";
        }
        return null;
    }

    private String resultText(Integer correct, String answerContent) {
        if (StringUtils.isBlank(answerContent)) {
            return "未答";
        }
        if (Integer.valueOf(1).equals(correct)) {
            return "正确";
        }
        if (Integer.valueOf(3).equals(correct)) {
            return "部分正确";
        }
        if (Integer.valueOf(2).equals(correct)) {
            return "错误";
        }
        return "待阅";
    }

    /** 从作答 JSON 里取出选中的选项：兼容 {choices:[A,B]} 与直接的 [A,B] */
    private Set<String> parseChoiceKeys(String answerContent) {
        Set<String> keys = new HashSet<>();
        if (StringUtils.isBlank(answerContent)) {
            return keys;
        }
        String json = answerContent.trim();
        int idx = json.indexOf("[");
        int end = json.indexOf("]");
        if (idx < 0 || end <= idx) {
            return keys;
        }
        String[] arr = json.substring(idx + 1, end).split(",");
        for (String item : arr) {
            String key = item.replace("\"", "").replace("'", "").trim().toUpperCase();
            if (StringUtils.isNotBlank(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /** 正确选项：从统计表反查，拿不到就返回空（不影响得分，只影响「易错项」标记） */
    private Set<String> parseRightKeys(Long questionId) {
        List<StatExamQuestionOption> list = optionMapper.selectList(
            Wrappers.lambdaQuery(StatExamQuestionOption.class)
                .eq(StatExamQuestionOption::getQuestionId, questionId)
                .eq(StatExamQuestionOption::getIsCorrect, "1"));
        return list.stream().map(StatExamQuestionOption::getOptionKey).collect(java.util.stream.Collectors.toSet());
    }

    private String buildCategoryPath(Long categoryId, Map<Long, String> nameMap, Map<Long, Long> parentMap) {
        List<String> names = new ArrayList<>();
        Long cur = categoryId;
        int guard = 0;
        while (ObjectUtil.isNotNull(cur) && cur > 0 && guard++ < 10) {
            String name = nameMap.get(cur);
            if (StringUtils.isBlank(name)) {
                break;
            }
            names.add(0, name);
            cur = parentMap.get(cur);
        }
        return String.join(" / ", names);
    }

    private String plainText(String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }
        return html.replaceAll("<[^>]+>", "").replace("&nbsp;", " ").trim();
    }

    private String plainAnswer(String answerContent) {
        if (StringUtils.isBlank(answerContent)) {
            return "";
        }
        Set<String> keys = parseChoiceKeys(answerContent);
        if (CollUtil.isNotEmpty(keys)) {
            List<String> sorted = new ArrayList<>(keys);
            sorted.sort(Comparator.naturalOrder());
            return String.join("、", sorted);
        }
        return plainText(answerContent);
    }

    /** 题目维度累计器 */
    private static class QuestionAgg {
        private BigDecimal fullScore = BigDecimal.ZERO;
        private BigDecimal scoreSum = BigDecimal.ZERO;
        private BigDecimal maxScore = BigDecimal.ZERO;
        private BigDecimal minScore = BigDecimal.valueOf(Long.MAX_VALUE);
        private int answerCount;
        private int blankCount;
        private int correctCount;
        private int partialCount;
        private int wrongCount;
        private int zeroCount;
        private int fullCount;
        private int highCorrect;
        private int lowCorrect;
        private BigDecimal discrimination;
        private final Set<Long> correctUsers = new HashSet<>();
        private final Set<Long> wrongUsers = new HashSet<>();
        private final Map<String, Integer> optionCount = new LinkedHashMap<>();
    }

    /** 知识点维度累计器 */
    private static class KnowledgeAgg {
        private int questionCount;
        private int answerCount;
        private int wrongCount;
        private BigDecimal fullScore = BigDecimal.ZERO;
        private BigDecimal lostScore = BigDecimal.ZERO;
        private final Set<Long> userSet = new HashSet<>();
    }
}
