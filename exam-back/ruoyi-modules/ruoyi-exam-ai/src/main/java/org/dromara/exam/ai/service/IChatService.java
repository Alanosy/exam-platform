package org.dromara.exam.ai.service;

import org.dromara.exam.ai.domain.bo.AiChatBo;
import org.dromara.exam.ai.domain.vo.AiChatVo;

/**
 * 对话式 Agent（转发到 ruoyi-exam-agent）
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public interface IChatService {

    /**
     * 跑一轮对话
     *
     * <p>agent 不可用时返回一条说明性回复，不抛异常——聊天窗可以降级，
     * 但不能因为 AI 挂了就让整个页面报错。
     *
     * @param bo 对话入参
     * @return 对话结果，永远非空
     */
    AiChatVo chat(AiChatBo bo);
}
