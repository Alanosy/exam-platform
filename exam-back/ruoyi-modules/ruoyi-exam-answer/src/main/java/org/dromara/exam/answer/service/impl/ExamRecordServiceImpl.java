package org.dromara.exam.answer.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.exam.answer.domain.ExamAnswer;
import org.dromara.exam.answer.domain.ExamRecord;
import org.dromara.exam.answer.domain.bo.AnswerSaveBo;
import org.dromara.exam.answer.domain.bo.ExamRecordBo;
import org.dromara.exam.answer.domain.vo.ExamAnswerJudgeVo;
import org.dromara.exam.answer.domain.vo.ExamCenterVo;
import org.dromara.exam.answer.domain.vo.ExamOptionVo;
import org.dromara.exam.answer.domain.vo.ExamPaperVo;
import org.dromara.exam.answer.domain.vo.ExamQuestionVo;
import org.dromara.exam.answer.domain.vo.ExamRecordVo;
import org.dromara.exam.answer.domain.vo.ExamResultQuestionVo;
import org.dromara.exam.answer.domain.vo.ExamResultVo;
import org.dromara.exam.answer.mapper.ExamAnswerMapper;
import org.dromara.exam.answer.mapper.ExamRecordMapper;
import org.dromara.exam.answer.service.IExamRecordService;
import org.dromara.exam.cert.api.RemoteCertService;
import org.dromara.exam.cert.api.domain.RemoteCertIssueBo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.manage.api.RemoteExamService;
import org.dromara.exam.manage.api.domain.RemoteExamInviteVo;
import org.dromara.exam.manage.api.domain.RemoteExamVo;
import org.dromara.exam.mark.api.RemoteMarkService;
import org.dromara.exam.mark.api.domain.RemoteMarkItemSyncBo;
import org.dromara.exam.mark.api.domain.RemoteMarkSyncBo;
import org.dromara.exam.paper.api.RemotePaperService;
import org.dromara.exam.paper.api.domain.RemotePaperQuestionVo;
import org.dromara.exam.paper.api.domain.RemotePaperVo;
import org.dromara.exam.practice.api.RemoteWrongQuestionService;
import org.dromara.exam.practice.api.domain.RemoteWrongQuestionBo;
import org.dromara.exam.proctor.api.RemoteProctorService;
import org.dromara.exam.question.api.RemoteQuestionService;
import org.dromara.exam.question.api.domain.RemoteQuestionOptionVo;
import org.dromara.exam.question.api.domain.RemoteQuestionVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试答卷Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExamRecordServiceImpl implements IExamRecordService {

    /** 考试已归档 */
    private static final String EXAM_ARCHIVED = "archived";

    /** 考试已结束 */
    private static final String EXAM_FINISHED = "finished";

    /** 客观题：单选 */
    private static final String TYPE_SINGLE = "SINGLE";

    /** 客观题：多选 */
    private static final String TYPE_MULTIPLE = "MULTIPLE";

    /** 客观题：判断 */
    private static final String TYPE_JUDGE = "JUDGE";

    /** 填空 */
    private static final String TYPE_BLANK = "BLANK";

    /** 交卷后即可看答案 */
    private static final String SHOW_AFTER_SUBMIT = "after_submit";

    /** 考试结束后才看答案 */
    private static final String SHOW_AFTER_EXAM = "after_exam";

    /** 错题本来源：正式考试，与错题表的 source_type 取值保持一致 */
    private static final String WRONG_SOURCE_EXAM = "EXAM";

    /** 任务类型：正式考试 */
    private static final String EXAM_TYPE_FORMAL = "1";

    /** 任务类型：练习考试（刷题） */
    private static final String EXAM_TYPE_PRACTICE = "2";

    /** 答案展示：即时露出，刷题时做完一题立刻看答案与解析 */
    private static final String SHOW_IMMEDIATE = "immediate";

    /**
     * 入场缓冲（秒）
     *
     * <p>考生要等页面刷新、点按钮、请求还要跑一趟，人不可能卡在整秒入场。
     * 「迟到不可参加」管的是晚到几分钟的人，不是晚一秒就把守时的人踢出去，
     * 所以不管是否允许迟到，最晚入场时间都在开始时间上再留这段缓冲。
     */
    private static final long ENTRY_GRACE_SECONDS = 60L;

    /**
     * 提前入场余量（秒）
     *
     * <p>考生电脑的时钟和服务端差几秒很常见，差这几秒不该把已经守在页面前的人挡住。
     */
    private static final long EARLY_GRACE_SECONDS = 10L;

    private final ExamRecordMapper baseMapper;

    private final ExamAnswerMapper examAnswerMapper;

    @DubboReference
    private RemoteExamService remoteExamService;

    @DubboReference
    private RemotePaperService remotePaperService;

    @DubboReference
    private RemoteQuestionService remoteQuestionService;

    @DubboReference
    private RemoteWrongQuestionService remoteWrongQuestionService;

    @DubboReference
    private RemoteMarkService remoteMarkService;

    @DubboReference
    private RemoteProctorService remoteProctorService;

    @DubboReference
    private RemoteCertService remoteCertService;

    /* ---------------------------------- 考试中心 ---------------------------------- */

    @Override
    public List<ExamCenterVo> listMyCenter() {
        String account = currentAccount();
        Long userId = LoginHelper.getUserId();
        // 考试中心 = 我创建的 + 我加入的：自己建的不用拿链接加入，直接能看到、能开考
        Set<Long> ownExamIds = new LinkedHashSet<>();
        List<Long> created = remoteExamService.listExamIdsByCreator(userId);
        if (CollUtil.isNotEmpty(created)) {
            ownExamIds.addAll(created);
        }
        List<RemoteExamInviteVo> invites = remoteExamService.listInvitesByAccount(account);
        Set<Long> examIds = new LinkedHashSet<>(ownExamIds);
        if (CollUtil.isNotEmpty(invites)) {
            // 同一场考试可能有多条邀请记录（短信 + 链接），按考试去重
            examIds.addAll(invites.stream().map(RemoteExamInviteVo::getExamId).filter(ObjectUtil::isNotNull).toList());
        }
        // 白名单：被指派到的考试不用拿链接，直接出现在考试中心里
        List<Long> whiteExamIds = remoteExamService.listExamIdsByWhiteUser(userId);
        if (CollUtil.isNotEmpty(whiteExamIds)) {
            examIds.addAll(whiteExamIds);
        }
        if (examIds.isEmpty()) {
            return List.of();
        }

        List<ExamCenterVo> list = new ArrayList<>();
        Date now = new Date();
        for (Long examId : examIds) {
            RemoteExamVo exam = remoteExamService.queryExam(examId);
            if (ObjectUtil.isNull(exam)) {
                continue;
            }
            List<ExamRecord> records = selectMyRecords(examId, account);
            // 中途退出再进列表时顺手把超时的答卷收掉，避免列表一直显示「答题中」
            autoSubmitExpired(records, exam, now);

            ExamCenterVo vo = new ExamCenterVo();
            vo.setExamId(exam.getExamId());
            vo.setExamName(exam.getExamName());
            vo.setExamType(exam.getExamType());
            vo.setExamDesc(exam.getExamDesc());
            vo.setStartTime(exam.getStartTime());
            vo.setEndTime(exam.getEndTime());
            vo.setDuration(exam.getDuration());
            vo.setExamStatus(exam.getStatus());
            vo.setAttemptCount(records.size());
            // 把服务端当前时间带回去：前端倒计时以它为准，避免考生电脑时钟不准导致按钮早亮/晚亮
            vo.setServerTime(now);
            // 最晚入场时间：前端据此提示「最晚 xx:xx 前入场」
            vo.setLatestEntryTime(latestEntryTime(exam));

            ExamRecord answering = records.stream().filter(r -> ExamRecord.STATUS_ANSWERING.equals(r.getStatus())).findFirst().orElse(null);
            List<ExamRecord> submittedList = records.stream().filter(r -> ExamRecord.STATUS_SUBMITTED.equals(r.getStatus())).toList();
            ExamRecord lastSubmitted = submittedList.isEmpty() ? null : submittedList.get(submittedList.size() - 1);

            vo.setRecordId(ObjectUtil.isNotNull(answering) ? answering.getId() : (ObjectUtil.isNull(lastSubmitted) ? null : lastSubmitted.getId()));
            if (ObjectUtil.isNotNull(lastSubmitted)) {
                vo.setTotalScore(lastSubmitted.getTotalScore());
                vo.setPassScore(lastSubmitted.getPassScore());
                vo.setPassed(ObjectUtil.equal(1L, lastSubmitted.getPassed()));
            }

            // 按优先级判定「我现在能做什么」
            String myStatus;
            String tip = null;
            boolean canStart = false;
            if (ObjectUtil.isNotNull(answering)) {
                myStatus = "answering";
                canStart = true;
            } else if (EXAM_ARCHIVED.equals(exam.getStatus())) {
                myStatus = ObjectUtil.isNull(lastSubmitted) ? "blocked" : "submitted";
                tip = ObjectUtil.isNull(lastSubmitted) ? "该考试已归档" : null;
            } else if (isAfter(now, exam.getEndTime()) || EXAM_FINISHED.equals(exam.getStatus())) {
                myStatus = ObjectUtil.isNull(lastSubmitted) ? "ended" : "submitted";
                tip = ObjectUtil.isNull(lastSubmitted) ? "考试已结束，你未参加" : null;
            } else if (isNotStarted(now, exam)) {
                myStatus = "not_start";
                // 具体开考时间由前端倒计时体现，这里只给一句话的结论
                tip = "考试尚未开始";
            } else if (isLate(now, exam)) {
                myStatus = ObjectUtil.isNull(lastSubmitted) ? "late" : "submitted";
                tip = ObjectUtil.isNull(lastSubmitted) ? "已超过最晚入场时间，无法参加本次考试" : null;
            } else if (!canRetry(exam, submittedList.size())) {
                myStatus = "blocked";
                tip = "已达到该考试允许的考试次数";
            } else {
                myStatus = "pending";
                canStart = true;
            }
            vo.setMyStatus(myStatus);
            vo.setTip(tip);
            vo.setCanStart(canStart);
            vo.setOwner(ownExamIds.contains(examId) || isOwner(exam));
            list.add(vo);
        }

        // 能考的排前面：答题中 > 待考试 > 未开始 > 已交卷 > 已结束
        Map<String, Integer> order = Map.of("answering", 0, "pending", 1, "not_start", 2, "submitted", 3, "late", 4, "ended", 5, "blocked", 6);
        list.sort(Comparator.comparing((ExamCenterVo vo) -> order.getOrDefault(vo.getMyStatus(), 9))
            .thenComparing(ExamCenterVo::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    /* ---------------------------------- 考试记录 ---------------------------------- */

    @Override
    public TableDataInfo<ExamRecordVo> listMyRecords(ExamRecordBo bo, PageQuery pageQuery) {
        String account = currentAccount();
        // 查询条件可能整个不传（GET 无参时 Spring 也会给个空对象，这里再兜一层）
        if (ObjectUtil.isNull(bo)) {
            bo = new ExamRecordBo();
        }
        LambdaQueryWrapper<ExamRecord> lqw = Wrappers.lambdaQuery();
        // 只查自己的：账号一律走登录态，不信前端传的
        lqw.eq(ExamRecord::getAccount, account);
        lqw.eq(ObjectUtil.isNotNull(bo.getExamId()), ExamRecord::getExamId, bo.getExamId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ExamRecord::getStatus, bo.getStatus());
        // 考试类型不在本库，先把这类考试的ID捞回来再下推到 SQL，保证分页条数准确
        if (StringUtils.isNotBlank(bo.getExamType())) {
            List<Long> examIds = remoteExamService.listExamIdsByType(bo.getExamType());
            if (CollUtil.isEmpty(examIds)) {
                return TableDataInfo.build();
            }
            lqw.in(ExamRecord::getExamId, examIds);
        }
        // 注意：Boolean 不能直接三元拆箱，未传时为 null 会 NPE
        lqw.eq(ObjectUtil.isNotNull(bo.getPassed()), ExamRecord::getPassed, Boolean.TRUE.equals(bo.getPassed()) ? 1L : 0L);
        lqw.orderByDesc(ExamRecord::getSubmitTime).orderByDesc(ExamRecord::getStartTime).orderByDesc(ExamRecord::getId);

        Page<ExamRecord> page = baseMapper.selectPage(pageQuery.build(), lqw);
        List<ExamRecord> records = page.getRecords();
        List<ExamRecordVo> voList = new ArrayList<>();
        if (CollUtil.isNotEmpty(records)) {
            // 考试名 / 试卷名都不在本库，同一次查询里按 ID 缓存，避免一行一次 Dubbo 调用
            Map<Long, RemoteExamVo> examMap = new HashMap<>();
            Map<Long, RemotePaperVo> paperMap = new HashMap<>();
            Map<Long, List<ExamAnswer>> answersMap = selectAnswersOf(records.stream().map(ExamRecord::getId).toList());
            for (ExamRecord record : records) {
                RemoteExamVo exam = ObjectUtil.isNull(record.getExamId()) ? null
                    : examMap.computeIfAbsent(record.getExamId(), remoteExamService::queryExam);
                RemotePaperVo paper = ObjectUtil.isNull(record.getPaperId()) ? null
                    : paperMap.computeIfAbsent(record.getPaperId(), remotePaperService::queryPaper);
                List<ExamAnswer> answers = answersMap.getOrDefault(record.getId(), List.of());
                voList.add(toRecordVo(record, exam, paper, answers));
            }
        }
        Page<ExamRecordVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return TableDataInfo.build(voPage);
    }

    /**
     * 一次性取回这些答卷的所有作答，按答卷分组
     */
    private Map<Long, List<ExamAnswer>> selectAnswersOf(List<Long> recordIds) {
        if (CollUtil.isEmpty(recordIds)) {
            return Map.of();
        }
        List<ExamAnswer> answers = examAnswerMapper.selectList(
            Wrappers.lambdaQuery(ExamAnswer.class).in(ExamAnswer::getRecordId, recordIds));
        return answers.stream().collect(Collectors.groupingBy(ExamAnswer::getRecordId));
    }

    /**
     * 答卷 → 考试记录行
     */
    private ExamRecordVo toRecordVo(ExamRecord record, RemoteExamVo exam, RemotePaperVo paper, List<ExamAnswer> answers) {
        ExamRecordVo vo = new ExamRecordVo();
        vo.setRecordId(record.getId());
        vo.setExamId(record.getExamId());
        vo.setExamName(ObjectUtil.isNull(exam) ? null : exam.getExamName());
        vo.setExamType(ObjectUtil.isNull(exam) ? null : exam.getExamType());
        vo.setPaperId(record.getPaperId());
        vo.setPaperName(ObjectUtil.isNull(paper) ? null : paper.getPaperName());
        vo.setAttemptNo(record.getAttemptNo());
        vo.setStatus(record.getStatus());
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setUsedSeconds(record.getUsedSeconds());
        vo.setDurationMinutes(record.getDurationMinutes());
        vo.setQuestionCount(record.getQuestionCount());
        vo.setAnsweredCount(record.getAnsweredCount());
        vo.setCorrectCount((int) answers.stream().filter(a -> Integer.valueOf(ExamAnswer.CORRECT_YES).equals(a.getCorrect())).count());
        vo.setWrongCount((int) answers.stream().filter(a -> Integer.valueOf(ExamAnswer.CORRECT_NO).equals(a.getCorrect())).count());
        vo.setObjectiveScore(record.getObjectiveScore());
        vo.setSubjectiveScore(record.getSubjectiveScore());
        vo.setTotalScore(record.getTotalScore());
        vo.setPaperTotalScore(paperTotalScore(paper));
        vo.setPassScore(record.getPassScore());
        vo.setPassed(ObjectUtil.equal(1L, record.getPassed()));
        vo.setAutoSubmit(ObjectUtil.equal(1L, record.getAutoSubmit()));
        vo.setShowAnswer(ObjectUtil.isNotNull(exam) && showAnswer(exam));
        return vo;
    }

    /**
     * 试卷总分，试卷被删时退回 0，避免前端显示 undefined
     */
    private BigDecimal paperTotalScore(RemotePaperVo paper) {
        if (ObjectUtil.isNull(paper)) {
            return null;
        }
        return BigDecimal.valueOf(ObjectUtil.defaultIfNull(paper.getTotalScore(), 0L));
    }

    /* ----------------------------------- 开考 ----------------------------------- */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startExam(Long examId) {
        String account = currentAccount();
        RemoteExamVo exam = remoteExamService.queryExam(examId);
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("考试不存在或已删除");
        }
        if (EXAM_ARCHIVED.equals(exam.getStatus())) {
            throw new ServiceException("该考试已归档，无法参加");
        }
        // 准入：自己建的考试直接放行；其余人要么在白名单里，要么已经有邀请 / 加入记录
        boolean owner = isOwner(exam);
        if (!owner) {
            // 白名单考生不走加入链接，自然没有邀请记录，这里认名单，不去查 invite
            boolean whitelisted = Boolean.TRUE.equals(remoteExamService.isWhiteUser(examId, LoginHelper.getUserId()));
            if (!whitelisted) {
                RemoteExamInviteVo invite = remoteExamService.queryInvite(examId, account);
                if (ObjectUtil.isNull(invite)) {
                    throw new ServiceException("你还没有加入该考试，请先通过邀请链接加入");
                }
            }
        }

        List<ExamRecord> records = selectMyRecords(examId, account);
        // 中途退出续答：直接续上原来那份答卷，倒计时接着走。
        //
        // 入场规则（未开始 / 最晚入场时间）管的是「第一次进场」，续答的人早就进场了，
        // 所以这段必须放在下面的时间校验之前返回：否则答到一半退出，回来就被一句
        // 「已超过最晚入场时间」挡在门外，手里的答卷还交不掉。
        // 至于「考试已结束」：这里同样放行，进入答题页时 getPaper() 会立刻按结束时间 /
        // 剩余时长自动交卷并提示，不会让人真的继续答下去，也不会把答卷悬在「答题中」。
        ExamRecord answering = records.stream().filter(r -> ExamRecord.STATUS_ANSWERING.equals(r.getStatus())).findFirst().orElse(null);
        if (ObjectUtil.isNotNull(answering)) {
            return answering.getId();
        }

        Date now = new Date();
        if (isNotStarted(now, exam)) {
            throw new ServiceException("考试尚未开始");
        }
        if (isAfter(now, exam.getEndTime()) || EXAM_FINISHED.equals(exam.getStatus())) {
            throw new ServiceException("考试已结束");
        }
        if (isLate(now, exam)) {
            throw new ServiceException("已超过最晚入场时间，无法参加本次考试");
        }

        long submittedCount = records.stream().filter(r -> ExamRecord.STATUS_SUBMITTED.equals(r.getStatus())).count();
        if (!canRetry(exam, (int) submittedCount)) {
            throw new ServiceException("已达到该考试允许的考试次数");
        }

        RemotePaperVo paper = remotePaperService.queryPaper(exam.getPaperId());
        if (ObjectUtil.isNull(paper)) {
            throw new ServiceException("该考试关联的试卷不存在，请联系管理员");
        }
        List<RemotePaperQuestionVo> paperQuestions = remotePaperService.listQuestions(exam.getPaperId());
        if (CollUtil.isEmpty(paperQuestions)) {
            throw new ServiceException("该考试还没有题目，无法开始");
        }

        ExamRecord record = new ExamRecord();
        record.setExamId(exam.getExamId());
        record.setPaperId(exam.getPaperId());
        record.setUserId(LoginHelper.getUserId());
        record.setAccount(account);
        record.setAttemptNo(records.size() + 1);
        record.setStatus(ExamRecord.STATUS_ANSWERING);
        record.setStartTime(now);
        // 限时是「一场活动」的规则，只看考试上配的 duration；没配就是不限时（0）
        long duration = ObjectUtil.isNotNull(exam.getDuration()) && exam.getDuration() > 0 ? exam.getDuration() : 0L;
        record.setDurationMinutes(duration);
        record.setQuestionCount(paperQuestions.size());
        record.setAnsweredCount(0);
        record.setPassScore(BigDecimal.valueOf(ObjectUtil.defaultIfNull(paper.getPassScore(), 0L)));
        record.setObjectiveScore(BigDecimal.ZERO);
        record.setSubjectiveScore(BigDecimal.ZERO);
        record.setTotalScore(BigDecimal.ZERO);
        record.setPassed(0L);
        record.setAutoSubmit(0L);
        baseMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamAnswerJudgeVo judgeAnswer(Long recordId, AnswerSaveBo bo) {
        String account = currentAccount();
        ExamRecord record = selectOwnRecord(recordId, account);
        if (!ExamRecord.STATUS_ANSWERING.equals(record.getStatus())) {
            throw new ServiceException("该答卷已提交，无法继续作答");
        }
        RemoteExamVo exam = remoteExamService.queryExam(record.getExamId());
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("考试不存在或已删除");
        }
        // 只有刷题模式才允许提前判题，正式考试必须等交卷，否则答案就是提前泄了
        if (!isRealTimeJudge(exam)) {
            throw new ServiceException("本场考试不支持即时判题，请交卷后查看答案");
        }
        RemoteQuestionVo question = questionOf(record.getPaperId(), bo.getQuestionId());
        if (ObjectUtil.isNull(question)) {
            throw new ServiceException("该题目已被删除，无法判题");
        }
        // 先把作答落库，判完无论对错这份答案都算数
        saveAnswer(recordId, bo);

        boolean objective = isObjective(question.getQuestionType());
        RemotePaperVo paper = remotePaperService.queryPaper(record.getPaperId());
        // 部分分先看考试自己的配置，考试没开就退回试卷配置，两者都关才要求全对
        boolean partialScore = partialScoreOf(exam, paper);
        boolean right = objective && judge(question, bo.getAnswerContent(), partialScore);

        // 本题的对错标记先写进去，页面上「已经答过哪些题」能立即反映出来；
        // 分数不在这里算，交卷时会整体重算一遍，避免两处口径打架
        ExamAnswer answer = examAnswerMapper.selectOne(
            Wrappers.lambdaQuery(ExamAnswer.class)
                .eq(ExamAnswer::getRecordId, recordId)
                .eq(ExamAnswer::getQuestionId, bo.getQuestionId())
                .last("limit 1"));
        if (ObjectUtil.isNotNull(answer) && objective) {
            answer.setCorrect(right ? ExamAnswer.CORRECT_YES : ExamAnswer.CORRECT_NO);
            examAnswerMapper.updateById(answer);
        }

        ExamAnswerJudgeVo vo = new ExamAnswerJudgeVo();
        vo.setQuestionId(bo.getQuestionId());
        vo.setCorrect(objective ? right : null);
        vo.setMyAnswerText(toAnswerText(question.getQuestionType(), bo.getAnswerContent(), false));
        vo.setStandardAnswerText(toAnswerText(question.getQuestionType(), question.getAnswer(), true));
        vo.setAnalysis(question.getAnalysis());
        if (!objective) {
            vo.setMessage("主观题不自动判分，请对照参考答案与解析自行检查");
        } else {
            vo.setMessage(right ? "回答正确" : "回答错误，看看解析再来一次");
        }
        return vo;
    }

    /**
     * 取试卷里的某一道题
     *
     * @param paperId    试卷ID
     * @param questionId 题目ID
     * @return 题目，不存在或不属于这张卷时返回 null
     */
    private RemoteQuestionVo questionOf(Long paperId, Long questionId) {
        // 先确认这道题真的在这张卷子里，否则拿到别的卷子的题也能判分了
        List<RemotePaperQuestionVo> paperQuestions = remotePaperService.listQuestions(paperId);
        boolean belongs = paperQuestions.stream().anyMatch(item -> item.getQuestionId().equals(questionId));
        if (!belongs) {
            return null;
        }
        List<RemoteQuestionVo> questions = remoteQuestionService.listByIds(List.of(questionId));
        return CollUtil.isEmpty(questions) ? null : questions.get(0);
    }

    /* ---------------------------------- 答题页 ---------------------------------- */

    @Override
    public ExamPaperVo getPaper(Long recordId) {
        String account = currentAccount();
        ExamRecord record = selectOwnRecord(recordId, account);
        RemoteExamVo exam = remoteExamService.queryExam(record.getExamId());
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("考试不存在或已删除");
        }
        if (!ExamRecord.STATUS_ANSWERING.equals(record.getStatus())) {
            throw new ServiceException("该答卷已提交，无法继续答题");
        }
        // 时间到了：先自动交卷，再告诉前端去成绩页，保证状态不悬在「答题中」
        if (remainingSeconds(record) == 0 || isAfter(new Date(), exam.getEndTime())) {
            doSubmit(record, exam, true);
            throw new ServiceException("考试时间已结束，系统已自动交卷");
        }

        RemotePaperVo paper = remotePaperService.queryPaper(record.getPaperId());
        if (ObjectUtil.isNull(paper)) {
            throw new ServiceException("该考试关联的试卷不存在，请联系管理员");
        }
        List<RemotePaperQuestionVo> paperQuestions = remotePaperService.listQuestions(record.getPaperId());
        List<Long> questionIds = paperQuestions.stream().map(RemotePaperQuestionVo::getQuestionId).toList();
        List<RemoteQuestionVo> questions = remoteQuestionService.listByIds(questionIds);
        Map<Long, RemoteQuestionVo> questionMap = questions.stream()
            .collect(Collectors.toMap(RemoteQuestionVo::getQuestionId, item -> item, (a, b) -> a));

        // 已作答内容
        Map<Long, ExamAnswer> answerMap = selectAnswerMap(recordId);

        // 乱序：用 recordId 做随机种子，保证同一份答卷多次进入顺序一致
        List<RemotePaperQuestionVo> ordered = new ArrayList<>(paperQuestions);
        if ("1".equals(paper.getQuestionShuffle())) {
            Collections.shuffle(ordered, new Random(recordId));
        }
        boolean optionShuffle = "1".equals(paper.getOptionShuffle());

        List<ExamQuestionVo> voList = new ArrayList<>(ordered.size());
        int sort = 1;
        for (RemotePaperQuestionVo item : ordered) {
            RemoteQuestionVo question = questionMap.get(item.getQuestionId());
            if (ObjectUtil.isNull(question)) {
                // 题目被删了就跳过，避免整份卷子打不开
                continue;
            }
            ExamQuestionVo vo = new ExamQuestionVo();
            vo.setQuestionId(question.getQuestionId());
            vo.setQuestionType(question.getQuestionType());
            vo.setTitle(question.getTitle());
            vo.setScore(scoreOf(item, question));
            vo.setSort(sort++);
            if (CollUtil.isNotEmpty(question.getOptions())) {
                List<RemoteQuestionOptionVo> options = new ArrayList<>(question.getOptions());
                if (optionShuffle) {
                    Collections.shuffle(options, new Random(recordId + question.getQuestionId()));
                }
                vo.setOptions(options.stream().map(option -> {
                    ExamOptionVo optionVo = new ExamOptionVo();
                    optionVo.setOptionKey(option.getOptionKey());
                    optionVo.setOptionContent(option.getOptionContent());
                    return optionVo;
                }).toList());
            }
            ExamAnswer answer = answerMap.get(question.getQuestionId());
            vo.setMyAnswer(ObjectUtil.isNull(answer) ? null : answer.getAnswerContent());
            voList.add(vo);
        }

        ExamPaperVo paperVo = new ExamPaperVo();
        paperVo.setRecordId(record.getId());
        paperVo.setExamId(record.getExamId());
        paperVo.setExamName(exam.getExamName());
        paperVo.setExamType(exam.getExamType());
        paperVo.setRealTimeJudge(isRealTimeJudge(exam));
        paperVo.setPaperName(paper.getPaperName());
        paperVo.setTotalScore(BigDecimal.valueOf(ObjectUtil.defaultIfNull(paper.getTotalScore(), 0L)));
        paperVo.setQuestions(voList);
        paperVo.setRemainingSeconds(remainingSeconds(record));
        paperVo.setStartTime(record.getStartTime() == null ? null : String.valueOf(record.getStartTime().getTime()));
        return paperVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAnswer(Long recordId, AnswerSaveBo bo) {
        String account = currentAccount();
        ExamRecord record = selectOwnRecord(recordId, account);
        if (!ExamRecord.STATUS_ANSWERING.equals(record.getStatus())) {
            throw new ServiceException("该答卷已提交，无法继续作答");
        }
        if (remainingSeconds(record) == 0) {
            throw new ServiceException("考试时间已结束，无法继续作答");
        }

        ExamAnswer exist = examAnswerMapper.selectOne(
            Wrappers.lambdaQuery(ExamAnswer.class)
                .eq(ExamAnswer::getRecordId, recordId)
                .eq(ExamAnswer::getQuestionId, bo.getQuestionId())
                .last("limit 1"));
        if (ObjectUtil.isNotNull(exist)) {
            exist.setAnswerContent(bo.getAnswerContent());
            examAnswerMapper.updateById(exist);
        } else {
            ExamAnswer add = new ExamAnswer();
            add.setRecordId(recordId);
            add.setQuestionId(bo.getQuestionId());
            add.setAnswerContent(bo.getAnswerContent());
            add.setScore(BigDecimal.ZERO);
            add.setCorrect(ExamAnswer.CORRECT_UNKNOWN);
            long count = examAnswerMapper.selectCount(Wrappers.lambdaQuery(ExamAnswer.class).eq(ExamAnswer::getRecordId, recordId));
            add.setSort((int) count + 1);
            examAnswerMapper.insert(add);
            // 已答题数只在首次作答时累加
            ExamRecord update = new ExamRecord();
            update.setId(recordId);
            update.setAnsweredCount(ObjectUtil.defaultIfNull(record.getAnsweredCount(), 0) + 1);
            baseMapper.updateById(update);
        }
    }

    /* ----------------------------------- 交卷 ----------------------------------- */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamResultVo submit(Long recordId) {
        String account = currentAccount();
        ExamRecord record = selectOwnRecord(recordId, account);
        RemoteExamVo exam = remoteExamService.queryExam(record.getExamId());
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("考试不存在或已删除");
        }
        if (!ExamRecord.STATUS_ANSWERING.equals(record.getStatus())) {
            // 重复点交卷：直接把已有成绩返回，不重复判分
            return buildResult(record, exam);
        }
        doSubmit(record, exam, remainingSeconds(record) == 0);
        return buildResult(baseMapper.selectById(recordId), exam);
    }

    @Override
    public ExamResultVo getResult(Long recordId) {
        String account = currentAccount();
        ExamRecord record = selectOwnRecord(recordId, account);
        RemoteExamVo exam = remoteExamService.queryExam(record.getExamId());
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("考试不存在或已删除");
        }
        return buildResult(record, exam);
    }

    @Override
    public int autoSubmitExpired() {
        List<ExamRecord> answering = baseMapper.selectList(
            Wrappers.lambdaQuery(ExamRecord.class).eq(ExamRecord::getStatus, ExamRecord.STATUS_ANSWERING));
        if (CollUtil.isEmpty(answering)) {
            return 0;
        }
        Date now = new Date();
        int count = 0;
        for (ExamRecord record : answering) {
            RemoteExamVo exam = remoteExamService.queryExam(record.getExamId());
            if (ObjectUtil.isNull(exam)) {
                continue;
            }
            boolean timeout = remainingSeconds(record, now) == 0 || isAfter(now, exam.getEndTime());
            if (!timeout) {
                continue;
            }
            try {
                doSubmit(record, exam, true);
                count++;
            } catch (Exception e) {
                log.warn("自动交卷失败 recordId={}, {}", record.getId(), e.getMessage());
            }
        }
        return count;
    }

    /* ---------------------------------- 私有方法 ---------------------------------- */

    /**
     * 当前登录账号，作为考生在本服务里的身份标识
     */
    private String currentAccount() {
        String account = LoginHelper.getUsername();
        if (StringUtils.isBlank(account)) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        return account;
    }

    /**
     * 取自己的答卷，顺便挡掉越权访问
     */
    private ExamRecord selectOwnRecord(Long recordId, String account) {
        ExamRecord record = baseMapper.selectById(recordId);
        if (ObjectUtil.isNull(record)) {
            throw new ServiceException("答卷不存在");
        }
        if (!StringUtils.equals(account, record.getAccount())) {
            throw new ServiceException("无权查看他人答卷");
        }
        return record;
    }

    /**
     * 我在某场考试上的所有答卷，按次数升序
     */
    private List<ExamRecord> selectMyRecords(Long examId, String account) {
        return baseMapper.selectList(
            Wrappers.lambdaQuery(ExamRecord.class)
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getAccount, account)
                .orderByAsc(ExamRecord::getAttemptNo));
    }

    private Map<Long, ExamAnswer> selectAnswerMap(Long recordId) {
        List<ExamAnswer> answers = examAnswerMapper.selectList(
            Wrappers.lambdaQuery(ExamAnswer.class).eq(ExamAnswer::getRecordId, recordId));
        Map<Long, ExamAnswer> map = new HashMap<>();
        for (ExamAnswer answer : answers) {
            map.put(answer.getQuestionId(), answer);
        }
        return map;
    }

    /**
     * 剩余秒数，0 表示已超时，-1 表示不限时
     */
    private long remainingSeconds(ExamRecord record) {
        return remainingSeconds(record, new Date());
    }

    private long remainingSeconds(ExamRecord record, Date now) {
        if (ObjectUtil.isNull(record.getDurationMinutes()) || record.getDurationMinutes() <= 0) {
            return -1L;
        }
        if (ObjectUtil.isNull(record.getStartTime())) {
            return -1L;
        }
        long used = (now.getTime() - record.getStartTime().getTime()) / 1000;
        long total = record.getDurationMinutes() * 60;
        return Math.max(0, total - used);
    }

    private boolean isAfter(Date now, Date target) {
        return ObjectUtil.isNotNull(target) && now.after(target);
    }

    /**
     * 是否还没开考
     *
     * <p>留 EARLY_GRACE_SECONDS 秒余量，抵消考生机器与服务端之间的时钟差；
     * 没配开始时间（练习考试）一律当作早已开始。
     */
    private boolean isNotStarted(Date now, RemoteExamVo exam) {
        Date startTime = exam.getStartTime();
        if (ObjectUtil.isNull(startTime)) {
            return false;
        }
        return now.getTime() < startTime.getTime() - EARLY_GRACE_SECONDS * 1000L;
    }

    /**
     * 最晚入场时间
     *
     * <p>允许迟到 = 开始时间 + 允许的迟到分钟；不允许迟到 = 开始时间。
     * 两种情况都再叠加入场缓冲，见 ENTRY_GRACE_SECONDS 的说明。
     */
    private Date latestEntryTime(RemoteExamVo exam) {
        if (ObjectUtil.isNull(exam.getStartTime())) {
            return null;
        }
        long lateMinute = ObjectUtil.equal(1L, exam.getAllowLate()) ? ObjectUtil.defaultIfNull(exam.getLateMinute(), 0L) : 0L;
        return new Date(exam.getStartTime().getTime() + lateMinute * 60_000L + ENTRY_GRACE_SECONDS * 1000L);
    }

    /**
     * 迟到判定：超过最晚入场时间才算迟到
     *
     * <p>原来「不允许迟到」是过了开考时间就一票否决，结果整点守在页面前的考生
     * 因为刷新慢一两秒就被判迟到，实际上没人能卡在整秒入场，这里统一改成看最晚入场时间。
     */
    private boolean isLate(Date now, RemoteExamVo exam) {
        Date latest = latestEntryTime(exam);
        if (ObjectUtil.isNull(latest) || isNotStarted(now, exam)) {
            return false;
        }
        return now.getTime() > latest.getTime();
    }

    /**
     * 是否还能再考一次
     */
    private boolean canRetry(RemoteExamVo exam, int submittedCount) {
        // 练习考试不限制次数：刷题本来就该反复练，进多少次它就多一份练习记录
        if (isPractice(exam)) {
            return true;
        }
        boolean allowRetry = ObjectUtil.equal(1L, exam.getAllowRetry());
        if (!allowRetry) {
            return submittedCount == 0;
        }
        int max = ObjectUtil.defaultIfNull(exam.getMaxRetryCount(), 1L).intValue();
        // maxRetryCount 记的是「最多考几次」，按 1 次兜底
        return submittedCount < Math.max(max, 1);
    }

    /**
     * 是否我创建的考试
     *
     * <p>创建人不用走邀请链接：考试中心直接列出，开考也直接放行；
     * 只有别人参加的考试才需要先用链接加入。
     */
    private boolean isOwner(RemoteExamVo exam) {
        return ObjectUtil.isNotNull(exam)
            && ObjectUtil.isNotNull(exam.getCreatorId())
            && ObjectUtil.equal(exam.getCreatorId(), LoginHelper.getUserId());
    }

    /**
     * 是否练习考试（刷题）
     *
     * <p>练习考试放宽两处限制：不卡「几次机会」，起止时间可以留空长期有效。
     * 时间相关判定（isBefore / isAfter / isLate）本身对空值返回 false，天然就是长期有效。
     */
    private boolean isPractice(RemoteExamVo exam) {
        return ObjectUtil.isNotNull(exam) && EXAM_TYPE_PRACTICE.equals(exam.getExamType());
    }

    /**
     * 是否刷题即时判题模式：做完一题立刻给答案与解析
     */
    private boolean isRealTimeJudge(RemoteExamVo exam) {
        return ObjectUtil.isNotNull(exam) && SHOW_IMMEDIATE.equals(exam.getShowAnswerMode());
    }

    /**
     * 批量自动交卷（列表页兜底，只对超时或考试已结束的答卷生效）
     */
    private void autoSubmitExpired(List<ExamRecord> records, RemoteExamVo exam, Date now) {
        for (ExamRecord record : records) {
            if (!ExamRecord.STATUS_ANSWERING.equals(record.getStatus())) {
                continue;
            }
            if (remainingSeconds(record, now) == 0 || isAfter(now, exam.getEndTime())) {
                try {
                    doSubmit(record, exam, true);
                } catch (Exception e) {
                    log.warn("自动交卷失败 recordId={}, {}", record.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 交卷 + 客观题自动判分
     */
    private void doSubmit(ExamRecord record, RemoteExamVo exam, boolean auto) {
        RemotePaperVo paper = remotePaperService.queryPaper(record.getPaperId());
        List<RemotePaperQuestionVo> paperQuestions = ObjectUtil.isNull(paper)
            ? List.of() : remotePaperService.listQuestions(record.getPaperId());
        List<Long> questionIds = paperQuestions.stream().map(RemotePaperQuestionVo::getQuestionId).toList();
        List<RemoteQuestionVo> questions = remoteQuestionService.listByIds(questionIds);
        Map<Long, RemoteQuestionVo> questionMap = questions.stream()
            .collect(Collectors.toMap(RemoteQuestionVo::getQuestionId, item -> item, (a, b) -> a));
        Map<Long, ExamAnswer> answerMap = selectAnswerMap(record.getId());

        boolean autoJudge = ObjectUtil.isNull(paper) || !"0".equals(paper.getAutoJudge());
        // 部分分以考试配置为准：同一张卷子给不同班级考，可能一个要求全对一个允许部分得分
        boolean partialScore = partialScoreOf(exam, paper);
        int partialRate = partialRateOf(exam);

        BigDecimal objective = BigDecimal.ZERO;
        // 判完分顺手把答错的题收集起来，交卷收尾时一次性同步到错题本
        List<RemoteWrongQuestionBo> wrongList = new ArrayList<>();
        // 主观题自动判不了，收进阅卷列表等老师打分
        List<RemoteMarkItemSyncBo> markList = new ArrayList<>();
        for (ExamAnswer answer : answerMap.values()) {
            RemoteQuestionVo question = questionMap.get(answer.getQuestionId());
            if (ObjectUtil.isNull(question)) {
                continue;
            }
            BigDecimal fullScore = scoreOf(paperQuestions.stream()
                .filter(item -> item.getQuestionId().equals(question.getQuestionId())).findFirst().orElse(null), question);
            if (autoJudge && isObjective(question.getQuestionType())) {
                boolean right = judge(question, answer.getAnswerContent(), partialScore);
                BigDecimal gained = right
                    ? fullScore
                    : gainedScore(question, answer.getAnswerContent(), fullScore, partialScore, partialRate);
                // 半对（得分了但没拿满）要单独标记：统计里它既不能算「答对」也不该算「答错」
                answer.setCorrect(right ? ExamAnswer.CORRECT_YES
                    : (gained.compareTo(BigDecimal.ZERO) > 0 ? ExamAnswer.CORRECT_PARTIAL : ExamAnswer.CORRECT_NO));
                answer.setScore(gained);
                objective = objective.add(answer.getScore());
                if (!right) {
                    // 答错与半对（部分得分但没拿满）都进错题本
                    wrongList.add(toWrongBo(record, answer));
                }
            } else {
                // 主观题留待人工阅卷，分数保持 0
                answer.setCorrect(ExamAnswer.CORRECT_UNKNOWN);
                answer.setScore(BigDecimal.ZERO);
                markList.add(toMarkItemBo(answer, question.getQuestionType(), fullScore));
            }
            examAnswerMapper.updateById(answer);
        }

        ExamRecord update = new ExamRecord();
        update.setId(record.getId());
        update.setStatus(ExamRecord.STATUS_SUBMITTED);
        update.setSubmitTime(new Date());
        update.setUsedSeconds(record.getStartTime() == null ? 0
            : (int) ((update.getSubmitTime().getTime() - record.getStartTime().getTime()) / 1000));
        update.setObjectiveScore(objective);
        update.setSubjectiveScore(BigDecimal.ZERO);
        update.setTotalScore(objective);
        update.setAutoSubmit(auto ? 1L : 0L);
        BigDecimal passScore = ObjectUtil.defaultIfNull(record.getPassScore(), BigDecimal.ZERO);
        boolean passed = objective.compareTo(passScore) >= 0;
        update.setPassed(passed ? 1L : 0L);
        update.setAnsweredCount(answerMap.size());
        baseMapper.updateById(update);
        record.setStatus(ExamRecord.STATUS_SUBMITTED);
        record.setSubmitTime(update.getSubmitTime());
        record.setTotalScore(update.getTotalScore());
        record.setPassed(update.getPassed());
        // 成绩落库后再同步错题与阅卷任务，下游写失败不能把交卷结果带崩
        syncWrongQuestions(record, wrongList);
        syncMarkQuestions(record, objective, markList);
        // 顺手通知防作弊服务收掉监考会话：考生交完卷直接关浏览器时也不会一直挂着「作答中」
        finishProctor(record);
        // 没有主观题时成绩到这里就定了，可以发证书；
        // 含主观题的卷子要等阅卷完成（RemoteExamAnswerServiceImpl#writeBackMark）再发，
        // 否则证书上写的是客观题分，考生阅完卷分数变了，证书就对不上成绩了
        if (passed && CollUtil.isEmpty(markList)) {
            issueCertificate(record, exam.getExamName(), objective, paperTotalScore(paper));
        }
    }

    /**
     * 及格颁发证书
     *
     * <p>证书服务没部署、这场考试没配证书、模板被停用，这些都只是发不出证书，
     * 绝不能影响交卷本身，所以异常一律吞掉只记 warn。
     *
     * @param examName   考试名称，写进证书正文
     * @param score      最终得分
     * @param totalScore 试卷总分
     */
    private void issueCertificate(ExamRecord record, String examName, BigDecimal score, BigDecimal totalScore) {
        try {
            RemoteCertIssueBo bo = new RemoteCertIssueBo();
            bo.setExamId(record.getExamId());
            bo.setExamName(examName);
            bo.setRecordId(record.getId());
            bo.setUserId(record.getUserId());
            bo.setAccount(record.getAccount());
            bo.setAttemptNo(record.getAttemptNo());
            bo.setScore(score);
            bo.setPassScore(ObjectUtil.defaultIfNull(record.getPassScore(), BigDecimal.ZERO));
            bo.setTotalScore(totalScore);
            bo.setPassed(Boolean.TRUE);
            bo.setSubmitTime(ObjectUtil.defaultIfNull(record.getSubmitTime(), new Date()));
            // 跨服务调用拿不到租户上下文，显式带过去
            bo.setTenantId(TenantHelper.getTenantId());
            remoteCertService.issueOnPass(bo);
        } catch (Exception e) {
            log.warn("颁发证书失败 recordId={}, {}", record.getId(), e.getMessage());
        }
    }

    /**
     * 交卷后结束监考会话
     *
     * <p>防作弊服务挂了或者没部署都不能影响交卷，所以异常只记 warn。
     */
    private void finishProctor(ExamRecord record) {
        try {
            remoteProctorService.finishSession(record.getId(), "submitted");
        } catch (Exception e) {
            log.warn("结束监考会话失败 recordId={}, {}", record.getId(), e.getMessage());
        }
    }

    /**
     * 交卷后把主观题推进阅卷列表
     *
     * <p>客观题已经自动判完分，这里只送主观题；没有主观题的卷子不建任务，
     * 免得阅卷列表里出现一堆 0 题待阅的空任务。
     */
    private void syncMarkQuestions(ExamRecord record, BigDecimal objectiveScore, List<RemoteMarkItemSyncBo> markList) {
        if (CollUtil.isEmpty(markList) || ObjectUtil.isNull(record.getUserId())) {
            return;
        }
        RemoteMarkSyncBo bo = new RemoteMarkSyncBo();
        bo.setRecordId(record.getId());
        bo.setExamId(record.getExamId());
        bo.setPaperId(record.getPaperId());
        bo.setUserId(record.getUserId());
        bo.setAccount(record.getAccount());
        bo.setAttemptNo(record.getAttemptNo());
        bo.setSubmitTime(new Date());
        bo.setObjectiveScore(ObjectUtil.defaultIfNull(objectiveScore, BigDecimal.ZERO));
        bo.setPassScore(ObjectUtil.defaultIfNull(record.getPassScore(), BigDecimal.ZERO));
        // 跨服务调用拿不到租户上下文，显式带过去
        bo.setTenantId(TenantHelper.getTenantId());
        bo.setItems(markList);
        try {
            remoteMarkService.syncSubjective(bo);
        } catch (Exception e) {
            log.warn("同步阅卷任务失败 recordId={}, 主观题数={}, {}", record.getId(), markList.size(), e.getMessage());
        }
    }

    /**
     * 作答记录 → 待阅主观题
     */
    private RemoteMarkItemSyncBo toMarkItemBo(ExamAnswer answer, String questionType, BigDecimal fullScore) {
        RemoteMarkItemSyncBo bo = new RemoteMarkItemSyncBo();
        bo.setQuestionId(answer.getQuestionId());
        bo.setQuestionType(questionType);
        bo.setSort(answer.getSort());
        bo.setAnswerContent(answer.getAnswerContent());
        bo.setFullScore(fullScore);
        return bo;
    }

    /**
     * 交卷后把客观题的错题同步到错题本
     *
     * <p>主观题要等人工 / AI 阅卷判定，由阅卷服务调同一个接口回写，这里不处理。
     */
    private void syncWrongQuestions(ExamRecord record, List<RemoteWrongQuestionBo> wrongList) {
        // 访客答卷没有用户ID，按约定不做错题本
        if (CollUtil.isEmpty(wrongList) || ObjectUtil.isNull(record.getUserId())) {
            return;
        }
        try {
            remoteWrongQuestionService.syncWrong(wrongList);
        } catch (Exception e) {
            log.warn("同步错题本失败 recordId={}, 错题数={}, {}", record.getId(), wrongList.size(), e.getMessage());
        }
    }

    /**
     * 作答记录 → 错题同步入参
     */
    private RemoteWrongQuestionBo toWrongBo(ExamRecord record, ExamAnswer answer) {
        RemoteWrongQuestionBo bo = new RemoteWrongQuestionBo();
        bo.setUserId(record.getUserId());
        bo.setQuestionId(answer.getQuestionId());
        bo.setSourceType(WRONG_SOURCE_EXAM);
        bo.setSourceId(record.getExamId());
        bo.setAnswerItemId(answer.getId());
        return bo;
    }

    /**
     * 组装成绩详情
     */
    private ExamResultVo buildResult(ExamRecord record, RemoteExamVo exam) {
        RemotePaperVo paper = remotePaperService.queryPaper(record.getPaperId());
        List<RemotePaperQuestionVo> paperQuestions = ObjectUtil.isNull(paper)
            ? List.of() : remotePaperService.listQuestions(record.getPaperId());
        List<Long> questionIds = paperQuestions.stream().map(RemotePaperQuestionVo::getQuestionId).toList();
        List<RemoteQuestionVo> questions = remoteQuestionService.listByIds(questionIds);
        Map<Long, RemoteQuestionVo> questionMap = questions.stream()
            .collect(Collectors.toMap(RemoteQuestionVo::getQuestionId, item -> item, (a, b) -> a));
        Map<Long, ExamAnswer> answerMap = selectAnswerMap(record.getId());

        // 字段名（id -> recordId）与类型（Long -> Boolean）都不一致，这里直接手工组装，不走对象转换
        ExamResultVo vo = new ExamResultVo();
        vo.setRecordId(record.getId());
        vo.setExamId(record.getExamId());
        vo.setStatus(record.getStatus());
        vo.setObjectiveScore(record.getObjectiveScore());
        vo.setSubjectiveScore(record.getSubjectiveScore());
        vo.setTotalScore(record.getTotalScore());
        vo.setPassScore(record.getPassScore());
        vo.setPassed(ObjectUtil.equal(1L, record.getPassed()));
        vo.setUsedSeconds(record.getUsedSeconds());
        vo.setExamName(exam.getExamName());
        vo.setPaperId(record.getPaperId());
        vo.setPaperName(ObjectUtil.isNull(paper) ? null : paper.getPaperName());
        vo.setPaperTotalScore(paperTotalScore(paper));
        vo.setAttemptNo(record.getAttemptNo());
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setDurationMinutes(record.getDurationMinutes());
        vo.setQuestionCount(record.getQuestionCount());
        vo.setAnsweredCount(record.getAnsweredCount());
        vo.setCorrectCount((int) answerMap.values().stream().filter(a -> Integer.valueOf(ExamAnswer.CORRECT_YES).equals(a.getCorrect())).count());
        vo.setWrongCount((int) answerMap.values().stream().filter(a -> Integer.valueOf(ExamAnswer.CORRECT_NO).equals(a.getCorrect())).count());
        vo.setAutoSubmit(ObjectUtil.equal(1L, record.getAutoSubmit()));
        vo.setShowAnswer(showAnswer(exam));

        List<ExamResultQuestionVo> voList = new ArrayList<>();
        int sort = 1;
        for (RemotePaperQuestionVo item : paperQuestions) {
            RemoteQuestionVo question = questionMap.get(item.getQuestionId());
            if (ObjectUtil.isNull(question)) {
                continue;
            }
            ExamAnswer answer = answerMap.get(question.getQuestionId());
            String myAnswer = ObjectUtil.isNull(answer) ? null : answer.getAnswerContent();
            ExamResultQuestionVo q = new ExamResultQuestionVo();
            q.setQuestionId(question.getQuestionId());
            q.setQuestionType(question.getQuestionType());
            q.setTitle(question.getTitle());
            q.setSort(sort++);
            q.setScore(scoreOf(item, question));
            q.setMyAnswer(myAnswer);
            q.setMyAnswerText(toAnswerText(question.getQuestionType(), myAnswer, false));
            // 选项原样带回，前端才能把 A/B/C 还原成可读的选项内容
            q.setOptions(ObjectUtil.isNull(question.getOptions()) ? null : question.getOptions().stream().map(option -> {
                ExamOptionVo optionVo = new ExamOptionVo();
                optionVo.setOptionKey(option.getOptionKey());
                optionVo.setOptionContent(option.getOptionContent());
                return optionVo;
            }).toList());
            if (ObjectUtil.isNotNull(answer)) {
                q.setGainedScore(answer.getScore());
                q.setCorrect(answer.getCorrect());
            } else {
                q.setGainedScore(BigDecimal.ZERO);
                q.setCorrect(ExamAnswer.CORRECT_UNKNOWN);
            }
            if (Boolean.TRUE.equals(vo.getShowAnswer())) {
                q.setStandardAnswer(question.getAnswer());
                q.setStandardAnswerText(toAnswerText(question.getQuestionType(), question.getAnswer(), true));
                q.setAnalysis(question.getAnalysis());
            }
            voList.add(q);
        }
        vo.setQuestions(voList);
        return vo;
    }

    /**
     * 答案 JSON → 人话
     *
     * <p>存的是结构化 JSON（{choices:[A]} / {blanks:[{text}]} / {rightKeys:[A]} / {text}），
     * 直接给考生看是天书，这里按题型翻成人能读的字符串。
     *
     * @param questionType 题型
     * @param json         答案JSON
     * @param standard     true 处理参考答案，false 处理考生作答
     */
    private String toAnswerText(String questionType, String json, boolean standard) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        if (TYPE_BLANK.equals(questionType)) {
            BlankAnswer parsed = parse(json, BlankAnswer.class);
            if (ObjectUtil.isNull(parsed) || CollUtil.isEmpty(parsed.getBlanks())) {
                return plainText(json);
            }
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < parsed.getBlanks().size(); i++) {
                BlankItem blank = parsed.getBlanks().get(i);
                String text = standard
                    ? (CollUtil.isEmpty(blank.getAnswers()) ? "" : String.join(" / ", blank.getAnswers()))
                    : blank.getText();
                parts.add("第" + (i + 1) + "空：" + (StringUtils.isBlank(text) ? "未作答" : text));
            }
            return String.join("；", parts);
        }
        if (TYPE_SINGLE.equals(questionType) || TYPE_MULTIPLE.equals(questionType) || TYPE_JUDGE.equals(questionType)) {
            OptionAnswer parsed = parse(json, OptionAnswer.class);
            if (ObjectUtil.isNull(parsed)) {
                return plainText(json);
            }
            List<String> keys = standard ? parsed.getRightKeys() : parsed.getChoices();
            if (CollUtil.isEmpty(keys)) {
                return standard ? null : "未作答";
            }
            return String.join("、", keys);
        }
        return plainText(json);
    }

    /**
     * 主观题：答案可能是 {text} / {answer}，也可能压根不是 JSON
     */
    private String plainText(String json) {
        TextAnswer parsed = parse(json, TextAnswer.class);
        if (ObjectUtil.isNotNull(parsed)) {
            if (StringUtils.isNotBlank(parsed.getText())) {
                return parsed.getText();
            }
            if (StringUtils.isNotBlank(parsed.getAnswer())) {
                return parsed.getAnswer();
            }
        }
        return json;
    }

    /**
     * 是否展示答案与解析
     */
    private boolean showAnswer(RemoteExamVo exam) {
        String mode = exam.getShowAnswerMode();
        if (SHOW_AFTER_SUBMIT.equals(mode)) {
            return true;
        }
        if (SHOW_AFTER_EXAM.equals(mode)) {
            return isAfter(new Date(), exam.getEndTime()) || EXAM_FINISHED.equals(exam.getStatus());
        }
        return false;
    }

    private boolean isObjective(String questionType) {
        return TYPE_SINGLE.equals(questionType) || TYPE_MULTIPLE.equals(questionType)
            || TYPE_JUDGE.equals(questionType) || TYPE_BLANK.equals(questionType);
    }

    /**
     * 本题分值：试卷里单独配了就用试卷的，否则用题目默认分
     */
    private BigDecimal scoreOf(RemotePaperQuestionVo paperQuestion, RemoteQuestionVo question) {
        if (ObjectUtil.isNotNull(paperQuestion) && ObjectUtil.isNotNull(paperQuestion.getScore()) && paperQuestion.getScore() > 0) {
            return BigDecimal.valueOf(paperQuestion.getScore());
        }
        return ObjectUtil.defaultIfNull(question.getScore(), BigDecimal.ZERO);
    }

    /**
     * 客观题判分
     */
    private boolean judge(RemoteQuestionVo question, String answerContent, boolean partialScore) {
        if (StringUtils.isBlank(answerContent)) {
            return false;
        }
        if (TYPE_BLANK.equals(question.getQuestionType())) {
            BlankAnswer standard = parse(question.getAnswer(), BlankAnswer.class);
            BlankAnswer mine = parse(answerContent, BlankAnswer.class);
            if (ObjectUtil.isNull(standard) || CollUtil.isEmpty(standard.getBlanks())
                || ObjectUtil.isNull(mine) || CollUtil.isEmpty(mine.getBlanks())) {
                return false;
            }
            if (standard.getBlanks().size() != mine.getBlanks().size()) {
                return false;
            }
            for (int i = 0; i < standard.getBlanks().size(); i++) {
                Set<String> accepted = standard.getBlanks().get(i).getAnswers() == null
                    ? Set.of() : standard.getBlanks().get(i).getAnswers().stream().map(this::normalize).collect(Collectors.toSet());
                String mineText = mine.getBlanks().get(i).getText();
                if (!accepted.contains(normalize(mineText))) {
                    return false;
                }
            }
            return true;
        }
        // 单选 / 多选 / 判断
        OptionAnswer standard = parse(question.getAnswer(), OptionAnswer.class);
        OptionAnswer mine = parse(answerContent, OptionAnswer.class);
        if (ObjectUtil.isNull(standard) || CollUtil.isEmpty(standard.getRightKeys()) || ObjectUtil.isNull(mine)) {
            return false;
        }
        Set<String> right = standard.getRightKeys().stream().map(this::normalize).collect(Collectors.toSet());
        Set<String> mineKeys = CollUtil.isEmpty(mine.getChoices()) ? Set.of()
            : mine.getChoices().stream().map(this::normalize).collect(Collectors.toSet());
        return right.equals(mineKeys);
    }

    /**
     * 部分得分开关：考试配置优先，考试没开就退回试卷配置
     *
     * <p>一场考试的判分规则应该由「考试」这一层决定——同一张试卷给两个班考，
     * 一个要求全对才给分、一个允许部分得分，是很常见的诉求。
     */
    private boolean partialScoreOf(RemoteExamVo exam, RemotePaperVo paper) {
        if (ObjectUtil.isNotNull(exam) && ObjectUtil.isNotNull(exam.getPartialScore())) {
            return "1".equals(exam.getPartialScore());
        }
        return ObjectUtil.isNotNull(paper) && "1".equals(paper.getPartialScore());
    }

    /**
     * 部分正确时的得分比例（%），默认 100 表示按命中比例给分
     */
    private int partialRateOf(RemoteExamVo exam) {
        if (ObjectUtil.isNull(exam) || ObjectUtil.isNull(exam.getPartialScoreRate()) || exam.getPartialScoreRate() <= 0) {
            return 100;
        }
        return Math.min(exam.getPartialScoreRate(), 100);
    }

    /**
     * 部分得分：多选题按命中比例给分，填空题按答对的空数给分
     *
     * <p>两条硬规则，防止「部分分」被钻空子：
     * <ol>
     *     <li>多选题只要选中了任何一个错误选项，一律 0 分。
     *         否则四个选项全选必然命中所有正确项，等于蒙题也能拿满分。</li>
     *     <li>一个都没命中（hit = 0）也是 0 分，不会出现「全错还得分」。</li>
     * </ol>
     * rate &lt; 100 时（如配 50）只要不是全对就一律给该题满分的固定比例。
     */
    private BigDecimal gainedScore(RemoteQuestionVo question, String answerContent, BigDecimal fullScore,
                                   boolean partialScore, int rate) {
        if (judge(question, answerContent, partialScore)) {
            return fullScore;
        }
        if (!partialScore) {
            return BigDecimal.ZERO;
        }
        if (TYPE_BLANK.equals(question.getQuestionType())) {
            BlankAnswer standard = parse(question.getAnswer(), BlankAnswer.class);
            BlankAnswer mine = parse(answerContent, BlankAnswer.class);
            if (ObjectUtil.isNull(standard) || CollUtil.isEmpty(standard.getBlanks())
                || ObjectUtil.isNull(mine) || CollUtil.isEmpty(mine.getBlanks())) {
                return BigDecimal.ZERO;
            }
            int hit = 0;
            int size = Math.min(standard.getBlanks().size(), mine.getBlanks().size());
            for (int i = 0; i < size; i++) {
                Set<String> accepted = standard.getBlanks().get(i).getAnswers() == null
                    ? Set.of() : standard.getBlanks().get(i).getAnswers().stream().map(this::normalize).collect(Collectors.toSet());
                if (accepted.contains(normalize(mine.getBlanks().get(i).getText()))) {
                    hit++;
                }
            }
            if (hit == 0) {
                return BigDecimal.ZERO;
            }
            // 考生填的空比标准答案多，多出来的算错：填空题不允许靠「把所有可能的答案都写进去」拿满分
            if (mine.getBlanks().size() > standard.getBlanks().size()) {
                return BigDecimal.ZERO;
            }
            if (rate < 100) {
                return fullScore.multiply(BigDecimal.valueOf(rate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            return fullScore.multiply(BigDecimal.valueOf(hit)).divide(BigDecimal.valueOf(standard.getBlanks().size()), 2, RoundingMode.HALF_UP);
        }
        OptionAnswer standard = parse(question.getAnswer(), OptionAnswer.class);
        OptionAnswer mine = parse(answerContent, OptionAnswer.class);
        if (ObjectUtil.isNull(standard) || CollUtil.isEmpty(standard.getRightKeys()) || ObjectUtil.isNull(mine)) {
            return BigDecimal.ZERO;
        }
        Set<String> right = standard.getRightKeys().stream().map(this::normalize).collect(Collectors.toSet());
        Set<String> mineKeys = CollUtil.isEmpty(mine.getChoices()) ? Set.of()
            : mine.getChoices().stream().map(this::normalize).collect(Collectors.toSet());
        // 错选直接清零：选中了正确答案之外的任何选项，本题不得分
        boolean hasWrong = mineKeys.stream().anyMatch(key -> !right.contains(key));
        if (hasWrong) {
            return BigDecimal.ZERO;
        }
        long hit = mineKeys.stream().filter(right::contains).count();
        if (hit == 0) {
            return BigDecimal.ZERO;
        }
        if (rate < 100) {
            return fullScore.multiply(BigDecimal.valueOf(rate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return fullScore.multiply(BigDecimal.valueOf(hit)).divide(BigDecimal.valueOf(right.size()), 2, RoundingMode.HALF_UP);
    }

    private <T> T parse(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return JsonUtils.parseObject(json, clazz);
        } catch (Exception e) {
            log.warn("解析答案JSON失败 json={}", json);
            return null;
        }
    }

    /**
     * 答案归一化：去空格、转大写，避免大小写与空白影响判分
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return StrUtil.trim(value).replaceAll("\\s+", "").toUpperCase();
    }

    /**
     * 选项题参考答案 {rightKeys:[A,B]}
     */
    @lombok.Data
    public static class OptionAnswer {
        private List<String> rightKeys;
        private List<String> choices;
    }

    /**
     * 填空题参考答案 {blanks:[{answers:[..]}]}；考生作答 {blanks:[{text:".."}]}
     */
    @lombok.Data
    public static class BlankAnswer {
        private List<BlankItem> blanks;
    }

    @lombok.Data
    public static class BlankItem {
        private List<String> answers;
        private String text;
    }

    /**
     * 主观题答案 {text:".."}，参考答案有时写成 {answer:".."}
     */
    @lombok.Data
    public static class TextAnswer {
        private String text;
        private String answer;
    }
}
