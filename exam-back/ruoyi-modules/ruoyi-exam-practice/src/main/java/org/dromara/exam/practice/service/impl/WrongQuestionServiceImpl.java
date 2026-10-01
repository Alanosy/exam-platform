package org.dromara.exam.practice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.manage.api.RemoteExamService;
import org.dromara.exam.manage.api.domain.RemoteExamVo;
import org.dromara.exam.paper.api.RemotePaperService;
import org.dromara.exam.paper.api.domain.RemotePaperVo;
import org.dromara.exam.practice.api.domain.RemoteWrongQuestionBo;
import org.dromara.exam.practice.domain.WrongQuestion;
import org.dromara.exam.practice.domain.WrongReviewRecord;
import org.dromara.exam.practice.domain.bo.WrongNoteBo;
import org.dromara.exam.practice.domain.bo.WrongQuestionBo;
import org.dromara.exam.practice.domain.bo.WrongReviewBo;
import org.dromara.exam.practice.domain.bo.WrongSourceBo;
import org.dromara.exam.practice.domain.vo.WrongOptionVo;
import org.dromara.exam.practice.domain.vo.WrongOverviewVo;
import org.dromara.exam.practice.domain.vo.WrongQuestionVo;
import org.dromara.exam.practice.domain.vo.WrongReviewRecordVo;
import org.dromara.exam.practice.domain.vo.WrongReviewResultVo;
import org.dromara.exam.practice.domain.vo.WrongSourceVo;
import org.dromara.exam.practice.mapper.WrongQuestionMapper;
import org.dromara.exam.practice.mapper.WrongReviewRecordMapper;
import org.dromara.exam.practice.service.IWrongQuestionService;
import org.dromara.exam.question.api.RemoteQuestionService;
import org.dromara.exam.question.api.domain.RemoteQuestionOptionVo;
import org.dromara.exam.question.api.domain.RemoteQuestionVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 错题本Service业务层处理
 *
 * <p>题目正文、选项、解析都在题库服务，考试名 / 试卷名在考试管理与试卷服务，
 * 这里统一按批补齐后再组装，避免一行一次远程调用。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WrongQuestionServiceImpl implements IWrongQuestionService {

    /** 客观题：单选 */
    private static final String TYPE_SINGLE = "SINGLE";

    /** 客观题：多选 */
    private static final String TYPE_MULTIPLE = "MULTIPLE";

    /** 客观题：判断 */
    private static final String TYPE_JUDGE = "JUDGE";

    /** 客观题：填空 */
    private static final String TYPE_BLANK = "BLANK";

    private final WrongQuestionMapper baseMapper;

    private final WrongReviewRecordMapper wrongReviewRecordMapper;

    @DubboReference
    private RemoteQuestionService remoteQuestionService;

    @DubboReference
    private RemotePaperService remotePaperService;

    @DubboReference
    private RemoteExamService remoteExamService;

    /* ---------------------------------- 查询 ---------------------------------- */

    @Override
    public TableDataInfo<WrongQuestionVo> listMyWrong(WrongQuestionBo bo, PageQuery pageQuery) {
        Long userId = currentUserId();
        if (ObjectUtil.isNull(bo)) {
            bo = new WrongQuestionBo();
        }
        LambdaQueryWrapper<WrongQuestion> lqw = Wrappers.lambdaQuery();
        // 只查自己的，不信前端传的用户ID
        lqw.eq(WrongQuestion::getUserId, userId);
        lqw.eq(ObjectUtil.isNotNull(bo.getQuestionId()), WrongQuestion::getQuestionId, bo.getQuestionId());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceType()), WrongQuestion::getSourceType, bo.getSourceType());
        // 来源ID非法（0 / 负数）时宁可不过滤，也不要拼出一个永远查不到的条件
        lqw.eq(ObjectUtil.isNotNull(bo.getSourceId()) && bo.getSourceId() > 0, WrongQuestion::getSourceId, bo.getSourceId());
        if (StringUtils.isNotBlank(bo.getMasterStatus())) {
            lqw.eq(WrongQuestion::getMasterStatus, bo.getMasterStatus());
        } else if (!Boolean.TRUE.equals(bo.getIncludeIgnored())) {
            // 默认不展示已忽略的，不然用户移出去的题又跑回来。
            // 注意要显式兼容 NULL：SQL 里 NULL <> 'IGNORED' 结果是 NULL，会连历史数据一起漏掉，
            // 导致首页按来源统计出来的条数和明细页对不上
            lqw.and(wrapper -> wrapper.isNull(WrongQuestion::getMasterStatus)
                .or()
                .ne(WrongQuestion::getMasterStatus, WrongQuestion.MASTER_IGNORED));
        }
        lqw.orderByDesc(WrongQuestion::getLastWrongTime).orderByDesc(WrongQuestion::getId);

        Page<WrongQuestion> page = baseMapper.selectPage(pageQuery.build(), lqw);
        List<WrongQuestionVo> voList = toVoList(page.getRecords());
        Page<WrongQuestionVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return TableDataInfo.build(voPage);
    }

    @Override
    public WrongOverviewVo overview() {
        Long userId = currentUserId();
        List<WrongQuestion> all = selectMyAll(userId);

        WrongOverviewVo vo = new WrongOverviewVo();
        long notMaster = 0L;
        long mastered = 0L;
        long ignored = 0L;
        Date todayStart = DateUtil.beginOfDay(new Date());
        Date weekStart = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -6));
        long today = 0L;
        long week = 0L;
        for (WrongQuestion item : all) {
            if (WrongQuestion.MASTER_IGNORED.equals(item.getMasterStatus())) {
                ignored++;
                continue;
            }
            if (WrongQuestion.MASTER_MASTERED.equals(item.getMasterStatus())) {
                mastered++;
            } else {
                notMaster++;
            }
            // 新增按「最近一次答错时间」算：今天又错了一次也算今天的
            Date lastWrong = item.getLastWrongTime();
            if (ObjectUtil.isNotNull(lastWrong) && !lastWrong.before(todayStart)) {
                today++;
            }
            if (ObjectUtil.isNotNull(lastWrong) && !lastWrong.before(weekStart)) {
                week++;
            }
        }
        vo.setTotalCount(notMaster + mastered);
        vo.setNotMasterCount(notMaster);
        vo.setMasteredCount(mastered);
        vo.setIgnoredCount(ignored);
        vo.setTodayCount(today);
        vo.setWeekCount(week);

        // 题型 / 难度在题库侧，批量取回后本地分组统计
        List<WrongQuestion> active = all.stream()
            .filter(item -> !WrongQuestion.MASTER_IGNORED.equals(item.getMasterStatus()))
            .toList();
        Map<Long, RemoteQuestionVo> questionMap = selectQuestionMap(active.stream().map(WrongQuestion::getQuestionId).toList());
        Map<String, Long> typeDist = new LinkedHashMap<>();
        Map<String, Long> diffDist = new LinkedHashMap<>();
        for (WrongQuestion item : active) {
            RemoteQuestionVo question = questionMap.get(item.getQuestionId());
            if (ObjectUtil.isNull(question)) {
                continue;
            }
            if (StringUtils.isNotBlank(question.getQuestionType())) {
                typeDist.merge(question.getQuestionType(), 1L, Long::sum);
            }
            if (StringUtils.isNotBlank(question.getDifficulty())) {
                diffDist.merge(question.getDifficulty(), 1L, Long::sum);
            }
        }
        vo.setTypeDistribution(typeDist);
        vo.setDifficultyDistribution(diffDist);
        return vo;
    }

    @Override
    public List<WrongSourceVo> listSources() {
        Long userId = currentUserId();
        List<WrongQuestion> all = selectMyAll(userId).stream()
            .filter(item -> !WrongQuestion.MASTER_IGNORED.equals(item.getMasterStatus()))
            .toList();
        if (CollUtil.isEmpty(all)) {
            return List.of();
        }
        // 按 来源类型 + 来源ID 聚合，同一次查询里复用名称缓存
        Map<String, List<WrongQuestion>> grouped = all.stream()
            .collect(Collectors.groupingBy(item -> sourceKey(item.getSourceType(), item.getSourceId()), LinkedHashMap::new, Collectors.toList()));
        Map<String, String> nameCache = new HashMap<>();
        Map<String, RemoteExamVo> examCache = new HashMap<>();
        List<WrongSourceVo> list = new ArrayList<>(grouped.size());
        for (Map.Entry<String, List<WrongQuestion>> entry : grouped.entrySet()) {
            List<WrongQuestion> items = entry.getValue();
            WrongQuestion first = items.get(0);
            WrongSourceVo vo = new WrongSourceVo();
            vo.setSourceType(first.getSourceType());
            vo.setSourceId(first.getSourceId());
            vo.setExamType(examTypeOf(first.getSourceType(), first.getSourceId(), examCache));
            vo.setSourceName(sourceName(first.getSourceType(), first.getSourceId(), nameCache, examCache));
            vo.setWrongCount((long) items.size());
            vo.setNotMasterCount(items.stream().filter(item -> WrongQuestion.MASTER_NOT.equals(item.getMasterStatus())).count());
            vo.setMasteredCount(items.stream().filter(item -> WrongQuestion.MASTER_MASTERED.equals(item.getMasterStatus())).count());
            vo.setLastWrongTime(items.stream().map(WrongQuestion::getLastWrongTime).filter(ObjectUtil::isNotNull)
                .max(Date::compareTo).orElse(null));
            list.add(vo);
        }
        list.sort((a, b) -> {
            Date at = a.getLastWrongTime();
            Date bt = b.getLastWrongTime();
            if (ObjectUtil.isNull(at) && ObjectUtil.isNull(bt)) {
                return 0;
            }
            if (ObjectUtil.isNull(at)) {
                return 1;
            }
            if (ObjectUtil.isNull(bt)) {
                return -1;
            }
            return bt.compareTo(at);
        });
        return list;
    }

    @Override
    public TableDataInfo<WrongSourceVo> listSourcesPage(WrongSourceBo bo, PageQuery pageQuery) {
        if (ObjectUtil.isNull(bo)) {
            bo = new WrongSourceBo();
        }
        String keyword = StringUtils.isNotBlank(bo.getSourceName()) ? bo.getSourceName().toLowerCase() : null;
        List<WrongSourceVo> all = listSources();
        // examType / 名称关键字都要等聚合完才拿得到，只能在内存里过滤；
        // 来源数天然有限（等于考过的考试数 + 练过的试卷数），内存分页够用且条数准确
        WrongSourceBo finalBo = bo;
        List<WrongSourceVo> filtered = all.stream()
            .filter(item -> StringUtils.isBlank(finalBo.getSourceType()) || finalBo.getSourceType().equals(item.getSourceType()))
            .filter(item -> StringUtils.isBlank(finalBo.getExamType()) || finalBo.getExamType().equals(item.getExamType()))
            .filter(item -> ObjectUtil.isNull(keyword)
                || ObjectUtil.defaultIfNull(item.getSourceName(), "").toLowerCase().contains(keyword))
            .toList();

        // 来源数天然有限，用框架的「假分页」把结果切成一页即可，总条数按过滤后的算
        Page<WrongSourceVo> page = ObjectUtil.isNull(pageQuery) ? new Page<>() : pageQuery.build();
        return TableDataInfo.build(filtered, page);
    }

    @Override
    public WrongQuestionVo queryById(Long id) {
        Long userId = currentUserId();
        WrongQuestion wrong = selectOwn(id, userId);
        List<WrongQuestionVo> voList = toVoList(List.of(wrong));
        if (CollUtil.isEmpty(voList)) {
            throw new ServiceException("错题不存在");
        }
        return voList.get(0);
    }

    @Override
    public List<WrongReviewRecordVo> listReviews(Long id) {
        Long userId = currentUserId();
        WrongQuestion wrong = selectOwn(id, userId);
        RemoteQuestionVo question = selectQuestionMap(List.of(wrong.getQuestionId())).get(wrong.getQuestionId());
        List<WrongReviewRecord> records = wrongReviewRecordMapper.selectList(
            Wrappers.lambdaQuery(WrongReviewRecord.class)
                .eq(WrongReviewRecord::getUserWrongId, wrong.getId())
                .orderByDesc(WrongReviewRecord::getReviewTime));
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }
        return records.stream().map(record -> {
            WrongReviewRecordVo vo = new WrongReviewRecordVo();
            vo.setId(record.getId());
            vo.setUserWrongId(record.getUserWrongId());
            vo.setQuestionId(record.getQuestionId());
            vo.setCorrect(ObjectUtil.equal(1, record.getIsCorrect()));
            vo.setReviewTime(record.getReviewTime());
            vo.setUserAnswerText(ObjectUtil.isNull(question) ? record.getUserAnswer()
                : toAnswerText(question.getQuestionType(), record.getUserAnswer(), false));
            return vo;
        }).toList();
    }

    /* ---------------------------------- 重做 ---------------------------------- */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WrongReviewResultVo review(Long id, WrongReviewBo bo) {
        Long userId = currentUserId();
        WrongQuestion wrong = selectOwn(id, userId);
        RemoteQuestionVo question = selectQuestionMap(List.of(wrong.getQuestionId())).get(wrong.getQuestionId());
        if (ObjectUtil.isNull(question)) {
            throw new ServiceException("该题目已被删除，无法继续重做");
        }
        boolean objective = isObjective(question.getQuestionType());
        boolean right = objective && judge(question, bo.getAnswerContent());
        Date now = new Date();

        // 每重做一次都留痕，回看时能看到自己是怎么一步步做对的
        WrongReviewRecord record = new WrongReviewRecord();
        record.setUserWrongId(wrong.getId());
        record.setUserId(userId);
        record.setQuestionId(wrong.getQuestionId());
        record.setUserAnswer(bo.getAnswerContent());
        record.setIsCorrect(right ? 1 : 0);
        record.setReviewTime(now);
        // 实体不继承 TenantEntity，租户ID在这里显式写入，保证与查询时的租户过滤一致
        record.setTenantId(TenantHelper.getTenantId());
        wrongReviewRecordMapper.insert(record);

        WrongQuestion update = new WrongQuestion();
        update.setId(wrong.getId());
        update.setLastReviewTime(now);
        boolean autoMastered = false;
        String message;
        if (!objective) {
            // 主观题没法自动判分，只记录作答，不参与掌握度判定
            message = "主观题已记录本次作答，请对照参考答案与解析自行检查";
        } else if (right) {
            int rightCount = ObjectUtil.defaultIfNull(wrong.getRightCount(), 0) + 1;
            update.setRightCount(rightCount);
            if (rightCount >= WrongQuestion.AUTO_MASTER_RIGHT_COUNT) {
                if (!WrongQuestion.MASTER_MASTERED.equals(wrong.getMasterStatus())) {
                    update.setMasterStatus(WrongQuestion.MASTER_MASTERED);
                    autoMastered = true;
                }
                message = "答对了！已连续答对 " + rightCount + " 次，这道题可以移出错题本了";
            } else {
                message = "答对了！再答对 1 次就会自动标记为已掌握，也可以现在就移出错题本";
            }
        } else {
            // 又错了：连续答对清零，错误次数累加，已掌握的退回未掌握
            update.setRightCount(0);
            update.setWrongCount(ObjectUtil.defaultIfNull(wrong.getWrongCount(), 0) + 1);
            update.setLastWrongTime(now);
            if (WrongQuestion.MASTER_MASTERED.equals(wrong.getMasterStatus())) {
                update.setMasterStatus(WrongQuestion.MASTER_NOT);
            }
            message = "还是答错了，看看解析再来一次吧";
        }
        baseMapper.updateById(update);

        WrongReviewResultVo vo = new WrongReviewResultVo();
        vo.setWrongId(wrong.getId());
        vo.setQuestionId(wrong.getQuestionId());
        vo.setCorrect(objective ? right : null);
        vo.setMyAnswerText(toAnswerText(question.getQuestionType(), bo.getAnswerContent(), false));
        vo.setStandardAnswerText(toAnswerText(question.getQuestionType(), question.getAnswer(), true));
        vo.setAnalysis(question.getAnalysis());
        vo.setOptions(toOptions(question.getOptions()));
        vo.setRightCount(ObjectUtil.defaultIfNull(update.getRightCount(), wrong.getRightCount()));
        vo.setWrongCount(ObjectUtil.defaultIfNull(update.getWrongCount(), wrong.getWrongCount()));
        vo.setMasterStatus(ObjectUtil.defaultIfNull(update.getMasterStatus(), wrong.getMasterStatus()));
        vo.setAutoMastered(autoMastered);
        vo.setMessage(message);
        log.info("错题重做 wrongId={}, questionId={}, correct={}", wrong.getId(), wrong.getQuestionId(), vo.getCorrect());
        return vo;
    }

    /* ---------------------------------- 维护 ---------------------------------- */

    @Override
    public void updateNote(Long id, WrongNoteBo bo) {
        Long userId = currentUserId();
        selectOwn(id, userId);
        WrongQuestion update = new WrongQuestion();
        update.setId(id);
        update.setUserNote(bo.getUserNote());
        baseMapper.updateById(update);
    }

    @Override
    public void markMastered(Long id) {
        updateMasterStatus(id, WrongQuestion.MASTER_MASTERED);
    }

    @Override
    public void markIgnored(Long id) {
        updateMasterStatus(id, WrongQuestion.MASTER_IGNORED);
    }

    @Override
    public void restore(Long id) {
        updateMasterStatus(id, WrongQuestion.MASTER_NOT);
    }

    @Override
    public void deleteById(Long id) {
        Long userId = currentUserId();
        selectOwn(id, userId);
        baseMapper.deleteById(id);
    }

    /* ------------------------------ 跨服务同步入口 ------------------------------ */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncWrong(List<RemoteWrongQuestionBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return;
        }
        Date now = new Date();
        for (RemoteWrongQuestionBo bo : boList) {
            // 访客没有用户ID，按约定不做错题本
            if (ObjectUtil.isNull(bo.getUserId()) || ObjectUtil.isNull(bo.getQuestionId())) {
                continue;
            }
            // 逐条兜异常：一道题写失败不能把同批其它题一起带崩
            try {
                syncOne(bo, now);
            } catch (Exception e) {
                log.warn("同步单条错题失败 userId={}, questionId={}, {}", bo.getUserId(), bo.getQuestionId(), e.getMessage());
            }
        }
        log.info("错题同步完成 userId={}, 题目数={}", boList.get(0).getUserId(), boList.size());
    }

    /**
     * 同步一道错题：已存在则累加次数，不存在则新增
     */
    private void syncOne(RemoteWrongQuestionBo bo, Date now) {
        // 必须连已删除的一起查：只查未删除会走到新增分支，撞上 uk_user_question 唯一键，
        // 整批同步失败，表现出来就是「考试记录里有错题，错题本里一条没有」
        WrongQuestion exist = baseMapper.selectOneIgnoreDeleted(bo.getUserId(), bo.getQuestionId());
        if (ObjectUtil.isNotNull(exist)) {
            // 之前被彻底删过又错了，先复活，否则 updateById 的 where 带 is_deleted=0，一行也更新不到
            if (ObjectUtil.equal(1, exist.getIsDeleted())) {
                baseMapper.restore(exist.getId());
            }
            WrongQuestion update = new WrongQuestion();
            update.setId(exist.getId());
            update.setWrongCount(ObjectUtil.defaultIfNull(exist.getWrongCount(), 0) + 1);
            // 又错了一次，连续答对记录清零
            update.setRightCount(0);
            update.setLastWrongTime(now);
            // 来源跟随最近一次答错，保证「这场考试错了这道题」能在该场考试的明细里看到
            update.setSourceType(bo.getSourceType());
            update.setSourceId(bo.getSourceId());
            update.setAnswerItemId(bo.getAnswerItemId());
            if (ObjectUtil.isNull(exist.getMasterStatus()) || WrongQuestion.MASTER_MASTERED.equals(exist.getMasterStatus())) {
                // 已掌握的题再错要退回未掌握；状态为空的历史数据一并兜底
                update.setMasterStatus(WrongQuestion.MASTER_NOT);
            }
            baseMapper.updateById(update);
            return;
        }
        WrongQuestion add = new WrongQuestion();
        add.setUserId(bo.getUserId());
        add.setQuestionId(bo.getQuestionId());
        add.setSourceType(bo.getSourceType());
        add.setSourceId(bo.getSourceId());
        add.setAnswerItemId(bo.getAnswerItemId());
        add.setWrongCount(1);
        add.setRightCount(0);
        add.setMasterStatus(WrongQuestion.MASTER_NOT);
        add.setLastWrongTime(now);
        add.setCreateTime(now);
        add.setUpdateTime(now);
        add.setIsDeleted(0);
        // 实体不继承 TenantEntity，租户ID在这里显式写入，保证与查询时的租户过滤一致
        add.setTenantId(TenantHelper.getTenantId());
        baseMapper.insert(add);
    }

    /* ---------------------------------- 私有方法 ---------------------------------- */

    /**
     * 当前登录用户ID
     */
    private Long currentUserId() {
        Long userId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(userId)) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        return userId;
    }

    /**
     * 取自己的错题，顺便挡掉越权访问
     */
    private WrongQuestion selectOwn(Long id, Long userId) {
        WrongQuestion wrong = baseMapper.selectById(id);
        if (ObjectUtil.isNull(wrong)) {
            throw new ServiceException("错题不存在或已删除");
        }
        if (!ObjectUtil.equal(userId, wrong.getUserId())) {
            throw new ServiceException("无权操作他人的错题");
        }
        return wrong;
    }

    /**
     * 我的全部错题（含已忽略，不含软删除）
     */
    private List<WrongQuestion> selectMyAll(Long userId) {
        return baseMapper.selectList(
            Wrappers.lambdaQuery(WrongQuestion.class).eq(WrongQuestion::getUserId, userId));
    }

    /**
     * 只改掌握状态，统一走这里保证归属校验一致
     */
    private void updateMasterStatus(Long id, String masterStatus) {
        Long userId = currentUserId();
        selectOwn(id, userId);
        WrongQuestion update = new WrongQuestion();
        update.setId(id);
        update.setMasterStatus(masterStatus);
        baseMapper.updateById(update);
    }

    /**
     * 批量取题目，按题目ID建索引
     */
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

    /**
     * 错题行 → VO，题目信息由题库服务补齐
     */
    private List<WrongQuestionVo> toVoList(List<WrongQuestion> list) {
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        Map<Long, RemoteQuestionVo> questionMap = selectQuestionMap(list.stream().map(WrongQuestion::getQuestionId).toList());
        Map<String, String> nameCache = new HashMap<>();
        Map<String, RemoteExamVo> examCache = new HashMap<>();
        List<WrongQuestionVo> voList = new ArrayList<>(list.size());
        for (WrongQuestion wrong : list) {
            WrongQuestionVo vo = new WrongQuestionVo();
            vo.setId(wrong.getId());
            vo.setQuestionId(wrong.getQuestionId());
            vo.setSourceType(wrong.getSourceType());
            vo.setSourceId(wrong.getSourceId());
            vo.setSourceName(sourceName(wrong.getSourceType(), wrong.getSourceId(), nameCache, examCache));
            vo.setExamType(examTypeOf(wrong.getSourceType(), wrong.getSourceId(), examCache));
            vo.setWrongCount(wrong.getWrongCount());
            vo.setRightCount(wrong.getRightCount());
            vo.setMasterStatus(wrong.getMasterStatus());
            vo.setUserNote(wrong.getUserNote());
            vo.setLastWrongTime(wrong.getLastWrongTime());
            vo.setLastReviewTime(wrong.getLastReviewTime());
            vo.setCreateTime(wrong.getCreateTime());

            RemoteQuestionVo question = questionMap.get(wrong.getQuestionId());
            if (ObjectUtil.isNotNull(question)) {
                vo.setQuestionType(question.getQuestionType());
                vo.setDifficulty(question.getDifficulty());
                vo.setScore(question.getScore());
                vo.setTitle(question.getTitle());
                vo.setOptions(toOptions(question.getOptions()));
                vo.setAnalysis(question.getAnalysis());
                vo.setStandardAnswer(question.getAnswer());
                vo.setStandardAnswerText(toAnswerText(question.getQuestionType(), question.getAnswer(), true));
            }
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 选项转换：题库侧的选项对象换成前端要的精简结构
     */
    private List<WrongOptionVo> toOptions(List<RemoteQuestionOptionVo> options) {
        if (CollUtil.isEmpty(options)) {
            return null;
        }
        return options.stream().map(option -> {
            WrongOptionVo vo = new WrongOptionVo();
            vo.setOptionKey(option.getOptionKey());
            vo.setOptionContent(option.getOptionContent());
            return vo;
        }).toList();
    }

    /**
     * 来源名称：考试名或试卷名，查不到返回 null 由前端兜底
     */
    private String sourceName(String sourceType, Long sourceId, Map<String, String> cache, Map<String, RemoteExamVo> examCache) {
        if (StringUtils.isBlank(sourceType) || ObjectUtil.isNull(sourceId)) {
            return null;
        }
        String key = sourceKey(sourceType, sourceId);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        String name = null;
        if (WrongQuestion.SOURCE_EXAM.equals(sourceType)) {
            RemoteExamVo exam = cachedExam(sourceType, sourceId, examCache);
            name = ObjectUtil.isNull(exam) ? null : exam.getExamName();
        } else if (WrongQuestion.SOURCE_PAPER_PRACTICE.equals(sourceType)) {
            RemotePaperVo paper = remotePaperService.queryPaper(sourceId);
            name = ObjectUtil.isNull(paper) ? null : paper.getPaperName();
        }
        cache.put(key, name);
        return name;
    }

    /**
     * 取来源对应的考试，同一批数据里复用结果，避免一行一次远程调用
     */
    private RemoteExamVo cachedExam(String sourceType, Long sourceId, Map<String, RemoteExamVo> cache) {
        String key = sourceKey(sourceType, sourceId);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        RemoteExamVo exam = remoteExamService.queryExam(sourceId);
        cache.put(key, exam);
        return exam;
    }

    /**
     * 来源对应的任务类型：1正式考试 / 2练习考试
     *
     * <p>类型就长在考试上，考试名也是同一次调用带回来的，所以这里不会多查一次。
     */
    private String examTypeOf(String sourceType, Long sourceId, Map<String, RemoteExamVo> examCache) {
        if (!WrongQuestion.SOURCE_EXAM.equals(sourceType) || ObjectUtil.isNull(sourceId)) {
            return null;
        }
        RemoteExamVo exam = cachedExam(sourceType, sourceId, examCache);
        return ObjectUtil.isNull(exam) ? null : exam.getExamType();
    }

    private String sourceKey(String sourceType, Long sourceId) {
        return sourceType + ":" + sourceId;
    }

    private boolean isObjective(String questionType) {
        return TYPE_SINGLE.equals(questionType) || TYPE_MULTIPLE.equals(questionType)
            || TYPE_JUDGE.equals(questionType) || TYPE_BLANK.equals(questionType);
    }

    /**
     * 客观题判分
     *
     * <p>与答题服务的判分口径保持一致：填空逐空比对，选项题按集合全等比对。
     */
    private boolean judge(RemoteQuestionVo question, String answerContent) {
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
                if (!accepted.contains(normalize(mine.getBlanks().get(i).getText()))) {
                    return false;
                }
            }
            return true;
        }
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
