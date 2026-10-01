package org.dromara.exam.mark.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RemoteMarkScoreBo;
import org.dromara.exam.answer.api.domain.RemoteMarkWriteBackBo;
import org.dromara.exam.manage.api.RemoteExamService;
import org.dromara.exam.manage.api.domain.RemoteExamVo;
import org.dromara.exam.mark.api.domain.RemoteMarkItemSyncBo;
import org.dromara.exam.mark.api.domain.RemoteMarkSyncBo;
import org.dromara.exam.mark.domain.MarkItem;
import org.dromara.exam.mark.domain.MarkLog;
import org.dromara.exam.mark.domain.MarkTask;
import org.dromara.exam.mark.domain.bo.MarkExamBo;
import org.dromara.exam.mark.domain.bo.MarkScoreBo;
import org.dromara.exam.mark.domain.bo.MarkTaskBo;
import org.dromara.exam.mark.domain.vo.MarkExamVo;
import org.dromara.exam.mark.domain.vo.MarkLogVo;
import org.dromara.exam.mark.domain.vo.MarkOptionVo;
import org.dromara.exam.mark.domain.vo.MarkQuestionVo;
import org.dromara.exam.mark.domain.vo.MarkTaskVo;
import org.dromara.exam.mark.mapper.MarkItemMapper;
import org.dromara.exam.mark.mapper.MarkLogMapper;
import org.dromara.exam.mark.mapper.MarkTaskMapper;
import org.dromara.exam.mark.service.IMarkService;
import org.dromara.exam.mark.service.ai.MarkAiBo;
import org.dromara.exam.mark.service.ai.MarkAiResult;
import org.dromara.exam.mark.service.ai.MarkAiService;
import org.dromara.exam.paper.api.RemotePaperService;
import org.dromara.exam.paper.api.domain.RemotePaperVo;
import org.dromara.exam.practice.api.RemoteWrongQuestionService;
import org.dromara.exam.practice.api.domain.RemoteWrongQuestionBo;
import org.dromara.exam.question.api.RemoteQuestionService;
import org.dromara.exam.question.api.domain.RemoteQuestionOptionVo;
import org.dromara.exam.question.api.domain.RemoteQuestionVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.system.api.RemoteUserService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 阅卷Service业务层处理
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MarkServiceImpl implements IMarkService {

    /** 只统计正式考试：练习刷题是即时判题，不进人工阅卷 */
    private static final String EXAM_TYPE_FORMAL = "1";

    /** 错题本来源：正式考试，与错题表 source_type 取值一致 */
    private static final String WRONG_SOURCE_EXAM = "EXAM";

    private final MarkTaskMapper markTaskMapper;

    private final MarkItemMapper markItemMapper;

    private final MarkLogMapper markLogMapper;

    @DubboReference
    private RemoteExamService remoteExamService;

    @DubboReference
    private RemotePaperService remotePaperService;

    @DubboReference
    private RemoteQuestionService remoteQuestionService;

    @DubboReference
    private RemoteExamAnswerService remoteExamAnswerService;

    @DubboReference
    private RemoteWrongQuestionService remoteWrongQuestionService;

    @DubboReference
    private RemoteUserService remoteUserService;

    private final MarkAiService markAiService;

    /* ---------------------------------- 查询 ---------------------------------- */

    /**
     * 阅卷列表：按考试聚合，只统计正式考试
     *
     * <p>考试名 / 试卷名分别在考试库与试卷库，没法一次 SQL 连表带出来，
     * 所以先把正式考试ID和有任务的考试取回来，聚合后再在内存里过滤 + 分页。
     * 考试数量天然有限（一场考试一行），分页条数按过滤后的算，依然是准的。
     */
    @Override
    public TableDataInfo<MarkExamVo> listExamPage(MarkExamBo bo, PageQuery pageQuery) {
        if (ObjectUtil.isNull(bo)) {
            bo = new MarkExamBo();
        }
        Page<MarkExamVo> page = ObjectUtil.isNull(pageQuery) ? new Page<>() : pageQuery.build();
        List<Long> formalExamIds = remoteExamService.listExamIdsByType(EXAM_TYPE_FORMAL);
        if (CollUtil.isEmpty(formalExamIds)) {
            return TableDataInfo.build(List.<MarkExamVo>of(), page);
        }
        List<MarkTask> tasks = markTaskMapper.selectList(
            Wrappers.lambdaQuery(MarkTask.class)
                .in(MarkTask::getExamId, formalExamIds)
                .eq(MarkTask::getDelFlag, 0L));
        if (CollUtil.isEmpty(tasks)) {
            return TableDataInfo.build(List.<MarkExamVo>of(), page);
        }

        // 按考试聚合
        Map<Long, List<MarkTask>> grouped = new LinkedHashMap<>();
        for (MarkTask task : tasks) {
            grouped.computeIfAbsent(task.getExamId(), key -> new ArrayList<>()).add(task);
        }
        List<MarkExamVo> voList = new ArrayList<>(grouped.size());
        for (Map.Entry<Long, List<MarkTask>> entry : grouped.entrySet()) {
            Long examId = entry.getKey();
            List<MarkTask> list = entry.getValue();
            MarkTask first = list.get(0);
            RemoteExamVo exam = remoteExamService.queryExam(examId);
            RemotePaperVo paper = remotePaperService.queryPaper(first.getPaperId());

            MarkExamVo vo = new MarkExamVo();
            vo.setExamId(examId);
            vo.setExamName(ObjectUtil.isNull(exam) ? null : exam.getExamName());
            vo.setPaperId(first.getPaperId());
            vo.setPaperName(ObjectUtil.isNull(paper) ? null : paper.getPaperName());

            long pendingTask = list.stream().filter(item -> !MarkTask.STATUS_FINISHED.equals(item.getStatus())).count();
            long finishedTask = list.size() - pendingTask;
            // 任务上的已阅题数是打分时累加的，直接汇总即可，不必再去明细表数一遍
            long itemCount = list.stream().mapToLong(item -> ObjectUtil.defaultIfNull(item.getQuestionCount(), 0)).sum();
            long markedItem = list.stream().mapToLong(item -> ObjectUtil.defaultIfNull(item.getMarkedCount(), 0)).sum();
            vo.setPendingTaskCount(pendingTask);
            vo.setFinishedTaskCount(finishedTask);
            vo.setTaskCount((long) list.size());
            vo.setItemCount(itemCount);
            vo.setMarkedItemCount(markedItem);
            vo.setPendingItemCount(Math.max(itemCount - markedItem, 0));
            vo.setProgress(itemCount == 0 ? 0 : (int) Math.round(markedItem * 100.0 / itemCount));
            voList.add(vo);
        }
        // 名称关键字过滤：考试名 / 试卷名都要等远程查回来才拿得到，只能放在这一步
        List<MarkExamVo> filtered = filterExam(voList, bo);
        return TableDataInfo.build(filtered, page);
    }

    @Override
    public TableDataInfo<MarkTaskVo> listTaskPage(MarkTaskBo bo, PageQuery pageQuery) {
        if (ObjectUtil.isNull(bo)) {
            bo = new MarkTaskBo();
        }
        MarkTaskBo finalBo = bo;
        LambdaQueryWrapper<MarkTask> lqw = Wrappers.lambdaQuery(MarkTask.class)
            .eq(ObjectUtil.isNotNull(bo.getExamId()), MarkTask::getExamId, bo.getExamId())
            .eq(ObjectUtil.isNotNull(bo.getPaperId()), MarkTask::getPaperId, bo.getPaperId())
            .eq(StringUtils.isNotBlank(bo.getStatus()), MarkTask::getStatus, bo.getStatus())
            .and(StringUtils.isNotBlank(bo.getAccount()),
                wrapper -> wrapper.like(MarkTask::getAccount, finalBo.getAccount()))
            .eq(MarkTask::getDelFlag, 0L)
            .orderByAsc(MarkTask::getStatus)
            .orderByDesc(MarkTask::getSubmitTime);
        Page<MarkTask> param = ObjectUtil.isNull(pageQuery) ? new Page<>() : pageQuery.build();
        Page<MarkTask> page = markTaskMapper.selectPage(param, lqw);
        List<MarkTaskVo> voList = toTaskVoList(page.getRecords());
        Page<MarkTaskVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return TableDataInfo.build(voPage);
    }

    /**
     * 阅卷页：主观题明细（题干 / 作答 / 参考答案 / 解析 / 分值）
     */
    @Override
    public List<MarkQuestionVo> listQuestions(Long taskId) {
        MarkTask task = selectTask(taskId);
        List<MarkItem> items = markItemMapper.selectList(
            Wrappers.lambdaQuery(MarkItem.class)
                .eq(MarkItem::getTaskId, taskId)
                .eq(MarkItem::getDelFlag, 0L)
                .orderByAsc(MarkItem::getSort));
        if (CollUtil.isEmpty(items)) {
            return List.of();
        }
        Map<Long, RemoteQuestionVo> questionMap = selectQuestionMap(items.stream().map(MarkItem::getQuestionId).toList());
        List<MarkQuestionVo> voList = new ArrayList<>(items.size());
        for (MarkItem item : items) {
            RemoteQuestionVo question = questionMap.get(item.getQuestionId());
            MarkQuestionVo vo = MapstructUtils.convert(item, MarkQuestionVo.class);
            if (ObjectUtil.isNull(vo)) {
                continue;
            }
            vo.setItemId(item.getId());
            vo.setAnswerText(toAnswerText(item.getAnswerContent()));
            vo.setQuestionTypeName(questionTypeName(item.getQuestionType()));
            if (ObjectUtil.isNotNull(question)) {
                vo.setTitle(question.getTitle());
                vo.setAnalysis(question.getAnalysis());
                vo.setStandardAnswer(question.getAnswer());
                vo.setStandardAnswerText(toAnswerText(question.getAnswer()));
                vo.setDifficulty(question.getDifficulty());
                vo.setOptions(toOptions(question.getOptions()));
                if (ObjectUtil.isNull(item.getFullScore())) {
                    vo.setFullScore(question.getScore());
                }
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<MarkLogVo> listLogs(Long taskId) {
        if (ObjectUtil.isNull(taskId)) {
            return List.of();
        }
        List<MarkLog> logs = markLogMapper.selectList(
            Wrappers.lambdaQuery(MarkLog.class)
                .eq(MarkLog::getTaskId, taskId)
                .eq(MarkLog::getDelFlag, 0L)
                .orderByDesc(MarkLog::getId));
        List<MarkLogVo> voList = new ArrayList<>(logs.size());
        for (MarkLog item : logs) {
            MarkLogVo vo = MapstructUtils.convert(item, MarkLogVo.class);
            if (ObjectUtil.isNull(vo)) {
                continue;
            }
            vo.setLogId(item.getId());
            voList.add(vo);
        }
        return voList;
    }

    /* ---------------------------------- 打分 ---------------------------------- */

    /**
     * 单题打分：改明细 → 重算任务进度 → 回写答题库 → 全阅完则收尾
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void score(MarkScoreBo bo) {
        if (ObjectUtil.isNull(bo) || ObjectUtil.isNull(bo.getItemId())) {
            throw new ServiceException("请选择要打分的题目");
        }
        MarkItem item = markItemMapper.selectById(bo.getItemId());
        if (ObjectUtil.isNull(item)) {
            throw new ServiceException("阅卷明细不存在");
        }
        MarkTask task = selectTask(item.getTaskId());
        BigDecimal fullScore = ObjectUtil.defaultIfNull(item.getFullScore(), BigDecimal.ZERO);
        BigDecimal score = Boolean.TRUE.equals(bo.getUseAiScore()) && ObjectUtil.isNotNull(item.getAiScore())
            ? item.getAiScore() : bo.getScore();
        if (ObjectUtil.isNull(score)) {
            throw new ServiceException("得分不能为空");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(fullScore) > 0) {
            throw new ServiceException("得分必须在 0 ~ " + fullScore.stripTrailingZeros().toPlainString() + " 之间");
        }
        // 没显式指定对错时，拿满分算正确，否则算错误（半对的题也进错题本）
        Integer correct = ObjectUtil.isNotNull(bo.getCorrect())
            ? bo.getCorrect() : (score.compareTo(fullScore) >= 0 ? 1 : 2);
        String markType = Boolean.TRUE.equals(bo.getUseAiScore()) && ObjectUtil.isNotNull(item.getAiScore())
            ? MarkItem.TYPE_AI : MarkItem.TYPE_MANUAL;

        boolean rescore = MarkItem.STATUS_MARKED.equals(item.getStatus());
        writeLog(task, item, rescore ? MarkLog.ACTION_RESCORE : MarkLog.ACTION_SCORE,
            item.getScore(), score, markType, bo.getMarkComment());

        item.setScore(score);
        item.setCorrect(correct);
        item.setStatus(MarkItem.STATUS_MARKED);
        item.setMarkType(markType);
        item.setMarkComment(StringUtils.defaultIfBlank(bo.getMarkComment(), ""));
        item.setMarker(LoginHelper.getUserId());
        item.setMarkerName(currentUserName());
        item.setMarkTime(new Date());
        markItemMapper.updateById(item);

        recalcTask(task);
    }

    /**
     * 确认完成阅卷
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long taskId) {
        MarkTask task = selectTask(taskId);
        if (MarkTask.STATUS_FINISHED.equals(task.getStatus())) {
            // 幂等：重复点只重发一次成绩，不重复推错题
            writeBack(task, true);
            return;
        }
        List<MarkItem> items = listItems(task.getId());
        long marked = items.stream().filter(item -> MarkItem.STATUS_MARKED.equals(item.getStatus())).count();
        if (marked < ObjectUtil.defaultIfNull(task.getQuestionCount(), 0)) {
            throw new ServiceException("还有 " + (task.getQuestionCount() - marked) + " 道题没阅，不能完成");
        }
        recalcTask(task);
    }

    /**
     * AI 批量预评：只写建议分与理由，最终得分仍由教师确认
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void aiPreview(Long taskId) {
        if (!markAiService.enabled()) {
            throw new ServiceException("AI 阅卷能力未接入");
        }
        MarkTask task = selectTask(taskId);
        List<MarkItem> items = listItems(task.getId());
        Map<Long, RemoteQuestionVo> questionMap = selectQuestionMap(items.stream().map(MarkItem::getQuestionId).toList());
        Date now = new Date();
        for (MarkItem item : items) {
            RemoteQuestionVo question = questionMap.get(item.getQuestionId());
            MarkAiBo aiBo = new MarkAiBo();
            aiBo.setQuestionId(item.getQuestionId());
            aiBo.setQuestionType(item.getQuestionType());
            aiBo.setTitle(ObjectUtil.isNull(question) ? null : plainText(question.getTitle()));
            aiBo.setStandardAnswer(ObjectUtil.isNull(question) ? null : toAnswerText(question.getAnswer()));
            aiBo.setAnalysis(ObjectUtil.isNull(question) ? null : plainText(question.getAnalysis()));
            aiBo.setAnswerText(toAnswerText(item.getAnswerContent()));
            aiBo.setFullScore(ObjectUtil.defaultIfNull(item.getFullScore(), BigDecimal.ZERO));

            MarkAiResult result;
            try {
                result = markAiService.judge(aiBo);
            } catch (Exception e) {
                log.warn("AI 预评异常 itemId={}, {}", item.getId(), e.getMessage());
                result = MarkAiResult.fail(e.getMessage());
            }
            item.setAiTime(now);
            if (ObjectUtil.isNotNull(result) && Boolean.TRUE.equals(result.getSuccess()) && ObjectUtil.isNotNull(result.getScore())) {
                BigDecimal full = ObjectUtil.defaultIfNull(item.getFullScore(), BigDecimal.ZERO);
                BigDecimal score = result.getScore();
                // 模型偶尔会超出分值区间，这里夹回 0 ~ 满分，避免出现非法分
                if (score.compareTo(BigDecimal.ZERO) < 0) {
                    score = BigDecimal.ZERO;
                }
                if (score.compareTo(full) > 0) {
                    score = full;
                }
                item.setAiScore(score);
                item.setAiReason(StringUtils.defaultIfBlank(result.getReason(), ""));
                item.setAiStatus(MarkItem.AI_SUCCESS);
                item.setAiModel(StringUtils.defaultIfBlank(result.getModel(), ""));
            } else {
                item.setAiStatus(MarkItem.AI_FAIL);
                item.setAiReason(ObjectUtil.isNull(result) ? "AI 未返回结果" : StringUtils.defaultIfBlank(result.getMessage(), "AI 评估失败"));
            }
            markItemMapper.updateById(item);
            writeLog(task, item, MarkLog.ACTION_AI, null, item.getAiScore(), MarkItem.TYPE_AI, item.getAiReason());
        }
    }

    /* ---------------------------------- 同步 ---------------------------------- */

    /**
     * 交卷后同步：一份答卷建一条任务，主观题逐条入明细
     *
     * <p>按 recordId 幂等：已有任务只补齐缺失的题目，
     * 教师打过的分不会被重复交卷冲掉。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long syncSubjective(RemoteMarkSyncBo bo) {
        if (ObjectUtil.isNull(bo) || ObjectUtil.isNull(bo.getRecordId()) || CollUtil.isEmpty(bo.getItems())) {
            return null;
        }
        MarkTask exist = markTaskMapper.selectOne(
            Wrappers.lambdaQuery(MarkTask.class)
                .eq(MarkTask::getRecordId, bo.getRecordId())
                .last("limit 1"));
        MarkTask task;
        if (ObjectUtil.isNotNull(exist)) {
            task = exist;
            task.setObjectiveScore(ObjectUtil.defaultIfNull(bo.getObjectiveScore(), BigDecimal.ZERO));
            task.setPassScore(ObjectUtil.defaultIfNull(bo.getPassScore(), BigDecimal.ZERO));
            task.setSubmitTime(bo.getSubmitTime());
        } else {
            task = new MarkTask();
            task.setExamId(bo.getExamId());
            task.setPaperId(bo.getPaperId());
            task.setRecordId(bo.getRecordId());
            task.setUserId(bo.getUserId());
            task.setAccount(bo.getAccount());
            task.setAttemptNo(bo.getAttemptNo());
            task.setObjectiveScore(ObjectUtil.defaultIfNull(bo.getObjectiveScore(), BigDecimal.ZERO));
            task.setPassScore(ObjectUtil.defaultIfNull(bo.getPassScore(), BigDecimal.ZERO));
            task.setSubjectiveScore(BigDecimal.ZERO);
            task.setTotalScore(BigDecimal.ZERO);
            task.setPassed(0L);
            task.setStatus(MarkTask.STATUS_PENDING);
            task.setSubmitTime(bo.getSubmitTime());
            task.setDelFlag(0L);
            markTaskMapper.insert(task);
        }

        List<MarkItem> existItems = listItems(task.getId());
        Map<Long, MarkItem> itemMap = new HashMap<>();
        for (MarkItem item : existItems) {
            itemMap.put(item.getQuestionId(), item);
        }
        for (RemoteMarkItemSyncBo syncItem : bo.getItems()) {
            if (ObjectUtil.isNull(syncItem) || ObjectUtil.isNull(syncItem.getQuestionId())) {
                continue;
            }
            MarkItem item = itemMap.get(syncItem.getQuestionId());
            if (ObjectUtil.isNotNull(item)) {
                // 已阅过的题不再覆盖，避免把教师打的分刷新成未阅
                if (MarkItem.STATUS_MARKED.equals(item.getStatus())) {
                    continue;
                }
                item.setAnswerContent(syncItem.getAnswerContent());
                item.setFullScore(syncItem.getFullScore());
                item.setSort(syncItem.getSort());
                markItemMapper.updateById(item);
                continue;
            }
            MarkItem add = new MarkItem();
            add.setTaskId(task.getId());
            add.setExamId(task.getExamId());
            add.setPaperId(task.getPaperId());
            add.setRecordId(task.getRecordId());
            add.setQuestionId(syncItem.getQuestionId());
            add.setQuestionType(syncItem.getQuestionType());
            add.setSort(syncItem.getSort());
            add.setFullScore(ObjectUtil.defaultIfNull(syncItem.getFullScore(), BigDecimal.ZERO));
            add.setAnswerContent(syncItem.getAnswerContent());
            add.setScore(BigDecimal.ZERO);
            add.setCorrect(0);
            add.setStatus(MarkItem.STATUS_PENDING);
            add.setMarkType("");
            add.setMarkComment("");
            add.setAiStatus(MarkItem.AI_NONE);
            add.setAiReason("");
            add.setAiModel("");
            add.setDelFlag(0L);
            markItemMapper.insert(add);
        }
        List<MarkItem> all = listItems(task.getId());
        task.setQuestionCount(all.size());
        task.setMarkedCount((int) all.stream().filter(item -> MarkItem.STATUS_MARKED.equals(item.getStatus())).count());
        task.setStatus(task.getMarkedCount() >= task.getQuestionCount() ? MarkTask.STATUS_FINISHED : MarkTask.STATUS_PENDING);
        if (MarkTask.STATUS_FINISHED.equals(task.getStatus())) {
            writeLog(task, null, MarkLog.ACTION_CREATE, null, null, MarkItem.TYPE_MANUAL, "无主观题，直接完成");
        } else {
            writeLog(task, null, MarkLog.ACTION_CREATE, null, null, MarkItem.TYPE_MANUAL, "交卷同步，共 " + task.getQuestionCount() + " 道主观题");
        }
        markTaskMapper.updateById(task);
        log.info("阅卷任务同步完成 recordId={}, taskId={}, 主观题数={}", bo.getRecordId(), task.getId(), task.getQuestionCount());
        return task.getId();
    }

    /* ---------------------------------- 私有方法 ---------------------------------- */

    /**
     * 重算任务进度并回写成绩
     */
    private void recalcTask(MarkTask task) {
        List<MarkItem> items = listItems(task.getId());
        BigDecimal subjective = BigDecimal.ZERO;
        int marked = 0;
        for (MarkItem item : items) {
            if (!MarkItem.STATUS_MARKED.equals(item.getStatus())) {
                continue;
            }
            marked++;
            subjective = subjective.add(ObjectUtil.defaultIfNull(item.getScore(), BigDecimal.ZERO));
        }
        int total = items.size();
        task.setQuestionCount(total);
        task.setMarkedCount(marked);
        task.setSubjectiveScore(subjective);
        task.setMarker(LoginHelper.getUserId());
        task.setMarkerName(currentUserName());
        task.setMarkTime(new Date());

        boolean finished = total > 0 && marked >= total;
        if (finished) {
            BigDecimal objective = ObjectUtil.defaultIfNull(task.getObjectiveScore(), BigDecimal.ZERO);
            BigDecimal totalScore = objective.add(subjective);
            task.setTotalScore(totalScore);
            task.setPassed(totalScore.compareTo(ObjectUtil.defaultIfNull(task.getPassScore(), BigDecimal.ZERO)) >= 0 ? 1L : 0L);
            task.setStatus(MarkTask.STATUS_FINISHED);
        } else {
            task.setStatus(marked > 0 ? MarkTask.STATUS_MARKING : MarkTask.STATUS_PENDING);
        }
        markTaskMapper.updateById(task);

        // 成绩回写答题库：阅完才给总分，阅到一半只更新小题分
        writeBack(task, finished);
        if (finished) {
            writeLog(task, null, MarkLog.ACTION_FINISH, null, task.getTotalScore(), MarkItem.TYPE_MANUAL, "阅卷完成，已回写成绩");
            syncWrongQuestions(task, items);
        }
    }

    /**
     * 把主观题得分回写到答题库，成绩只有这一处写入口
     */
    private void writeBack(MarkTask task, boolean finished) {
        List<MarkItem> items = listItems(task.getId());
        List<RemoteMarkScoreBo> scores = new ArrayList<>(items.size());
        for (MarkItem item : items) {
            if (!MarkItem.STATUS_MARKED.equals(item.getStatus())) {
                continue;
            }
            RemoteMarkScoreBo scoreBo = new RemoteMarkScoreBo();
            scoreBo.setQuestionId(item.getQuestionId());
            scoreBo.setScore(ObjectUtil.defaultIfNull(item.getScore(), BigDecimal.ZERO));
            scoreBo.setCorrect(item.getCorrect());
            scores.add(scoreBo);
        }
        RemoteMarkWriteBackBo bo = new RemoteMarkWriteBackBo();
        bo.setRecordId(task.getRecordId());
        bo.setSubjectiveScore(ObjectUtil.defaultIfNull(task.getSubjectiveScore(), BigDecimal.ZERO));
        bo.setScores(scores);
        bo.setFinished(finished);
        try {
            remoteExamAnswerService.writeBackMark(bo);
        } catch (Exception e) {
            // 阅卷页的分已经落库，回写失败不能把教师的活儿弄丢，记日志后续重试
            log.warn("回写成绩失败 taskId={}, recordId={}, {}", task.getId(), task.getRecordId(), e.getMessage());
        }
    }

    /**
     * 阅完的卷子：判错的题推进错题本
     *
     * <p>主观题按「是否拿到满分」判定——没拿满说明答得不全，值得再练。
     * 主动忽略过的题由错题服务自己判断，不在这里干预。
     */
    private void syncWrongQuestions(MarkTask task, List<MarkItem> items) {
        if (ObjectUtil.isNull(task.getUserId()) || CollUtil.isEmpty(items)) {
            return;
        }
        List<RemoteWrongQuestionBo> wrongList = new ArrayList<>();
        for (MarkItem item : items) {
            if (!MarkItem.STATUS_MARKED.equals(item.getStatus())) {
                continue;
            }
            BigDecimal full = ObjectUtil.defaultIfNull(item.getFullScore(), BigDecimal.ZERO);
            BigDecimal score = ObjectUtil.defaultIfNull(item.getScore(), BigDecimal.ZERO);
            if (score.compareTo(full) >= 0) {
                continue;
            }
            RemoteWrongQuestionBo bo = new RemoteWrongQuestionBo();
            bo.setUserId(task.getUserId());
            bo.setQuestionId(item.getQuestionId());
            bo.setSourceType(WRONG_SOURCE_EXAM);
            bo.setSourceId(task.getExamId());
            wrongList.add(bo);
        }
        if (CollUtil.isEmpty(wrongList)) {
            return;
        }
        try {
            remoteWrongQuestionService.syncWrong(wrongList);
        } catch (Exception e) {
            log.warn("阅卷错题入错题本失败 taskId={}, {}", task.getId(), e.getMessage());
        }
    }

    private List<MarkExamVo> filterExam(List<MarkExamVo> list, MarkExamBo bo) {
        String examKeyword = StringUtils.isNotBlank(bo.getExamName()) ? bo.getExamName().toLowerCase() : null;
        String paperKeyword = StringUtils.isNotBlank(bo.getPaperName()) ? bo.getPaperName().toLowerCase() : null;
        return list.stream()
            .filter(item -> ObjectUtil.isNull(examKeyword)
                || StringUtils.defaultIfBlank(item.getExamName(), "").toLowerCase().contains(examKeyword))
            .filter(item -> ObjectUtil.isNull(paperKeyword)
                || StringUtils.defaultIfBlank(item.getPaperName(), "").toLowerCase().contains(paperKeyword))
            .filter(item -> !Boolean.TRUE.equals(bo.getOnlyUnfinished())
                || ObjectUtil.defaultIfNull(item.getPendingTaskCount(), 0L) > 0)
            .toList();
    }

    private List<MarkTaskVo> toTaskVoList(List<MarkTask> tasks) {
        if (CollUtil.isEmpty(tasks)) {
            return List.of();
        }
        Map<Long, String> nickMap = selectNickMap(tasks.stream().map(MarkTask::getUserId).filter(ObjectUtil::isNotNull).toList());
        List<MarkTaskVo> voList = new ArrayList<>(tasks.size());
        for (MarkTask task : tasks) {
            MarkTaskVo vo = MapstructUtils.convert(task, MarkTaskVo.class);
            if (ObjectUtil.isNull(vo)) {
                continue;
            }
            vo.setTaskId(task.getId());
            vo.setUserName(nickMap.getOrDefault(task.getUserId(), task.getAccount()));
            RemoteExamVo exam = remoteExamService.queryExam(task.getExamId());
            vo.setExamName(ObjectUtil.isNull(exam) ? null : exam.getExamName());
            RemotePaperVo paper = remotePaperService.queryPaper(task.getPaperId());
            vo.setPaperName(ObjectUtil.isNull(paper) ? null : paper.getPaperName());
            voList.add(vo);
        }
        return voList;
    }

    private MarkTask selectTask(Long taskId) {
        if (ObjectUtil.isNull(taskId)) {
            throw new ServiceException("阅卷任务ID不能为空");
        }
        MarkTask task = markTaskMapper.selectById(taskId);
        if (ObjectUtil.isNull(task)) {
            throw new ServiceException("阅卷任务不存在");
        }
        return task;
    }

    private List<MarkItem> listItems(Long taskId) {
        return markItemMapper.selectList(
            Wrappers.lambdaQuery(MarkItem.class)
                .eq(MarkItem::getTaskId, taskId)
                .eq(MarkItem::getDelFlag, 0L)
                .orderByAsc(MarkItem::getSort));
    }

    private Map<Long, RemoteQuestionVo> selectQuestionMap(List<Long> questionIds) {
        if (CollUtil.isEmpty(questionIds)) {
            return Map.of();
        }
        List<RemoteQuestionVo> questions = remoteQuestionService.listByIds(questionIds);
        if (CollUtil.isEmpty(questions)) {
            return Map.of();
        }
        return questions.stream().collect(Collectors.toMap(RemoteQuestionVo::getQuestionId, item -> item, (a, b) -> a));
    }

    private Map<Long, String> selectNickMap(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Map.of();
        }
        try {
            Map<Long, String> nicks = remoteUserService.selectUserNicksByIds(userIds);
            return ObjectUtil.isNull(nicks) ? Map.of() : nicks;
        } catch (Exception e) {
            log.warn("批量查询用户昵称失败 {}", e.getMessage());
            return Map.of();
        }
    }

    private void writeLog(MarkTask task, MarkItem item, String action, BigDecimal oldScore, BigDecimal newScore,
                          String markType, String remark) {
        MarkLog logEntity = new MarkLog();
        logEntity.setTaskId(task.getId());
        logEntity.setRecordId(task.getRecordId());
        logEntity.setItemId(ObjectUtil.isNull(item) ? null : item.getId());
        logEntity.setQuestionId(ObjectUtil.isNull(item) ? null : item.getQuestionId());
        logEntity.setAction(action);
        logEntity.setOldScore(oldScore);
        logEntity.setNewScore(newScore);
        logEntity.setMarkType(markType);
        logEntity.setRemark(StringUtils.defaultIfBlank(remark, ""));
        logEntity.setOperator(LoginHelper.getUserId());
        logEntity.setOperatorName(currentUserName());
        logEntity.setDelFlag(0L);
        markLogMapper.insert(logEntity);
    }

    private String currentUserName() {
        try {
            return StringUtils.defaultIfBlank(LoginHelper.getUsername(), "系统");
        } catch (Exception e) {
            return "系统";
        }
    }

    private List<MarkOptionVo> toOptions(List<RemoteQuestionOptionVo> options) {
        if (CollUtil.isEmpty(options)) {
            return null;
        }
        return options.stream().map(option -> {
            MarkOptionVo vo = new MarkOptionVo();
            vo.setOptionKey(option.getOptionKey());
            vo.setOptionContent(option.getOptionContent());
            return vo;
        }).toList();
    }

    /**
     * 作答 / 参考答案的人话版：主观题存的是 {"text":".."}，取不到就按纯文本原样返回
     */
    private String toAnswerText(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }
        if (!content.trim().startsWith("{")) {
            return content;
        }
        try {
            TextAnswer answer = JsonUtils.parseObject(content, TextAnswer.class);
            if (ObjectUtil.isNull(answer) || ObjectUtil.isNull(answer.getText())) {
                return content;
            }
            return answer.getText();
        } catch (Exception e) {
            return content;
        }
    }

    /**
     * 主观题作答 / 参考答案结构 {"text":".."}
     */
    @lombok.Data
    public static class TextAnswer {
        private String text;
    }

    private String plainText(String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }
        return html.replaceAll("<[^>]+>", "").replace("&nbsp;", " ").trim();
    }

    private String questionTypeName(String questionType) {
        if (StringUtils.isBlank(questionType)) {
            return "其他";
        }
        return switch (questionType) {
            case "SINGLE" -> "单选题";
            case "MULTIPLE" -> "多选题";
            case "JUDGE" -> "判断题";
            case "BLANK" -> "填空题";
            case "SHORT_ANSWER" -> "简答题";
            case "ESSAY" -> "论述题";
            case "CODE" -> "代码题";
            case "UPLOAD_FILE" -> "文件上传";
            case "MATCH" -> "匹配题";
            default -> questionType;
        };
    }
}
