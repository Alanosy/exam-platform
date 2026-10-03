package org.dromara.exam.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 运行流程的一个步骤
 *
 * <p>前端聊天窗把它渲染成时间线：用户能看到 Agent 到底调了哪个工具、
 * 跑了哪个技能、花了多久，而不是对着一个转圈的 loading 干等。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class AiChatTraceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 步骤ID */
    private String id;

    /** think 思考 / tool 工具 / skill 技能 / ask 询问 / write 写入 / done 完成 / error 出错 */
    private String type;

    /** 步骤标题 */
    private String title;

    /** 补充说明（工具端点、模型与提示词版本等） */
    private String detail;

    /** ok / error / waiting */
    private String status;

    /** 工具 code 或技能 code */
    private String ref;

    /** 耗时（毫秒） */
    @JsonAlias("latency_ms")
    private Long latencyMs;

    /** 关键结果摘要，前端折叠展示 */
    private List<String> preview;
}
