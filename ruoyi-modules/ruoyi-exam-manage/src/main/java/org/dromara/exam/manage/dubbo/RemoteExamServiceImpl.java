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
import org.dromara.exam.manage.mapper.ExamInviteMapper;
import org.dromara.exam.manage.mapper.ExamMapper;
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
