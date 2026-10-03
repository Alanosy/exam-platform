package org.dromara.exam.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.ai.client.AiAgentClient;
import org.dromara.exam.ai.domain.bo.AiChatBo;
import org.dromara.exam.ai.domain.vo.AiChatVo;
import org.dromara.exam.ai.service.IChatService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话式 Agent 实现
 *
 * <p>Java 侧只做三件事：补租户与操作者、转发、把 agent 的响应翻成前端 VO。
 * 编排逻辑全在 Python 侧，Java 不复制一份。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final AiAgentClient agentClient;

    private final ObjectMapper objectMapper;

    @Override
    public AiChatVo chat(AiChatBo bo) {
        AiChatBo payload = bo == null ? new AiChatBo() : bo;
        Map<String, Object> body = new HashMap<>();
        body.put("session_id", payload.getSessionId());
        body.put("message", payload.getMessage());
        body.put("answers", payload.getAnswers());
        // 租户与操作者必须由服务端补：Agent 按租户隔离题库与考试，不能信客户端传值
        body.put("tenant_id", TenantHelper.getTenantId());
        body.put("user_id", LoginHelper.getUserId());
        body.put("model_code", payload.getModelCode());

        AiAgentClient.AgentResp resp = agentClient.post("/chat", body);
        if (resp.getCode() != 200 || resp.getData() == null || resp.getData().isNull()) {
            log.warn("对话 Agent 调用失败 code={} msg={}", resp.getCode(), resp.getMsg());
            return unavailable("AI 服务暂时不可用：" + resp.getMsg());
        }
        try {
            AiChatVo vo = objectMapper.treeToValue(resp.getData(), AiChatVo.class);
            return vo == null ? unavailable("AI 服务返回为空") : vo;
        } catch (Exception e) {
            log.warn("对话结果解析失败: {}", e.getMessage());
            throw new ServiceException("AI 服务返回结构异常");
        }
    }

    /**
     * agent 挂掉时的降级回复
     *
     * <p>带上排查提示，别让用户对着一个空气泡猜。
     */
    private AiChatVo unavailable(String reason) {
        AiChatVo vo = new AiChatVo();
        vo.setReply(reason + "\n\n> 请确认 ruoyi-exam-agent（默认 9221 端口）已启动，"
            + "且 `exam-ai.url` 指向的地址可达。");
        vo.setTrace(List.of());
        return vo;
    }
}
