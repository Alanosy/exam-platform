package org.dromara.exam.mark.service.ai.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.commons.lang3.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.ai.api.RemoteAiService;
import org.dromara.exam.ai.api.domain.RemoteMarkAiBo;
import org.dromara.exam.ai.api.domain.RemoteMarkAiVo;
import org.dromara.exam.mark.service.ai.MarkAiBo;
import org.dromara.exam.mark.service.ai.MarkAiResult;
import org.dromara.exam.mark.service.ai.MarkAiService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 阅卷：走 ruoyi-exam-ai（Dubbo）→ Python agent
 *
 * <p>阅卷服务自己不知道模型在哪、用的哪家供应商，只知道「给一道题一个建议分」。
 * 这条链路上的超时、主备切换、熔断全部由 agent 侧的模型网关处理。
 *
 * <p>装配条件：默认启用（非显式 {@code exam.mark.ai.enabled=false} 都走这里）。
 * 想退回「未接入」占位实现，把该配置置为 false 即可。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "exam.mark.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class RemoteMarkAiServiceImpl implements MarkAiService {

    /** 置信度低于这个值就不建议教师直接采纳 */
    private static final BigDecimal LOW_CONFIDENCE = new BigDecimal("0.6");

    @DubboReference
    private RemoteAiService remoteAiService;

    @Override
    public boolean enabled() {
        try {
            return remoteAiService.enabled();
        } catch (Exception e) {
            log.warn("判断 AI 可用性失败 {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MarkAiResult judge(MarkAiBo bo) {
        if (bo == null) {
            return MarkAiResult.fail("判分材料为空");
        }
        RemoteMarkAiVo vo;
        try {
            vo = remoteAiService.judgeMark(toRemoteBo(bo));
        } catch (Exception e) {
            log.warn("AI 评分失败 questionId={} {}", bo.getQuestionId(), e.getMessage());
            return MarkAiResult.fail("AI 评分失败: " + e.getMessage());
        }
        return toResult(vo);
    }

    @Override
    public List<MarkAiResult> judgeBatch(List<MarkAiBo> bos) {
        if (bos == null || bos.isEmpty()) {
            return List.of();
        }
        List<RemoteMarkAiBo> remoteBos = new ArrayList<>(bos.size());
        for (MarkAiBo bo : bos) {
            remoteBos.add(toRemoteBo(bo));
        }
        List<RemoteMarkAiVo> vos;
        try {
            vos = remoteAiService.judgeMarkBatch(remoteBos);
        } catch (Exception e) {
            log.warn("AI 批量评分失败，按条数补失败结果 {}", e.getMessage());
            List<MarkAiResult> failed = new ArrayList<>(bos.size());
            for (int i = 0; i < bos.size(); i++) {
                failed.add(MarkAiResult.fail("AI 批量评分失败: " + e.getMessage()));
            }
            return failed;
        }
        List<MarkAiResult> out = new ArrayList<>(bos.size());
        for (int i = 0; i < bos.size(); i++) {
            if (i >= vos.size()) {
                out.add(MarkAiResult.fail("AI 未返回该条结果"));
                continue;
            }
            out.add(toResult(vos.get(i)));
        }
        return out;
    }

    private RemoteMarkAiBo toRemoteBo(MarkAiBo bo) {
        RemoteMarkAiBo remote = new RemoteMarkAiBo();
        remote.setQuestionId(bo.getQuestionId());
        remote.setQuestionType(bo.getQuestionType());
        remote.setTitle(bo.getTitle());
        remote.setStandardAnswer(bo.getStandardAnswer());
        remote.setAnalysis(bo.getAnalysis());
        remote.setAnswerText(bo.getAnswerText());
        remote.setFullScore(bo.getFullScore());
        remote.setTenantId(TenantHelper.getTenantId());
        return remote;
    }

    private MarkAiResult toResult(RemoteMarkAiVo vo) {
        if (vo == null || !Boolean.TRUE.equals(vo.getSuccess()) || vo.getScore() == null) {
            return MarkAiResult.fail(vo == null ? "AI 未返回结果" : StringUtils.defaultIfBlank(vo.getMessage(), "AI 评估失败"));
        }
        String reason = StringUtils.defaultIfBlank(vo.getReason(), "");
        // 置信度低的评分不能让教师无脑采纳，直接在理由里点出来
        if (vo.getConfidence() != null && vo.getConfidence().compareTo(LOW_CONFIDENCE) < 0) {
            reason = "【置信度偏低，建议人工复核】" + reason;
        } else if (Boolean.TRUE.equals(vo.getNeedHuman())) {
            reason = "【AI 建议转人工】" + reason;
        }
        return MarkAiResult.ok(vo.getScore(), reason, vo.getModel());
    }
}
