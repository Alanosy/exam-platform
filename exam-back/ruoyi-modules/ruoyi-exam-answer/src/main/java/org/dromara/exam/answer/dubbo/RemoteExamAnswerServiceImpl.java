package org.dromara.exam.answer.dubbo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RemoteAnswerVo;
import org.dromara.exam.answer.api.domain.RemoteMarkScoreBo;
import org.dromara.exam.answer.api.domain.RemoteMarkWriteBackBo;
import org.dromara.exam.answer.api.domain.RemoteRecordVo;
import org.dromara.exam.answer.domain.ExamAnswer;
import org.dromara.exam.answer.domain.ExamRecord;
import org.dromara.exam.answer.mapper.ExamAnswerMapper;
import org.dromara.exam.answer.mapper.ExamRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private Map<Long, ExamAnswer> selectAnswerMap(Long recordId) {
        List<ExamAnswer> list = examAnswerMapper.selectList(
            Wrappers.lambdaQuery(ExamAnswer.class)
                .eq(ExamAnswer::getRecordId, recordId)
                .eq(ExamAnswer::getDelFlag, 0L));
        return list.stream().collect(Collectors.toMap(ExamAnswer::getQuestionId, item -> item, (a, b) -> a));
    }
}
