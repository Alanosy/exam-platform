package org.dromara.exam.proctor.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.exam.manage.api.RemoteExamService;
import org.dromara.exam.manage.api.domain.RemoteExamVo;
import org.dromara.exam.proctor.domain.ProctorEvent;
import org.dromara.exam.proctor.domain.ProctorRule;
import org.dromara.exam.proctor.domain.ProctorSession;
import org.dromara.exam.proctor.domain.ProctorSnapshot;
import org.dromara.exam.proctor.domain.bo.ProctorEventBo;
import org.dromara.exam.proctor.domain.bo.ProctorEventQueryBo;
import org.dromara.exam.proctor.domain.bo.ProctorExamGroupBo;
import org.dromara.exam.proctor.domain.bo.ProctorSessionBo;
import org.dromara.exam.proctor.domain.bo.ProctorStartBo;
import org.dromara.exam.proctor.domain.enums.ProctorEventType;
import org.dromara.exam.proctor.domain.vo.ProctorEventVo;
import org.dromara.exam.proctor.domain.vo.ProctorExamGroupVo;
import org.dromara.exam.proctor.domain.vo.ProctorOverviewVo;
import org.dromara.exam.proctor.domain.vo.ProctorReportVo;
import org.dromara.exam.proctor.domain.vo.ProctorSessionVo;
import org.dromara.exam.proctor.domain.vo.ProctorSnapshotVo;
import org.dromara.exam.proctor.mapper.ProctorEventMapper;
import org.dromara.exam.proctor.mapper.ProctorSessionMapper;
import org.dromara.exam.proctor.mapper.ProctorSnapshotMapper;
import org.dromara.exam.proctor.service.IProctorService;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.dromara.system.api.RemoteUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 防作弊服务实现
 *
 * <p>考生端只做「采集 + 上报」，所有计数、风险判定、是否强制交卷都由这里算，
 * 前端改不改都影响不了结果——这也是把防作弊单独拆一个微服务的原因。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProctorServiceImpl implements IProctorService {

    /** 单次上报最多处理多少条事件，防前端失控狂刷 */
    private static final int MAX_BATCH = 50;

    /** 心跳超过这个秒数没动静就算掉线 */
    private static final long OFFLINE_SECONDS = 120;

    private final ProctorSessionMapper sessionMapper;
    private final ProctorEventMapper eventMapper;
    private final ProctorSnapshotMapper snapshotMapper;

    @DubboReference
    private RemoteExamService remoteExamService;

    @DubboReference
    private RemoteUserService remoteUserService;

    @DubboReference
    private RemoteFileService remoteFileService;

    /* --------------------------------- 考生端 --------------------------------- */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProctorSessionVo startSession(ProctorStartBo bo) {
        Long userId = LoginHelper.getUserId();
        String account = LoginHelper.getUsername();
        RemoteExamVo exam = remoteExamService.queryExam(bo.getExamId());
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("考试不存在或已删除");
        }
        ProctorRule rule = ProctorRule.parse(exam.getAntiCheatConfig());
        Date now = new Date();

        ProctorSession session = selectSession(bo.getExamId(), bo.getRecordId());
        boolean resumed = ObjectUtil.isNotNull(session);
        if (!resumed) {
            session = new ProctorSession();
            session.setExamId(bo.getExamId());
            session.setRecordId(bo.getRecordId());
            session.setUserId(userId);
            session.setAccount(account);
            session.setNickName(nickNameOf(userId));
            session.setAttemptNo(ObjectUtil.defaultIfNull(bo.getAttemptNo(), 1));
            session.setSwitchCount(0);
            session.setBlurCount(0);
            session.setCopyCount(0);
            session.setPasteCount(0);
            session.setCutCount(0);
            session.setContextmenuCount(0);
            session.setExitFullscreenCount(0);
            session.setCameraCount(0);
            session.setDevtoolCount(0);
            session.setMultitabCount(0);
            session.setForceSubmit(0L);
            session.setRiskScore(0);
            session.setRiskLevel(ProctorSession.RISK_NORMAL);
            session.setStartTime(now);
            session.setDurationSeconds(0);
            session.setIp(clientIp());
            session.setUserAgent(userAgent());
            session.setDevice(StringUtils.defaultIfBlank(bo.getDevice(), ""));
            session.setStatus(ProctorSession.STATUS_ONLINE);
            session.setLastActiveTime(now);
            session.setDelFlag(0L);
        } else {
            // 续答：交完卷就不该再「续」回来，其余情况把状态拉回在线
            if (!ProctorSession.STATUS_SUBMITTED.equals(session.getStatus())
                && !ProctorSession.STATUS_FORCE_SUBMIT.equals(session.getStatus())) {
                session.setStatus(ProctorSession.STATUS_ONLINE);
            }
            session.setLastActiveTime(now);
            if (ObjectUtil.isNotNull(bo.getAttemptNo())) {
                session.setAttemptNo(bo.getAttemptNo());
            }
        }
        // 规则每次开考都按考试当前配置刷新一次：老师在考试中途改了设置也能生效
        session.setExamName(exam.getExamName());
        session.setMaxSwitch(rule.getSwitchScreen());
        session.setMaxExitFullscreen(rule.getMaxExitFullscreen());
        session.setMaxPaste(rule.getMaxPaste());
        session.setCameraInterval(rule.getCameraInterval());

        if (resumed) {
            sessionMapper.updateById(session);
            saveEvent(session, ProctorEventType.RESUME, "中途退出后重新进入答题", null, now);
        } else {
            sessionMapper.insert(session);
            saveEvent(session, ProctorEventType.ENTER, "进入答题页", null, now);
        }

        ProctorSessionVo vo = sessionMapper.selectVoById(session.getId());
        vo.setRule(rule);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProctorReportVo report(Long sessionId, List<ProctorEventBo> events) {
        ProctorSession session = requireOwnSession(sessionId);
        Date now = new Date();
        int saved = 0;

        if (ObjectUtil.isNotNull(events) && !events.isEmpty()) {
            // 同一秒内同一类型只记一次：切屏会同时触发 visibilitychange 与 blur，别算成两次
            Set<String> dedup = new HashSet<>();
            for (ProctorEventBo bo : events.subList(0, Math.min(events.size(), MAX_BATCH))) {
                ProctorEventType type = ProctorEventType.of(bo.getEventType());
                if (ObjectUtil.isNull(type) || ProctorEventType.HEARTBEAT == type) {
                    continue;
                }
                Date eventTime = parseTime(bo.getEventTime(), now);
                String key = type.getCode() + "_" + eventTime.getTime() / 1000;
                if (!dedup.add(key)) {
                    continue;
                }
                saveEvent(session, type, bo.getContent(), bo.getExtra(), eventTime);
                if (StringUtils.isNotBlank(type.getColumn())) {
                    incrCounter(sessionId, type.getColumn());
                }
                saved++;
            }
        }

        sessionMapper.update(
            null,
            Wrappers.lambdaUpdate(ProctorSession.class)
                .set(ProctorSession::getLastActiveTime, now)
                .eq(ProctorSession::getId, sessionId)
        );

        ProctorSession fresh = sessionMapper.selectById(sessionId);
        return buildReport(fresh, saved);
    }

    @Override
    public ProctorReportVo heartbeat(Long sessionId) {
        ProctorSession session = sessionMapper.selectById(sessionId);
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        LambdaUpdateWrapper<ProctorSession> uw = Wrappers.lambdaUpdate(ProctorSession.class)
            .set(ProctorSession::getLastActiveTime, new Date())
            .eq(ProctorSession::getId, sessionId);
        // 交卷后的会话不再拉回在线，否则监考端会看到「已交卷」的人又活了
        if (!ProctorSession.STATUS_SUBMITTED.equals(session.getStatus())
            && !ProctorSession.STATUS_FORCE_SUBMIT.equals(session.getStatus())) {
            uw.set(ProctorSession::getStatus, ProctorSession.STATUS_ONLINE);
        }
        sessionMapper.update(null, uw);
        // 把服务端真实计数带回去，前端刷新页面后靠它把本地计数拉回来
        return buildReport(sessionMapper.selectById(sessionId), 0);
    }

    @Override
    public void finishSession(Long recordId, String status) {
        if (ObjectUtil.isNull(recordId)) {
            return;
        }
        ProctorSession session = sessionMapper.selectOne(
            Wrappers.lambdaQuery(ProctorSession.class).eq(ProctorSession::getRecordId, recordId)
        );
        if (ObjectUtil.isNull(session)) {
            return;
        }
        Date now = new Date();
        session.setStatus(StringUtils.defaultIfBlank(status, ProctorSession.STATUS_SUBMITTED));
        session.setEndTime(now);
        session.setDurationSeconds(secondsBetween(session.getStartTime(), now));
        sessionMapper.updateById(session);
        if (ProctorSession.STATUS_FORCE_SUBMIT.equals(status)) {
            saveEvent(session, ProctorEventType.FORCE_SUBMIT, "违规达到上限，系统强制交卷", null, now);
        }
    }

    /* --------------------------------- 监考端 --------------------------------- */

    @Override
    public TableDataInfo<ProctorSessionVo> listSessionPage(ProctorSessionBo bo, PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        List<Long> ownExamIds = remoteExamService.listExamIdsByCreator(userId);
        List<Long> examIds = new ArrayList<>(ObjectUtil.isNull(ownExamIds) ? List.of() : ownExamIds);
        // 只能看自己发布的考试：指定了 examId 就校验归属，没指定就收敛到自己名下所有考试
        if (ObjectUtil.isNotNull(bo.getExamId())) {
            if (!examIds.contains(bo.getExamId())) {
                throw new ServiceException("只能查看自己创建的考试的监考记录");
            }
            examIds = List.of(bo.getExamId());
        }

        LambdaQueryWrapper<ProctorSession> lqw = Wrappers.lambdaQuery(ProctorSession.class);
        lqw.in(ObjectUtil.isNotEmpty(examIds), ProctorSession::getExamId, examIds);
        lqw.eq(ObjectUtil.isNotNull(bo.getRecordId()), ProctorSession::getRecordId, bo.getRecordId());
        lqw.and(StringUtils.isNotBlank(bo.getKeyword()), w -> w
            .like(ProctorSession::getAccount, bo.getKeyword())
            .or()
            .like(ProctorSession::getNickName, bo.getKeyword()));
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ProctorSession::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getRiskLevel()), ProctorSession::getRiskLevel, bo.getRiskLevel());
        lqw.and(Boolean.TRUE.equals(bo.getOnlyRisk()), w -> w
            .gt(ProctorSession::getSwitchCount, 0)
            .or().gt(ProctorSession::getPasteCount, 0)
            .or().gt(ProctorSession::getDevtoolCount, 0)
            .or().gt(ProctorSession::getMultitabCount, 0)
            .or().gt(ProctorSession::getExitFullscreenCount, 0));
        lqw.orderByDesc(ProctorSession::getRiskScore, ProctorSession::getLastActiveTime);

        Page<ProctorSessionVo> page = sessionMapper.selectVoPage(pageQuery.build(), lqw);
        // 掉线是「算」出来的，不落库：考生关了浏览器不会有最后一次心跳
        Date deadline = DateUtil.offsetSecond(new Date(), (int) -OFFLINE_SECONDS);
        for (ProctorSessionVo vo : page.getRecords()) {
            if (StringUtils.isBlank(vo.getExamName())) {
                vo.setExamName(examNameOf(vo.getExamId()));
            }
            if (ProctorSession.STATUS_ONLINE.equals(vo.getStatus())
                && ObjectUtil.isNotNull(vo.getLastActiveTime())
                && vo.getLastActiveTime().before(deadline)) {
                vo.setStatus(ProctorSession.STATUS_OFFLINE);
            }
        }
        return TableDataInfo.build(page);
    }

    /**
     * 按考试分组汇总
     *
     * <p>在 Java 里分组而不是写聚合 SQL：在线/掉线是「算」出来的（心跳超时），
     * 两套判定逻辑写在两个地方迟早会不一致，这里直接复用 {@link #overview(Long)} 的口径。
     * 一个人创建的考试场次有限，会话量撑得起全量捞一次。
     */
    @Override
    public List<ProctorExamGroupVo> listExamGroups(ProctorExamGroupBo bo) {
        Long userId = LoginHelper.getUserId();
        List<Long> examIds = remoteExamService.listExamIdsByCreator(userId);
        if (ObjectUtil.isNull(examIds) || examIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ProctorSession> lqw = Wrappers.lambdaQuery(ProctorSession.class);
        // 分组只需要这几个字段，没必要把 user_agent 之类的大字段也捞出来
        lqw.select(ProctorSession::getExamId, ProctorSession::getExamName, ProctorSession::getStatus,
            ProctorSession::getRiskLevel, ProctorSession::getForceSubmit, ProctorSession::getRiskScore,
            ProctorSession::getSwitchCount, ProctorSession::getPasteCount, ProctorSession::getCameraCount,
            ProctorSession::getDevtoolCount, ProctorSession::getMultitabCount, ProctorSession::getExitFullscreenCount,
            ProctorSession::getLastActiveTime);
        lqw.in(ProctorSession::getExamId, examIds);
        lqw.like(StringUtils.isNotBlank(bo.getKeyword()), ProctorSession::getExamName, bo.getKeyword());
        List<ProctorSession> sessions = sessionMapper.selectList(lqw);
        if (sessions.isEmpty()) {
            return List.of();
        }

        Date deadline = DateUtil.offsetSecond(new Date(), (int) -OFFLINE_SECONDS);
        // LinkedHashMap 保序：按考试ID第一次出现的顺序，最后再统一排序
        Map<Long, ProctorExamGroupVo> groups = new LinkedHashMap<>();
        for (ProctorSession s : sessions) {
            ProctorExamGroupVo vo = groups.computeIfAbsent(s.getExamId(), id -> {
                ProctorExamGroupVo item = new ProctorExamGroupVo();
                item.setExamId(id);
                item.setExamName(StringUtils.defaultIfBlank(s.getExamName(), examNameOf(id)));
                item.setTotalCount(0L);
                item.setOnlineCount(0L);
                item.setOfflineCount(0L);
                item.setSubmittedCount(0L);
                item.setSuspectCount(0L);
                item.setSeriousCount(0L);
                item.setForceSubmitCount(0L);
                item.setSwitchTotal(0L);
                item.setPasteTotal(0L);
                item.setCameraTotal(0L);
                item.setMaxRiskScore(0);
                item.setRiskLevel(ProctorSession.RISK_NORMAL);
                return item;
            });
            boolean onlineNow = ProctorSession.STATUS_ONLINE.equals(s.getStatus())
                && ObjectUtil.isNotNull(s.getLastActiveTime())
                && s.getLastActiveTime().after(deadline);
            if (onlineNow) {
                vo.setOnlineCount(vo.getOnlineCount() + 1);
            } else if (ProctorSession.STATUS_SUBMITTED.equals(s.getStatus())
                || ProctorSession.STATUS_FORCE_SUBMIT.equals(s.getStatus())) {
                vo.setSubmittedCount(vo.getSubmittedCount() + 1);
            } else {
                vo.setOfflineCount(vo.getOfflineCount() + 1);
            }
            if (ProctorSession.RISK_SUSPECT.equals(s.getRiskLevel())) {
                vo.setSuspectCount(vo.getSuspectCount() + 1);
            } else if (ProctorSession.RISK_SERIOUS.equals(s.getRiskLevel())) {
                vo.setSeriousCount(vo.getSeriousCount() + 1);
            }
            if (ObjectUtil.equal(1L, s.getForceSubmit())) {
                vo.setForceSubmitCount(vo.getForceSubmitCount() + 1);
            }
            vo.setTotalCount(vo.getTotalCount() + 1);
            vo.setSwitchTotal(vo.getSwitchTotal() + nz(s.getSwitchCount()));
            vo.setPasteTotal(vo.getPasteTotal() + nz(s.getPasteCount()));
            vo.setCameraTotal(vo.getCameraTotal() + nz(s.getCameraCount()));
            int score = ObjectUtil.isNull(s.getRiskScore()) ? 0 : s.getRiskScore();
            if (score > vo.getMaxRiskScore()) {
                vo.setMaxRiskScore(score);
                vo.setRiskLevel(StringUtils.defaultIfBlank(s.getRiskLevel(), ProctorSession.RISK_NORMAL));
            }
            if (ObjectUtil.isNotNull(s.getLastActiveTime())
                && (ObjectUtil.isNull(vo.getLastActiveTime()) || s.getLastActiveTime().after(vo.getLastActiveTime()))) {
                vo.setLastActiveTime(s.getLastActiveTime());
            }
        }

        List<ProctorExamGroupVo> list = new ArrayList<>(groups.values());
        // 「只看有异常」：风险分涵盖了切屏 / 粘贴 / 开发者工具 / 多开 / 退出全屏，
        // 再加一道切屏粘贴总数兜底，防止某场考试的 risk_score 还是旧值被漏掉
        if (Boolean.TRUE.equals(bo.getOnlyRisk())) {
            list.removeIf(vo -> vo.getMaxRiskScore() <= 0 && vo.getSwitchTotal() + vo.getPasteTotal() == 0);
        }
        list.sort(Comparator.comparingInt(ProctorExamGroupVo::getMaxRiskScore).reversed()
            .thenComparing(ProctorExamGroupVo::getLastActiveTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    @Override
    public ProctorSessionVo getSession(Long sessionId) {
        ProctorSessionVo vo = sessionMapper.selectVoById(sessionId);
        if (ObjectUtil.isNull(vo)) {
            throw new ServiceException("监考记录不存在");
        }
        checkExamOwner(vo.getExamId());
        return vo;
    }

    @Override
    public TableDataInfo<ProctorEventVo> listEventPage(ProctorEventQueryBo bo, PageQuery pageQuery) {
        Long sessionId = bo.getSessionId();
        if (ObjectUtil.isNull(sessionId) && ObjectUtil.isNotNull(bo.getExamId())) {
            checkExamOwner(bo.getExamId());
        } else if (ObjectUtil.isNotNull(sessionId)) {
            ProctorSession session = sessionMapper.selectById(sessionId);
            if (ObjectUtil.isNull(session)) {
                throw new ServiceException("监考记录不存在");
            }
            checkExamOwner(session.getExamId());
        } else {
            throw new ServiceException("请指定会话或考试");
        }

        LambdaQueryWrapper<ProctorEvent> lqw = Wrappers.lambdaQuery(ProctorEvent.class);
        lqw.eq(ObjectUtil.isNotNull(sessionId), ProctorEvent::getSessionId, sessionId);
        lqw.eq(ObjectUtil.isNotNull(bo.getExamId()), ProctorEvent::getExamId, bo.getExamId());
        lqw.eq(StringUtils.isNotBlank(bo.getEventType()), ProctorEvent::getEventType, bo.getEventType());
        lqw.eq(StringUtils.isNotBlank(bo.getLevel()), ProctorEvent::getLevel, bo.getLevel());
        lqw.and(Boolean.TRUE.equals(bo.getOnlyWarn()), w -> w
            .eq(ProctorEvent::getLevel, "warn")
            .or().eq(ProctorEvent::getLevel, "danger"));
        lqw.orderByDesc(ProctorEvent::getEventTime, ProctorEvent::getId);

        Page<ProctorEventVo> page = eventMapper.selectVoPage(pageQuery.build(), lqw);
        String account = null;
        String nickName = null;
        if (ObjectUtil.isNotNull(sessionId)) {
            ProctorSession session = sessionMapper.selectById(sessionId);
            if (ObjectUtil.isNotNull(session)) {
                account = session.getAccount();
                nickName = session.getNickName();
            }
        }
        for (ProctorEventVo vo : page.getRecords()) {
            vo.setAccount(account);
            vo.setNickName(nickName);
        }
        return TableDataInfo.build(page);
    }

    @Override
    public List<ProctorSnapshotVo> listSnapshots(Long sessionId) {
        if (ObjectUtil.isNull(sessionId)) {
            return List.of();
        }
        ProctorSession session = sessionMapper.selectById(sessionId);
        if (ObjectUtil.isNull(session)) {
            return List.of();
        }
        checkExamOwner(session.getExamId());
        List<ProctorSnapshotVo> list = snapshotMapper.selectVoList(
            Wrappers.lambdaQuery(ProctorSnapshot.class)
                .eq(ProctorSnapshot::getSessionId, sessionId)
                .orderByDesc(ProctorSnapshot::getCaptureTime)
                .last("limit 200")
        );
        for (ProctorSnapshotVo vo : list) {
            vo.setAccount(session.getAccount());
            vo.setNickName(session.getNickName());
        }
        return list;
    }

    @Override
    public ProctorSnapshotVo saveSnapshot(Long sessionId, String eventType, MultipartFile file) {
        if (ObjectUtil.isNull(file) || file.isEmpty()) {
            throw new ServiceException("抓拍内容为空");
        }
        ProctorSession session = requireOwnSession(sessionId);
        RemoteFile remoteFile;
        try {
            remoteFile = remoteFileService.upload(
                "proctor_" + sessionId + "_" + System.currentTimeMillis(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
            );
        } catch (IOException e) {
            throw new ServiceException("抓拍上传失败");
        }
        if (ObjectUtil.isNull(remoteFile) || StringUtils.isBlank(remoteFile.getUrl())) {
            throw new ServiceException("抓拍上传失败");
        }

        ProctorSnapshot snapshot = new ProctorSnapshot();
        snapshot.setSessionId(sessionId);
        snapshot.setExamId(session.getExamId());
        snapshot.setRecordId(session.getRecordId());
        snapshot.setUserId(session.getUserId());
        snapshot.setOssId(String.valueOf(remoteFile.getOssId()));
        snapshot.setUrl(remoteFile.getUrl());
        snapshot.setEventType(StringUtils.defaultIfBlank(eventType, "periodic"));
        snapshot.setCaptureTime(new Date());
        snapshot.setDelFlag(0L);
        snapshotMapper.insert(snapshot);

        incrCounter(sessionId, ProctorEventType.CAMERA.getColumn());
        saveEvent(session, ProctorEventType.CAMERA, "摄像头抓拍", remoteFile.getUrl(), new Date());

        ProctorSnapshotVo vo = snapshotMapper.selectVoById(snapshot.getId());
        vo.setAccount(session.getAccount());
        vo.setNickName(session.getNickName());
        return vo;
    }

    @Override
    public ProctorOverviewVo overview(Long examId) {
        if (ObjectUtil.isNull(examId)) {
            throw new ServiceException("请指定考试");
        }
        checkExamOwner(examId);
        List<ProctorSession> sessions = sessionMapper.selectList(
            Wrappers.lambdaQuery(ProctorSession.class).eq(ProctorSession::getExamId, examId)
        );
        Date deadline = DateUtil.offsetSecond(new Date(), (int) -OFFLINE_SECONDS);

        ProctorOverviewVo vo = new ProctorOverviewVo();
        vo.setExamId(examId);
        vo.setExamName(examNameOf(examId));
        vo.setTotalCount((long) sessions.size());
        long online = 0;
        long offline = 0;
        long submitted = 0;
        long suspect = 0;
        long serious = 0;
        long force = 0;
        long switchTotal = 0;
        long pasteTotal = 0;
        long cameraTotal = 0;
        for (ProctorSession s : sessions) {
            boolean onlineNow = ProctorSession.STATUS_ONLINE.equals(s.getStatus())
                && ObjectUtil.isNotNull(s.getLastActiveTime())
                && s.getLastActiveTime().after(deadline);
            if (onlineNow) {
                online++;
            } else if (ProctorSession.STATUS_SUBMITTED.equals(s.getStatus())
                || ProctorSession.STATUS_FORCE_SUBMIT.equals(s.getStatus())) {
                submitted++;
            } else {
                offline++;
            }
            if (ProctorSession.RISK_SUSPECT.equals(s.getRiskLevel())) {
                suspect++;
            } else if (ProctorSession.RISK_SERIOUS.equals(s.getRiskLevel())) {
                serious++;
            }
            if (ObjectUtil.equal(1L, s.getForceSubmit())) {
                force++;
            }
            switchTotal += nz(s.getSwitchCount());
            pasteTotal += nz(s.getPasteCount());
            cameraTotal += nz(s.getCameraCount());
        }
        vo.setOnlineCount(online);
        vo.setOfflineCount(offline);
        vo.setSubmittedCount(submitted);
        vo.setSuspectCount(suspect);
        vo.setSeriousCount(serious);
        vo.setForceSubmitCount(force);
        vo.setSwitchTotal(switchTotal);
        vo.setPasteTotal(pasteTotal);
        vo.setCameraTotal(cameraTotal);
        return vo;
    }

    /* --------------------------------- 内部方法 --------------------------------- */

    /**
     * 组装上报结果：顺带算风险等级与是否达到强制交卷条件
     *
     * <p>强制交卷只在「考试明确配了上限」时触发（0 = 不限制），
     * 没配的考试即便记了一堆流水也不会把人踢出去。
     */
    private ProctorReportVo buildReport(ProctorSession session, int saved) {
        ProctorReportVo vo = new ProctorReportVo();
        vo.setSessionId(session.getId());
        vo.setSwitchCount(session.getSwitchCount());
        vo.setPasteCount(session.getPasteCount());
        vo.setExitFullscreenCount(session.getExitFullscreenCount());
        vo.setCameraCount(session.getCameraCount());
        vo.setMaxSwitch(session.getMaxSwitch());
        vo.setMaxPaste(session.getMaxPaste());
        vo.setMaxExitFullscreen(session.getMaxExitFullscreen());
        vo.setSavedCount(saved);

        boolean exceed = false;
        String reason = null;
        if (gtLimit(session.getMaxSwitch(), session.getSwitchCount())) {
            exceed = true;
            reason = "切屏次数超过限制（最多 " + session.getMaxSwitch() + " 次），系统已自动交卷";
        } else if (gtLimit(session.getMaxPaste(), session.getPasteCount())) {
            exceed = true;
            reason = "粘贴次数超过限制（最多 " + session.getMaxPaste() + " 次），系统已自动交卷";
        } else if (gtLimit(session.getMaxExitFullscreen(), session.getExitFullscreenCount())) {
            exceed = true;
            reason = "退出全屏次数超过限制（最多 " + session.getMaxExitFullscreen() + " 次），系统已自动交卷";
        }

        int score = riskScore(session);
        String level = exceed || score >= 60 ? ProctorSession.RISK_SERIOUS : score >= 20 ? ProctorSession.RISK_SUSPECT : ProctorSession.RISK_NORMAL;
        session.setRiskScore(score);
        session.setRiskLevel(level);
        vo.setRiskLevel(level);
        vo.setExceed(exceed);
        vo.setExceedReason(reason);

        if (exceed && !ObjectUtil.equal(1L, session.getForceSubmit())) {
            session.setForceSubmit(1L);
            session.setStatus(ProctorSession.STATUS_FORCE_SUBMIT);
            saveEvent(session, ProctorEventType.FORCE_SUBMIT, reason, null, new Date());
        }
        sessionMapper.updateById(session);
        return vo;
    }

    /** 阈值 > 0 才算限制：0 表示不限制 */
    private boolean gtLimit(Integer limit, Integer count) {
        return ObjectUtil.isNotNull(limit) && limit > 0 && nz(count) > limit;
    }

    private int riskScore(ProctorSession s) {
        int score = 0;
        score += nz(s.getSwitchCount()) * 5;
        score += nz(s.getPasteCount()) * 3;
        score += nz(s.getCopyCount());
        score += nz(s.getCutCount()) * 2;
        score += nz(s.getExitFullscreenCount()) * 5;
        score += nz(s.getDevtoolCount()) * 20;
        score += nz(s.getMultitabCount()) * 15;
        return score;
    }

    private int nz(Integer value) {
        return ObjectUtil.isNull(value) ? 0 : value;
    }

    /** 计数自增：直接走 SQL 累加，避免读改写丢更新 */
    private void incrCounter(Long sessionId, String column) {
        LambdaUpdateWrapper<ProctorSession> uw = Wrappers.lambdaUpdate(ProctorSession.class)
            .setSql(column + " = " + column + " + 1")
            .eq(ProctorSession::getId, sessionId);
        sessionMapper.update(null, uw);
    }

    private void saveEvent(ProctorSession session, ProctorEventType type, String content, String extra, Date eventTime) {
        ProctorEvent event = new ProctorEvent();
        event.setSessionId(session.getId());
        event.setExamId(session.getExamId());
        event.setRecordId(session.getRecordId());
        event.setUserId(session.getUserId());
        event.setEventType(type.getCode());
        event.setEventName(type.getName());
        event.setLevel(type.getLevel());
        event.setContent(content);
        event.setExtra(extra);
        event.setEventTime(eventTime);
        event.setDelFlag(0L);
        eventMapper.insert(event);
    }

    private ProctorSession selectSession(Long examId, Long recordId) {
        return sessionMapper.selectOne(
            Wrappers.lambdaQuery(ProctorSession.class)
                .eq(ProctorSession::getExamId, examId)
                .eq(ProctorSession::getRecordId, recordId)
        );
    }

    /** 取会话并校验是当前考生本人的，避免改个 sessionId 就给别人加切屏 */
    private ProctorSession requireOwnSession(Long sessionId) {
        ProctorSession session = sessionMapper.selectById(sessionId);
        if (ObjectUtil.isNull(session)) {
            throw new ServiceException("监考会话不存在，请重新进入答题页");
        }
        if (ObjectUtil.isNotNull(session.getUserId()) && !ObjectUtil.equal(session.getUserId(), LoginHelper.getUserId())) {
            throw new ServiceException("监考会话不属于当前用户");
        }
        return session;
    }

    /** 校验考试是不是当前用户创建的 */
    private void checkExamOwner(Long examId) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        List<Long> ownExamIds = remoteExamService.listExamIdsByCreator(LoginHelper.getUserId());
        if (ObjectUtil.isNull(ownExamIds) || !ownExamIds.contains(examId)) {
            throw new ServiceException("只能查看自己创建的考试的监考记录");
        }
    }

    private String examNameOf(Long examId) {
        RemoteExamVo exam = remoteExamService.queryExam(examId);
        return ObjectUtil.isNull(exam) ? "" : exam.getExamName();
    }

    private String nickNameOf(Long userId) {
        try {
            return StringUtils.defaultIfBlank(remoteUserService.selectNicknameById(userId), "");
        } catch (Exception e) {
            return "";
        }
    }

    private Date parseTime(String text, Date defaultValue) {
        if (StringUtils.isBlank(text)) {
            return defaultValue;
        }
        try {
            return DateUtil.parse(text);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int secondsBetween(Date start, Date end) {
        if (ObjectUtil.isNull(start) || ObjectUtil.isNull(end)) {
            return 0;
        }
        return (int) Math.max(0, (end.getTime() - start.getTime()) / 1000);
    }

    private String clientIp() {
        try {
            return StringUtils.defaultIfBlank(ServletUtils.getClientIP(), "");
        } catch (Exception e) {
            return "";
        }
    }

    private String userAgent() {
        try {
            String ua = ServletUtils.getRequest().getHeader("User-Agent");
            return ua == null ? "" : ua.length() > 500 ? ua.substring(0, 500) : ua;
        } catch (Exception e) {
            return "";
        }
    }
}
