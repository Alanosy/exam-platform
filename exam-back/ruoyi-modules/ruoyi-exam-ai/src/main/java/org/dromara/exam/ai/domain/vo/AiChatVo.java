package org.dromara.exam.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 一轮对话的结果
 *
 * <p>reply 是 Markdown，前端直接渲染；trace 是运行流程；ask 不为空表示
 * 「我在等你补充信息」，此时前端渲染中断卡而不是普通气泡。
 *
 * <p><b>不使用 SnakeCaseStrategy</b>：前端类型全是 camelCase（sessionId / submitText），
 * 用蛇形命名会导致前端读不到字段，会话续跑直接断链。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class AiChatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID，下一轮要带回来 */
    @JsonAlias("session_id")
    private String sessionId;

    /** Markdown 正文 */
    private String reply;

    /** 运行流程 */
    private List<AiChatTraceVo> trace;

    /** 中断卡，为空表示本轮已结束 */
    private AiChatAskVo ask;

    /** 命中的意图 */
    private String intent;

    /** 结构化产物（题目列表、统计数据等） */
    private Map<String, Object> data;
}
