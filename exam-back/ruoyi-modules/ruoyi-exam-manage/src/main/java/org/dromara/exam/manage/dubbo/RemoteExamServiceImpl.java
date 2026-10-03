package org.dromara.exam.manage.dubbo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.exam.manage.api.RemoteExamService;
import org.dromara.exam.manage.api.domain.RemoteExamInviteVo;
import org.dromara.exam.manage.api.domain.RemoteExamVo;
import org.dromara.exam.manage.domain.Exam;
import org.dromara.exam.manage.domain.ExamInvite;
import org.dromara.exam.manage.domain.ExamUser;
import org.dromara.exam.manage.mapper.ExamInviteMapper;
import org.dromara.exam.manage.mapper.ExamMapper;
import org.dromara.exam.manage.mapper.ExamUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考试服务对外实现（供答题服务开考校验）
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteExamServiceImpl implements RemoteExamService {

    private final ExamMapper examMapper;

    private final ExamInviteMapper examInviteMapper;

    private final ExamUserMapper examUserMapper;

    /**
     * 查询考试信息
     */
    @Override
    public RemoteExamVo queryExam(Long examId) {
        if (ObjectUtil.isNull(examId)) {
            return null;
        }
        Exam exam = examMapper.selectById(examId);
        if (ObjectUtil.isNull(exam)) {
            return null;
        }
        RemoteExamVo vo = MapstructUtils.convert(exam, RemoteExamVo.class);
        if (ObjectUtil.isNull(vo)) {
            return null;
        }
        vo.setExamId(exam.getId());
        return vo;
    }

    /**
     * 查某类任务下的考试ID列表
     */
    @Override
    public List<Long> listExamIdsByType(String examType) {
        if (StringUtils.isBlank(examType)) {
            return List.of();
        }
        List<Exam> exams = examMapper.selectList(
            Wrappers.lambdaQuery(Exam.class)
                .select(Exam::getId)
                .eq(Exam::getExamType, examType)
                .eq(Exam::getDelFlag, 0L));
        return exams.stream().map(Exam::getId).toList();
    }

    /**
     * 按名称关键词模糊查询考试（AI 对话定位「某某考卷」用）
     */
    @Override
    public List<RemoteExamVo> searchExams(String keyword, Integer limit) {
        int size = (limit == null || limit < 1) ? 10 : Math.min(limit, 50);
        List<Exam> exams = examMapper.selectList(
            Wrappers.lambdaQuery(Exam.class)
                .like(StringUtils.isNotBlank(keyword), Exam::getExamName, StringUtils.trimToEmpty(keyword))
                .eq(Exam::getDelFlag, 0L)
                .orderByDesc(Exam::getId)
                .last("limit " + size));
        if (ObjectUtil.isNull(exams) || exams.isEmpty()) {
            return List.of();
        }
        return exams.stream()
            .map(this::toVo)
            .filter(ObjectUtil::isNotNull)
            .toList();
    }

    /**
     * 实体 -> 对外 VO
     *
     * <p>单独抽出来是为了让 queryExam 与 searchExams 共用同一套转换，
     * 否则两处各写一遍，改字段时必漏一处。
     */
    private RemoteExamVo toVo(Exam exam) {
        if (ObjectUtil.isNull(exam)) {
            return null;
        }
        RemoteExamVo vo = MapstructUtils.convert(exam, RemoteExamVo.class);
        if (ObjectUtil.isNull(vo)) {
            return null;
        }
        vo.setExamId(exam.getId());
        return vo;
    }

    /**
     * 查某个人创建的考试ID列表
     */
    @Override
    public List<Long> listExamIdsByCreator(Long creatorId) {
        if (ObjectUtil.isNull(creatorId)) {
            return List.of();
        }
        List<Exam> exams = examMapper.selectList(
            Wrappers.lambdaQuery(Exam.class)
                .select(Exam::getId)
                .eq(Exam::getCreatorId, creatorId)
                .eq(Exam::getDelFlag, 0L));
        return exams.stream().map(Exam::getId).toList();
    }

    /**
     * 查某个人作为白名单考生被指派的考试ID列表
     */
    @Override
    public List<Long> listExamIdsByWhiteUser(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            return List.of();
        }
        List<ExamUser> list = examUserMapper.selectList(
            Wrappers.lambdaQuery(ExamUser.class)
                .select(ExamUser::getExamId)
                .eq(ExamUser::getUserId, userId));
        return list.stream().map(ExamUser::getExamId).filter(ObjectUtil::isNotNull).distinct().toList();
    }

    /**
     * 判断某人是否在某场考试的白名单里
     */
    @Override
    public Boolean isWhiteUser(Long examId, Long userId) {
        if (ObjectUtil.isNull(examId) || ObjectUtil.isNull(userId)) {
            return false;
        }
        ExamUser exist = examUserMapper.selectOne(
            Wrappers.lambdaQuery(ExamUser.class)
                .eq(ExamUser::getExamId, examId)
                .eq(ExamUser::getUserId, userId)
                .last("limit 1"));
        return ObjectUtil.isNotNull(exist);
    }

    /**
     * 按账号查询邀请 / 加入记录
     */
    @Override
    public List<RemoteExamInviteVo> listInvitesByAccount(String account) {
        if (StringUtils.isBlank(account)) {
            return List.of();
        }
        List<ExamInvite> list = examInviteMapper.selectList(
            Wrappers.lambdaQuery(ExamInvite.class)
                .eq(ExamInvite::getInviteAccount, account)
                .orderByDesc(ExamInvite::getId));
        return MapstructUtils.convert(list, RemoteExamInviteVo.class);
    }

    /**
     * 查询某账号在某场考试上的邀请记录
     */
    @Override
    public RemoteExamInviteVo queryInvite(Long examId, String account) {
        if (ObjectUtil.isNull(examId) || StringUtils.isBlank(account)) {
            return null;
        }
        ExamInvite invite = examInviteMapper.selectOne(
            Wrappers.lambdaQuery(ExamInvite.class)
                .eq(ExamInvite::getExamId, examId)
                .eq(ExamInvite::getInviteAccount, account)
                .last("limit 1"));
        return MapstructUtils.convert(invite, RemoteExamInviteVo.class);
    }
}
