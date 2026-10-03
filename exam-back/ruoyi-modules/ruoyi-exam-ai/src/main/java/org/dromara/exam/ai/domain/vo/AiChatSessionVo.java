package org.dromara.exam.ai.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 历史会话
 *
 * <p>列表只带摘要（标题 / 条数 / 时间），点进去才带全量消息，
 * 避免一次把几十个会话的完整聊天记录都拉回前端。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class AiChatSessionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private String id;

    /** 会话标题（取第一条用户消息） */
    private String title;

    /** 最近一次命中的意图 */
    private String intent;

    /** 消息条数 */
    private Integer messageCount;

    /** 最后活跃时间（秒） */
    private Long updatedAt;

    /** 全量消息，仅详情接口返回 */
    private List<Map<String, Object>> messages = new ArrayList<>();
}
