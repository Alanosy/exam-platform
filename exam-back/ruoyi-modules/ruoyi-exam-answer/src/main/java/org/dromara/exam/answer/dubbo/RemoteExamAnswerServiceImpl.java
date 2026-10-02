package org.dromara.exam.answer.dubbo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RecordExamStatVo;
import org.dromara.exam.answer.api.domain.RecordStatVo;
import org.dromara.exam.answer.api.domain.RecordTrendVo;
import org.dromara.exam.answer.api.domain.RemoteAnswerVo;
import org.dromara.exam.answer.api.domain.RemoteMarkScoreBo;
import org.dromara.exam.answer.api.domain.RemoteMarkWriteBackBo;
import org.dromara.exam.answer.api.domain.RemoteRecordVo;
import org.dromara.exam.answer.domain.ExamAnswer;
import org.dromara.exam.answer.domain.ExamRecord;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.answer.mapper.ExamAnswerMapper;
import org.dromara.exam.answer.mapper.ExamRecordMapper;
import org.dromara.exam.cert.api.RemoteCertService;
import org.dromara.exam.cert.api.domain.RemoteCertIssueBo;
import org.dromara.exam.paper.api.RemotePaperService;
import org.dromara.exam.paper.api.domain.RemotePaperVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 答卷服务对外实现（供阅卷服务读写答卷与成绩）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteExamAnswerServiceImpl implements RemoteExamAnswerService {

    private final ExamRecordMapper examRecordMapper;

    private final ExamAnswerMapper examAnswerMapper;

    @DubboReference
    private RemoteCertService remoteCertService;

    @DubboReference
    private RemotePaperService remotePaperService;

    /**
     * 查询答卷记录
     */
    @Override
    public RemoteRecordVo queryRecord(Long recordId) {
        if (ObjectUtil.isNull(recordId)) {
            return null;
        }
        return toVo(examRecordMapper.selectById(recordId));
    }

    /**
     * 查询某场考试下的答卷记录
     */
    @Override
    public List<RemoteRecordVo> listRecordsByExam(Long examId, String status) {
        if (ObjectUtil.isNull(examId)) {
            return List.of();
        }
        List<ExamRecord> list = examRecordMapper.selectList(
            Wrappers.lambdaQuery(ExamRecord.class)
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getDelFlag, 0L)
                .eq(StringUtils.isNotBlank(status), ExamRecord::getStatus, status)
                .orderByDesc(ExamRecord::getSubmitTime)
                .orderByDesc(ExamRecord::getId));
        List<RemoteRecordVo> voList = new ArrayList<>(list.size());
        for (ExamRecord record : list) {
            voList.add(toVo(record));
        }
        return voList;
    }

    /**
     * 查询某份答卷的逐题作答
     */
    @Override
    public List<RemoteAnswerVo> listAnswers(Long recordId) {
        if (ObjectUtil.isNull(recordId)) {
            return List.of();
        }
        List<ExamAnswer> list = examAnswerMapper.selectList(
            Wrappers.lambdaQuery(ExamAnswer.class)
                .eq(ExamAnswer::getRecordId, recordId)
                .eq(ExamAnswer::getDelFlag, 0L)
                .orderByAsc(ExamAnswer::getSort));
        List<RemoteAnswerVo> voList = new ArrayList<>(list.size());
        for (ExamAnswer answer : list) {
            RemoteAnswerVo vo = MapstructUtils.convert(answer, RemoteAnswerVo.class);
            if (ObjectUtil.isNull(vo)) {
                continue;
            }
            vo.setAnswerId(answer.getId());
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 查有答卷的考试ID列表
     */
    @Override
    public List<Long> listExamIdsHasRecord() {
        List<ExamRecord> list = examRecordMapper.selectList(
            Wrappers.lambdaQuery(ExamRecord.class)
                .select(ExamRecord::getExamId)
                .eq(ExamRecord::getDelFlag, 0L));
        return list.stream().map(ExamRecord::getExamId).distinct().toList();
    }

    /* --------------------------------- 统计 --------------------------------- */

    @Override
    public RecordStatVo statRecords() {
        RecordStatVo vo = new RecordStatVo();
        Map<String, Object> row = firstRow(examRecordMapper.selectMaps(
            Wrappers.<ExamRecord>query()
                .select(true, List.of(
                    "count(1) as total_cnt",
                    "sum(case when status = '" + ExamRecord.STATUS_SUBMITTED + "' then 1 else 0 end) as submit_cnt",
                    "sum(case when status = '" + ExamRecord.STATUS_ANSWERING + "' then 1 else 0 end) as answering_cnt",
                    "sum(case when status = '" + ExamRecord.STATUS_SUBMITTED + "' and passed = 1 then 1 else 0 end) as pass_cnt",
                    "count(distinct user_id) as examinee_cnt",
                    "ifnull(round(avg(case when status = '" + ExamRecord.STATUS_SUBMITTED
                        + "' then total_score end), 1), 0) as avg_score"))));
        Long submitted = num(row, "submitCnt", "submit_cnt");
        Long passed = num(row, "passCnt", "pass_cnt");
        vo.setTotal(num(row, "totalCnt", "total_cnt"));
        vo.setSubmitted(submitted);
        vo.setAnswering(num(row, "answeringCnt", "answering_cnt"));
        vo.setPassed(passed);
        vo.setExamineeCount(num(row, "examineeCnt", "examinee_cnt"));
        vo.setAvgScore(dec(row, "avgScore", "avg_score"));
        vo.setPassRate(rate(passed, submitted));
        vo.setTodaySubmit(examRecordMapper.selectCount(
            Wrappers.lambdaQuery(ExamRecord.class)
                .eq(ExamRecord::getStatus, ExamRecord.STATUS_SUBMITTED)
                .between(ExamRecord::getSubmitTime, DateUtil.beginOfDay(new Date()), DateUtil.endOfDay(new Date()))));
        return vo;
    }

    @Override
    public List<RecordTrendVo> trendSubmit(int days) {
        int dayCount = days < 1 ? 7 : days;
        Date from = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -(dayCount - 1)));
        Map<String, Map<String, Object>> dayMap = examRecordMapper.selectMaps(
                Wrappers.<ExamRecord>query()
                    .select(true, List.of(
                        "date(submit_time) as day",
                        "count(1) as submit_cnt",
                        "sum(case when passed = 1 then 1 else 0 end) as pass_cnt",
                        "ifnull(round(avg(total_score), 1), 0) as avg_score"))
                    .eq("status", ExamRecord.STATUS_SUBMITTED)
                    .ge("submit_time", from)
                    .groupBy("date(submit_time)"))
            .stream()
            .collect(Collectors.toMap(
                item -> String.valueOf(item.getOrDefault("day", item.get("DAY"))),
                item -> item,
                (a, b) -> a));
        // 没有交卷的那天在库里没有行，这里补 0 —— 不补的话前端折线会把两个有数据的日期直接连起来
        List<RecordTrendVo> list = new ArrayList<>(dayCount);
        for (int i = dayCount - 1; i >= 0; i--) {
            String day = DateUtil.formatDate(DateUtil.offsetDay(new Date(), -i));
            Map<String, Object> row = dayMap.getOrDefault(day, Map.of());
            RecordTrendVo vo = new RecordTrendVo();
            vo.setDate(day);
            vo.setSubmitCount(num(row, "submitCnt", "submit_cnt"));
            vo.setPassCount(num(row, "passCnt", "pass_cnt"));
            vo.setAvgScore(dec(row, "avgScore", "avg_score"));
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<RecordExamStatVo> rankByExam(int limit) {
        int size = limit < 1 ? 5 : limit;
        return examRecordMapper.selectMaps(
                Wrappers.<ExamRecord>query()
                    .select(true, List.of(
                        "exam_id",
                        "count(1) as submit_cnt",
                        "sum(case when passed = 1 then 1 else 0 end) as pass_cnt",
                        "ifnull(round(avg(total_score), 1), 0) as avg_score"))
                    .eq("status", ExamRecord.STATUS_SUBMITTED)
                    .isNotNull("exam_id")
                    .groupBy("exam_id")
                    .orderByDesc("submit_cnt"))
            .stream()
            .limit(size)
            .map(item -> {
                RecordExamStatVo vo = new RecordExamStatVo();
                Long submitCount = num(item, "submitCnt", "submit_cnt");
                Long passCount = num(item, "passCnt", "pass_cnt");
                vo.setExamId(num(item, "examId", "exam_id"));
                vo.setSubmitCount(submitCount);
                vo.setPassCount(passCount);
                vo.setPassRate(rate(passCount, submitCount));
                vo.setAvgScore(dec(item, "avgScore", "avg_score"));
                return vo;
            }).toList();
    }

    /**
     * 回写阅卷结果
     *
     * <p>成绩只有这一处写入口：阅卷服务判完分调用本方法，
     * 逐题更新 exam_answer，再重算 exam_record 的主观题分 / 总分 / 及格。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void writeBackMark(RemoteMarkWriteBackBo bo) {
        if (ObjectUtil.isNull(bo) || ObjectUtil.isNull(bo.getRecordId())) {
            return;
        }
        ExamRecord record = examRecordMapper.selectById(bo.getRecordId());
        if (ObjectUtil.isNull(record)) {
            log.warn("回写阅卷结果失败，答卷不存在 recordId={}", bo.getRecordId());
            return;
        }
        Map<Long, ExamAnswer> answerMap = selectAnswerMap(bo.getRecordId());
        List<RemoteMarkScoreBo> scores = ObjectUtil.isNull(bo.getScores()) ? Collections.emptyList() : bo.getScores();
        for (RemoteMarkScoreBo item : scores) {
            if (ObjectUtil.isNull(item) || ObjectUtil.isNull(item.getQuestionId())) {
                continue;
            }
            ExamAnswer answer = answerMap.get(item.getQuestionId());
            if (ObjectUtil.isNull(answer)) {
                continue;
            }
            answer.setScore(ObjectUtil.defaultIfNull(item.getScore(), BigDecimal.ZERO));
            answer.setCorrect(ObjectUtil.defaultIfNull(item.getCorrect(), ExamAnswer.CORRECT_UNKNOWN));
            examAnswerMapper.updateById(answer);
        }

        ExamRecord update = new ExamRecord();
        update.setId(record.getId());
        update.setSubjectiveScore(ObjectUtil.defaultIfNull(bo.getSubjectiveScore(), BigDecimal.ZERO));
        if (Boolean.TRUE.equals(bo.getFinished())) {
            BigDecimal total = ObjectUtil.defaultIfNull(record.getObjectiveScore(), BigDecimal.ZERO)
                .add(update.getSubjectiveScore());
            update.setTotalScore(total);
            update.setPassed(total.compareTo(ObjectUtil.defaultIfNull(record.getPassScore(), BigDecimal.ZERO)) >= 0 ? 1L : 0L);
        }
        examRecordMapper.updateById(update);
        log.info("阅卷结果回写完成 recordId={}, 主观题分={}, 完成={}", record.getId(), update.getSubjectiveScore(), bo.getFinished());
        // 成绩到这里才算定稿：交卷时因为有主观题没阅所以没发证书，这里补发
        if (Boolean.TRUE.equals(bo.getFinished()) && ObjectUtil.equal(1L, update.getPassed())) {
            record.setTotalScore(update.getTotalScore());
            record.setPassed(update.getPassed());
            issueCertificate(record);
        }
    }

    /**
     * 阅卷完成后补发证书
     *
     * <p>含主观题的卷子，交卷那一刻只有客观题分，总分要等阅完才知道，
     * 所以证书只能在这里发 —— 早发了分数对不上，晚发了考生查不到证书。
     *
     * <p>发不出证书不影响成绩落库，异常只记 warn。
     */
    private void issueCertificate(ExamRecord record) {
        try {
            RemotePaperVo paper = remotePaperService.queryPaper(record.getPaperId());
            RemoteCertIssueBo certBo = new RemoteCertIssueBo();
            certBo.setExamId(record.getExamId());
            certBo.setRecordId(record.getId());
            certBo.setUserId(record.getUserId());
            certBo.setAccount(record.getAccount());
            certBo.setAttemptNo(record.getAttemptNo());
            certBo.setScore(record.getTotalScore());
            certBo.setPassScore(ObjectUtil.defaultIfNull(record.getPassScore(), BigDecimal.ZERO));
            certBo.setTotalScore(ObjectUtil.isNull(paper) ? null
                : BigDecimal.valueOf(ObjectUtil.defaultIfNull(paper.getTotalScore(), 0L)));
            certBo.setPassed(Boolean.TRUE);
            certBo.setSubmitTime(ObjectUtil.defaultIfNull(record.getSubmitTime(), new Date()));
            // 跨服务调用拿不到租户上下文，显式带过去
            certBo.setTenantId(TenantHelper.getTenantId());
            remoteCertService.issueOnPass(certBo);
        } catch (Exception e) {
            log.warn("颁发证书失败 recordId={}, {}", record.getId(), e.getMessage());
        }
    }

    private RemoteRecordVo toVo(ExamRecord record) {
        if (ObjectUtil.isNull(record)) {
            return null;
        }
        RemoteRecordVo vo = MapstructUtils.convert(record, RemoteRecordVo.class);
        if (ObjectUtil.isNull(vo)) {
            return null;
        }
        vo.setRecordId(record.getId());
        return vo;
    }

    /**
     * 取聚合查询的唯一结果行
     *
     * <p>聚合查询没有 from row 也要返回一个全 0 的行，调用方就不必判空：
     * 「没有答卷」和「有 0 张答卷」对统计来说是一件事，不该分两条分支去处理。
     */
    private Map<String, Object> firstRow(List<Map<String, Object>> rows) {
        return CollUtil.isEmpty(rows) ? Map.of() : rows.get(0);
    }

    /**
     * 取聚合列的值
     *
     * <p>selectMaps 返回的 key 受全局 map-underscore-to-camel-case 影响，
     * 两种写法都兜一下，免得换个 MyBatis 版本统计值悄悄变成 0。
     */
    private Long num(Map<String, Object> row, String camelKey, String underKey) {
        Object value = row.get(camelKey);
        if (ObjectUtil.isNull(value)) {
            value = row.get(underKey);
        }
        return ObjectUtil.isNull(value) ? 0L : Long.parseLong(String.valueOf(value));
    }

    private BigDecimal dec(Map<String, Object> row, String camelKey, String underKey) {
        Object value = row.get(camelKey);
        if (ObjectUtil.isNull(value)) {
            value = row.get(underKey);
        }
        return ObjectUtil.isNull(value) ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }

    /** 占比：分子为 0 或分母为 0 都返回 0，不做除零保护以外的特殊处理 */
    private BigDecimal rate(Long numerator, Long denominator) {
        if (ObjectUtil.isNull(numerator) || ObjectUtil.isNull(denominator) || denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    private Map<Long, ExamAnswer> selectAnswerMap(Long recordId) {
        List<ExamAnswer> list = examAnswerMapper.selectList(
            Wrappers.lambdaQuery(ExamAnswer.class)
                .eq(ExamAnswer::getRecordId, recordId)
                .eq(ExamAnswer::getDelFlag, 0L));
        return list.stream().collect(Collectors.toMap(ExamAnswer::getQuestionId, item -> item, (a, b) -> a));
    }
}
