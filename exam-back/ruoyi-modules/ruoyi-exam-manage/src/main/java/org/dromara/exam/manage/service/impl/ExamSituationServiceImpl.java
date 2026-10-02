package org.dromara.exam.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.file.FileUtils;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.excel.utils.ExcelWriterWrapper;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RemoteRecordVo;
import org.dromara.exam.manage.domain.Exam;
import org.dromara.exam.manage.domain.ExamInvite;
import org.dromara.exam.manage.domain.ExamUser;
import org.dromara.exam.manage.domain.bo.ExamSituationBo;
import org.dromara.exam.manage.domain.vo.ExamSituationOverviewRowVo;
import org.dromara.exam.manage.domain.vo.ExamSituationOverviewVo;
import org.dromara.exam.manage.domain.vo.ExamSituationVo;
import org.dromara.exam.manage.mapper.ExamInviteMapper;
import org.dromara.exam.manage.mapper.ExamMapper;
import org.dromara.exam.manage.mapper.ExamUserMapper;
import org.dromara.exam.manage.service.IExamSituationService;
import org.dromara.exam.mark.api.RemoteMarkService;
import org.dromara.system.api.RemoteDeptService;
import org.dromara.system.api.RemoteUserService;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考试情况Service业务层处理
 *
 * <p>数据全靠跨服务拼：成绩在答题库，待阅在阅卷库，姓名在系统库，应考名单在考试库。
 * 每一路都可能拿不到（服务没启 / 租户没数据），原则是**缺哪路就降级哪一路**，
 * 不能因为阅卷服务挂了就连「谁参考了」都看不到。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExamSituationServiceImpl implements IExamSituationService {

    /** 白名单准入 */
    private static final String PARTICIPANT_WHITE = "white";

    /** 主观题没阅完时的统一叫法，页面和导出保持一致 */
    private static final String SUBJECTIVE_PENDING_LABEL = "待阅";

    private final ExamMapper examMapper;

    private final ExamUserMapper examUserMapper;

    private final ExamInviteMapper examInviteMapper;

    @DubboReference
    private RemoteExamAnswerService remoteExamAnswerService;

    @DubboReference
    private RemoteMarkService remoteMarkService;

    @DubboReference
    private RemoteUserService remoteUserService;

    @DubboReference
    private RemoteDeptService remoteDeptService;

    /**
     * 整场考试的概览
     */
    @Override
    public ExamSituationOverviewVo overview(Long examId) {
        ExamSituationOverviewVo vo = new ExamSituationOverviewVo();
        vo.setExamId(examId);
        if (ObjectUtil.isNull(examId)) {
            return vo;
        }
        Exam exam = examMapper.selectById(examId);
        if (ObjectUtil.isNull(exam)) {
            return vo;
        }
        vo.setExamName(exam.getExamName());
        vo.setExamType(exam.getExamType());
        vo.setStatus(exam.getStatus());
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        vo.setDuration(exam.getDuration());

        List<RemoteRecordVo> records = fetchRecords(examId);
        Set<Long> pendingRecordIds = fetchPendingRecordIds(examId);

        // 参考人数按人去重：同一个人考两次算一个人，但下面「已交卷」按份数算
        Set<Long> joinedUserIds = records.stream()
            .map(RemoteRecordVo::getUserId)
            .filter(ObjectUtil::isNotNull)
            .collect(Collectors.toSet());
        long answering = records.stream().filter(r -> isAnswering(r)).count();
        long submitted = records.stream().filter(r -> isSubmitted(r)).count();

        vo.setInvitedCount(countInvited(exam, examId));
        vo.setJoinedCount((long) joinedUserIds.size());
        vo.setAnsweringCount(answering);
        vo.setSubmittedCount(submitted);
        vo.setPendingMarkCount((long) pendingRecordIds.size());

        // 平均分 / 及格率只在「成绩已确定」的答卷上算：
        // 主观题没阅完时 subjectiveScore 还是 0，把它们算进来会把平均分整体拉低
        List<RemoteRecordVo> settled = records.stream()
            .filter(r -> isSubmitted(r) && !pendingRecordIds.contains(r.getRecordId()))
            .toList();
        if (CollUtil.isNotEmpty(settled)) {
            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal max = null;
            BigDecimal min = null;
            long passedCount = 0;
            for (RemoteRecordVo r : settled) {
                BigDecimal score = ObjectUtil.defaultIfNull(r.getTotalScore(), BigDecimal.ZERO);
                sum = sum.add(score);
                max = ObjectUtil.isNull(max) || score.compareTo(max) > 0 ? score : max;
                min = ObjectUtil.isNull(min) || score.compareTo(min) < 0 ? score : min;
                if (ObjectUtil.equal(r.getPassed(), 1L)) {
                    passedCount++;
                }
            }
            vo.setAvgScore(divide(sum, settled.size()));
            vo.setMaxScore(max);
            vo.setMinScore(min);
            vo.setPassedCount(passedCount);
            vo.setPassRate(rate(passedCount, settled.size()));
            vo.setPassScore(settled.get(0).getPassScore());
        } else {
            vo.setAvgScore(BigDecimal.ZERO);
            vo.setPassedCount(0L);
            vo.setPassRate(BigDecimal.ZERO);
        }
        if (ObjectUtil.isNull(vo.getPassScore()) && CollUtil.isNotEmpty(records)) {
            // 一场全是待阅时上面那段不会执行，及格分仍要给用户看
            vo.setPassScore(records.get(0).getPassScore());
        }
        return vo;
    }

    /**
     * 参考名单与成绩（分页）
     *
     * <p>远程接口只提供「按考试查全部」，不分页。这里拉回来自己在内存过滤 + 分页：
     * 一场考试的人数天然有上限（几百到顶），比为了分页去改跨服务接口划算。
     */
    @Override
    public TableDataInfo<ExamSituationVo> listSituation(ExamSituationBo bo, PageQuery pageQuery) {
        List<ExamSituationVo> rows = queryRows(bo);
        if (CollUtil.isEmpty(rows)) {
            return TableDataInfo.build(new ArrayList<>());
        }
        int total = rows.size();
        int pageNum = ObjectUtil.defaultIfNull(pageQuery.getPageNum(), 1);
        int pageSize = ObjectUtil.defaultIfNull(pageQuery.getPageSize(), 10);
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<ExamSituationVo> pageList = from >= to ? new ArrayList<>() : new ArrayList<>(rows.subList(from, to));
        return buildPageTable(pageList, total, pageNum, pageSize);
    }

    /**
     * 查出符合过滤条件的全部行
     */
    @Override
    public List<ExamSituationVo> queryRows(ExamSituationBo bo) {
        if (ObjectUtil.isNull(bo) || ObjectUtil.isNull(bo.getExamId())) {
            return List.of();
        }
        Long examId = bo.getExamId();
        List<RemoteRecordVo> records = fetchRecords(examId);
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }
        Set<Long> pendingRecordIds = fetchPendingRecordIds(examId);
        Map<Long, RemoteUserVo> userMap = fetchUserMap(records);
        Map<Long, String> deptMap = fetchDeptMap(userMap.values());

        List<ExamSituationVo> rows = new ArrayList<>(records.size());
        for (RemoteRecordVo record : records) {
            boolean pending = pendingRecordIds.contains(record.getRecordId());
            ExamSituationVo vo = toVo(record, pending, userMap, deptMap);
            if (matchFilter(vo, bo)) {
                rows.add(vo);
            }
        }
        return rows;
    }

    /**
     * 导出：成绩概览一张 sheet + 考生明细一张 sheet
     *
     * <p>框架只有「单类型单 sheet」的简便方法，没有两个异构 sheet 的重载，所以走
     * Consumer 版自己拼：headType 传 Object，两张 sheet 各自用 head(Class) 指定表头。
     * 注意这个便捷方法不会帮忙写响应头（resetResponse 是框架私有的），这里得自己设。
     */
    @Override
    public void export(ExamSituationBo bo, HttpServletResponse response) {
        if (ObjectUtil.isNull(bo) || ObjectUtil.isNull(bo.getExamId())) {
            return;
        }
        // 文件名取概览里的考试名，顺带复用那次查询，不再单独查一遍考试表
        ExamSituationOverviewVo overview = overview(bo.getExamId());
        String filename = StringUtils.isNotBlank(overview.getExamName()) ? overview.getExamName() : "考试情况";
        List<ExamSituationOverviewRowVo> overviewRows = buildOverviewRows(overview);
        List<ExamSituationVo> rows = queryRows(bo);

        FileUtils.setAttachmentResponseHeader(response, filename + "_考试情况.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        try {
            ServletOutputStream os = response.getOutputStream();
            ExcelUtil.exportExcel(Object.class, os, (wrapper -> {
                WriteSheet overviewSheet = ExcelWriterWrapper.sheetBuilder(0, "成绩概览")
                    .head(ExamSituationOverviewRowVo.class)
                    .build();
                wrapper.write(new ArrayList<>(overviewRows), overviewSheet);

                WriteSheet detailSheet = ExcelWriterWrapper.sheetBuilder(1, "考生明细")
                    .head(ExamSituationVo.class)
                    .build();
                wrapper.write(new ArrayList<>(rows), detailSheet);
            }));
        } catch (IOException e) {
            throw new ServiceException("导出考试情况失败");
        }
    }

    /* ---------------------------------- 内部数据拼装 ---------------------------------- */

    /**
     * 导出的概览页：一行一个指标
     */
    private List<ExamSituationOverviewRowVo> buildOverviewRows(ExamSituationOverviewVo overview) {
        List<ExamSituationOverviewRowVo> rows = new ArrayList<>();
        rows.add(row("考试名称", overview.getExamName()));
        rows.add(row("考试类型", "1".equals(overview.getExamType()) ? "正式考试" : "练习考试"));
        rows.add(row("考试时间", ObjectUtil.isNull(overview.getStartTime()) ? "-" : String.valueOf(overview.getStartTime())));
        rows.add(row("应考人数", overview.getInvitedCount()));
        rows.add(row("参考人数", overview.getJoinedCount()));
        rows.add(row("已交卷", overview.getSubmittedCount()));
        rows.add(row("答题中", overview.getAnsweringCount()));
        rows.add(row("待阅卷", overview.getPendingMarkCount()));
        rows.add(row("及格分", overview.getPassScore()));
        rows.add(row("平均分", overview.getAvgScore()));
        rows.add(row("最高分", overview.getMaxScore()));
        rows.add(row("最低分", overview.getMinScore()));
        rows.add(row("及格人数", overview.getPassedCount()));
        rows.add(row("及格率", ObjectUtil.isNull(overview.getPassRate()) ? "-" : overview.getPassRate() + "%"));
        return rows;
    }

    private ExamSituationOverviewRowVo row(String label, Object value) {
        return new ExamSituationOverviewRowVo(label, ObjectUtil.isNull(value) ? "-" : String.valueOf(value));
    }

    /**
     * 答卷记录：答题服务挂了就当这场还没人考，不影响考试信息本身展示
     */
    private List<RemoteRecordVo> fetchRecords(Long examId) {
        try {
            List<RemoteRecordVo> list = remoteExamAnswerService.listRecordsByExam(examId, null);
            return CollUtil.isEmpty(list) ? List.of() : list;
        } catch (Exception e) {
            log.warn("查询考试答卷失败 examId={} {}", examId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 还没阅完的答卷ID
     */
    private Set<Long> fetchPendingRecordIds(Long examId) {
        try {
            List<Long> ids = remoteMarkService.listPendingRecordIds(examId);
            return CollUtil.isEmpty(ids) ? Set.of() : new HashSet<>(ids);
        } catch (Exception e) {
            log.warn("查询待阅答卷失败 examId={} {}", examId, e.getMessage());
            return Set.of();
        }
    }

    /**
     * 一次性把考生姓名、账号、部门ID捞回来，避免一行一次远程调用
     */
    private Map<Long, RemoteUserVo> fetchUserMap(List<RemoteRecordVo> records) {
        List<Long> userIds = records.stream()
            .map(RemoteRecordVo::getUserId)
            .filter(ObjectUtil::isNotNull)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(userIds)) {
            return Map.of();
        }
        try {
            List<RemoteUserVo> users = remoteUserService.selectListByIds(userIds);
            if (CollUtil.isEmpty(users)) {
                return Map.of();
            }
            return users.stream()
                .filter(u -> ObjectUtil.isNotNull(u.getUserId()))
                .collect(Collectors.toMap(RemoteUserVo::getUserId, Function.identity(), (a, b) -> a));
        } catch (Exception e) {
            log.warn("查询考生信息失败 {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 部门名一次查回来再分发
     */
    private Map<Long, String> fetchDeptMap(Collection<RemoteUserVo> users) {
        List<Long> deptIds = users.stream()
            .map(RemoteUserVo::getDeptId)
            .filter(ObjectUtil::isNotNull)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(deptIds)) {
            return Map.of();
        }
        try {
            Map<Long, String> names = remoteDeptService.selectDeptNamesByIds(deptIds);
            return ObjectUtil.isNull(names) ? Map.of() : names;
        } catch (Exception e) {
            log.warn("查询部门名称失败 {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 一行记录转 VO
     */
    private ExamSituationVo toVo(RemoteRecordVo record, boolean pending,
                                 Map<Long, RemoteUserVo> userMap, Map<Long, String> deptMap) {
        ExamSituationVo vo = new ExamSituationVo();
        vo.setRecordId(record.getRecordId());
        vo.setUserId(record.getUserId());
        vo.setAccount(record.getAccount());
        vo.setAttemptNo(record.getAttemptNo());
        vo.setStatus(record.getStatus());
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setUsedSeconds(record.getUsedSeconds());
        vo.setQuestionCount(record.getQuestionCount());
        vo.setAnsweredCount(record.getAnsweredCount());
        vo.setObjectiveScore(record.getObjectiveScore());
        vo.setSubjectiveScore(record.getSubjectiveScore());
        vo.setTotalScore(record.getTotalScore());
        vo.setPassScore(record.getPassScore());
        vo.setPassed(record.getPassed());
        vo.setAutoSubmit(record.getAutoSubmit());
        vo.setPendingMark(pending);

        RemoteUserVo user = ObjectUtil.isNull(record.getUserId()) ? null : userMap.get(record.getUserId());
        if (ObjectUtil.isNotNull(user)) {
            vo.setNickName(StringUtils.isNotBlank(user.getNickName()) ? user.getNickName() : user.getUserName());
            vo.setDeptName(ObjectUtil.isNull(user.getDeptId()) ? null : deptMap.get(user.getDeptId()));
            if (StringUtils.isBlank(vo.getAccount())) {
                vo.setAccount(user.getUserName());
            }
        }
        // 姓名拿不到时退回账号，别让这一列空着
        if (StringUtils.isBlank(vo.getNickName())) {
            vo.setNickName(vo.getAccount());
        }

        vo.setStatusLabel(statusLabel(record.getStatus(), pending));
        vo.setUsedTimeLabel(formatSeconds(record.getUsedSeconds()));
        vo.setSubjectiveScoreLabel(pending
            ? SUBJECTIVE_PENDING_LABEL
            : String.valueOf(ObjectUtil.defaultIfNull(record.getSubjectiveScore(), BigDecimal.ZERO)));
        vo.setPassedLabel(pending
            ? SUBJECTIVE_PENDING_LABEL
            : (ObjectUtil.equal(record.getPassed(), 1L) ? "及格" : "不及格"));
        vo.setAutoSubmitLabel(ObjectUtil.equal(record.getAutoSubmit(), 1L) ? "是" : "否");
        return vo;
    }

    /**
     * 状态 + 待阅合成一句话给导出用
     */
    private String statusLabel(String status, boolean pending) {
        String base = switch (ObjectUtil.defaultIfNull(status, "")) {
            case "answering" -> "答题中";
            case "submitted" -> "已交卷";
            case "expired" -> "超时作废";
            default -> "-";
        };
        return pending ? base + "（待阅）" : base;
    }

    /**
     * 页面上的关键词 / 状态 / 及格 / 待阅筛选（在这层做，因为跨服务没有这些filter）
     */
    private boolean matchFilter(ExamSituationVo vo, ExamSituationBo bo) {
        if (StringUtils.isNotBlank(bo.getKeyword())) {
            String keyword = bo.getKeyword().trim();
            boolean hit = StringUtils.contains(vo.getNickName(), keyword)
                || StringUtils.contains(vo.getAccount(), keyword)
                || StringUtils.contains(vo.getDeptName(), keyword);
            if (!hit) {
                return false;
            }
        }
        if (StringUtils.isNotBlank(bo.getStatus()) && !ObjectUtil.equal(vo.getStatus(), bo.getStatus())) {
            return false;
        }
        if (ObjectUtil.isNotNull(bo.getPassed())) {
            // 待阅的成绩还没定，不属于任何一边
            if (Boolean.TRUE.equals(vo.getPendingMark())) {
                return false;
            }
            if (!ObjectUtil.equal(vo.getPassed(), bo.getPassed().longValue())) {
                return false;
            }
        }
        if (ObjectUtil.isNotNull(bo.getPendingMark()) && ObjectUtil.equal(bo.getPendingMark(), 1)
            && !Boolean.TRUE.equals(vo.getPendingMark())) {
            return false;
        }
        return true;
    }

    /**
     * 应考人数：白名单看 exam_user，公开链接看已接受的邀请
     */
    private Long countInvited(Exam exam, Long examId) {
        try {
            if (PARTICIPANT_WHITE.equals(exam.getParticipantType())) {
                Long count = examUserMapper.selectCount(
                    Wrappers.lambdaQuery(ExamUser.class).eq(ExamUser::getExamId, examId));
                return ObjectUtil.defaultIfNull(count, 0L);
            }
            Long count = examInviteMapper.selectCount(
                Wrappers.lambdaQuery(ExamInvite.class).eq(ExamInvite::getExamId, examId));
            return ObjectUtil.defaultIfNull(count, 0L);
        } catch (Exception e) {
            log.warn("统计应考人数失败 examId={} {}", examId, e.getMessage());
            return 0L;
        }
    }

    /**
     * 秒转 mm:ss
     */
    private String formatSeconds(Integer seconds) {
        if (ObjectUtil.isNull(seconds) || seconds <= 0) {
            return "-";
        }
        return String.format("%d分%02d秒", seconds / 60, seconds % 60);
    }

    private BigDecimal divide(BigDecimal value, long count) {
        if (count <= 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    /**
     * 包一层 Page 拼 TableDataInfo，避开三元表达式里的泛型推断问题
     */
    private TableDataInfo<ExamSituationVo> buildPageTable(List<ExamSituationVo> pageList, int total, int pageNum, int pageSize) {
        Page<ExamSituationVo> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pageList);
        return TableDataInfo.build(page);
    }

    /**
     * 答题中 / 已交卷：答卷服务没有把这些状态值导出成常量，这里集中定义避免各处写错
     */
    private static boolean isSubmitted(RemoteRecordVo record) {
        return "submitted".equals(record.getStatus());
    }

    private static boolean isAnswering(RemoteRecordVo record) {
        return "answering".equals(record.getStatus());
    }
}
