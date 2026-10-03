package org.dromara.exam.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Python Agent 服务连接配置
 *
 * <p>对应 Nacos 配置 ruoyi-exam-ai.yml 的 exam.ai 前缀。这里全部给了默认值，
 * 不配也能连本机默认端口的 agent，方便本地联调。
 *
 * <p>两种寻址方式二选一：
 * <ol>
 *   <li>{@code url} 有值 → 直连（本地开发 / agent 不注册 Nacos / Docker 端口映射时用）</li>
 *   <li>{@code url} 为空 → 按 {@code serviceName} 走 Nacos 服务发现</li>
 * </ol>
 *
 * <p><b>为什么不用 Feign/Dubbo 直连</b>：agent 是 Python 服务，没有 Dubbo 接口，
 * 也不打算为了被调用就引入一整套 Java 注册逻辑。REST 是最省事且最易排查的选择。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
@Component
@ConfigurationProperties(prefix = "exam.ai")
public class AiAgentProperties {

    /** 是否启用 AI 能力，关掉后所有 AI 接口返回「未启用」，前端隐藏入口 */
    private Boolean enabled = true;

    /** agent 服务地址，形如 http://127.0.0.1:9221；留空则走服务发现 */
    private String url = "http://127.0.0.1:9221";

    /** agent 在 Nacos 上的服务名（url 为空时生效） */
    private String serviceName = "ruoyi-exam-agent";

    /** 接口前缀，与 agent 侧 API_PREFIX 保持一致 */
    private String apiPrefix = "/api/ai";

    /** 连接超时（毫秒） */
    private Integer connectTimeout = 3000;

    /** 读取超时（毫秒）。主观题评分涉及长文本，别设太短 */
    private Integer readTimeout = 60000;

    /** 单题评分的读取超时（毫秒），批量预评时逐题调用用这个 */
    private Integer markTimeout = 45000;

    /** 批量并发度，批量预评时一次提交多少条给 agent */
    private Integer batchParallel = 4;

    /** 调用失败重试次数（仅网络层，模型侧的主备切换由 agent 自己做） */
    private Integer retryCount = 1;

    /** 默认租户ID */
    private String defaultTenant = "000000";
}
