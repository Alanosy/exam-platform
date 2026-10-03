package org.dromara.exam.ai.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 一轮对话的入参
 *
 * <p>answers 用于回答上一轮的中断卡：带上 sessionId 与 answers，
 * message 可以为空，Agent 会从会话里取出中断点继续跑。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class AiChatBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID，首轮不传 */
    private String sessionId;

    /** 用户消息 */
    private String message;

    /** 中断卡答案 {key: value} */
    private Map<String, Object> answers;

    /** 指定模型编码，不传走默认主备链 */
    private String modelCode;

    /**
     * 会话设置（前端设置面板）
     *
     * <p>contextRounds：带多少轮历史进上下文；
     * confirmWrite：写操作是否必须先确认；
     * planner：是否先规划再执行（关掉就退化成「直接回答」）。
     */
    private Map<String, Object> options;
}
