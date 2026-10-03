package org.dromara.exam.ai.service;

import org.dromara.exam.ai.domain.bo.AiChatBo;
import org.dromara.exam.ai.domain.vo.AiChatSessionVo;
import org.dromara.exam.ai.domain.vo.AiChatVo;

import java.util.List;

/**
 * 对话式 Agent
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public interface IChatService {

    /**
     * 跑一轮对话
     *
     * @param bo 入参（sessionId 为空表示新会话）
     * @return 回复 + 运行流程 + 可能的中断卡
     */
    AiChatVo chat(AiChatBo bo);

    /**
     * 当前用户的历史会话列表
     */
    List<AiChatSessionVo> listSessions();

    /**
     * 会话详情：用于「接着上次继续聊」
     *
     * @param sessionId 会话ID
     * @return 不存在时返回 null
     */
    AiChatSessionVo getSession(String sessionId);
}
