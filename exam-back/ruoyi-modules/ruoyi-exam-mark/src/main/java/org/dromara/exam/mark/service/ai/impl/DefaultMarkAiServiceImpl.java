package org.dromara.exam.mark.service.ai.impl;

import lombok.extern.slf4j.Slf4j;
import org.dromara.exam.mark.service.ai.MarkAiBo;
import org.dromara.exam.mark.service.ai.MarkAiResult;
import org.dromara.exam.mark.service.ai.MarkAiService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * AI 阅卷默认实现：未接入大模型
 *
 * <p>占位实现，保证「AI 预评」这条链路在没有任何模型时也能跑通——
 * 点了会明确告诉用户没接，而不是假装给了分。
 *
 * <p>默认<b>不装配</b>：接了 ruoyi-exam-ai 之后由 RemoteMarkAiServiceImpl 顶上，
 * 两个实现不能同时存在（MarkServiceImpl 是单实例注入）。
 * 需要退回占位实现时配置 exam.mark.ai.enabled=false。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "exam.mark.ai", name = "enabled", havingValue = "false")
public class DefaultMarkAiServiceImpl implements MarkAiService {

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public MarkAiResult judge(MarkAiBo bo) {
        log.info("AI 阅卷未接入，跳过 questionId={}", bo == null ? null : bo.getQuestionId());
        return MarkAiResult.fail("AI 阅卷能力未接入，请先在阅卷服务配置大模型");
    }
}
