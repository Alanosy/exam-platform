package org.dromara.exam.ai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.ai.client.AiAgentClient;
import org.dromara.exam.ai.config.ExamApiCatalog;
import org.dromara.exam.ai.domain.bo.AiChatBo;
import org.dromara.exam.ai.domain.vo.AiChatSessionVo;
import org.dromara.exam.ai.domain.vo.AiChatVo;
import org.dromara.exam.ai.service.IChatService;
import org.dromara.system.api.model.LoginUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 对话式 Agent 实现
 *
 * <p>Java 侧只做四件事：补**身份**、补租户、转发、把 agent 的响应翻成前端 VO。
 * 编排逻辑全在 Python 侧，Java 不复制一份。
 *
 * <p><b>身份为什么要由这里补</b>：Agent 之后要用「当前用户」的身份去调业务接口，
 * 令牌与 clientid 都不能由前端自己上报（那是可伪造的），必须从登录态里取出来再下发。
 * 权限判定依旧发生在网关与业务服务，Agent 只是代劳的那一双手。
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
        // 提交中断卡时前端只回 answers，message 会是 null；
        // agent 侧字段是 str，传 null 会被 pydantic 打成 422
        body.put("message", payload.getMessage() == null ? "" : payload.getMessage());
        body.put("answers", payload.getAnswers());
        // 租户与操作者必须由服务端补：Agent 按租户隔离题库与考试，不能信客户端传值
        body.put("tenant_id", TenantHelper.getTenantId());
        body.put("user_id", LoginHelper.getUserId());
        body.put("model_code", payload.getModelCode());
        // 用户身份：Agent 代调业务接口时原样带上，权限交给网关判
        body.put("token", StpUtil.getTokenValue());
        body.put("client_id", LoginHelper.getLoginUser() == null ? "" : LoginHelper.getLoginUser().getClientKey());
        body.put("identity", buildIdentity());
        if (payload.getOptions() != null && !payload.getOptions().isEmpty()) {
            body.put("options", payload.getOptions());
        }

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

    @Override
    public List<AiChatSessionVo> listSessions() {
        String tenantId = TenantHelper.getTenantId();
        String userId = String.valueOf(LoginHelper.getUserId());
        AiAgentClient.AgentResp resp = agentClient.get(
            "/chat/sessions?tenant_id=" + tenantId + "&user_id=" + userId + "&limit=30");
        if (resp.getCode() != 200 || resp.getData() == null) {
            log.warn("拉取历史会话失败 code={} msg={}", resp.getCode(), resp.getMsg());
            return List.of();
        }
        List<AiChatSessionVo> list = new ArrayList<>();
        JsonNode items = resp.getData().path("items");
        if (items.isArray()) {
            for (JsonNode item : items) {
                AiChatSessionVo vo = new AiChatSessionVo();
                vo.setId(item.path("id").asText(""));
                vo.setTitle(item.path("title").asText("（新会话）"));
                vo.setIntent(item.path("intent").asText(""));
                vo.setMessageCount(item.path("messageCount").asInt(0));
                vo.setUpdatedAt(item.path("updatedAt").asLong(0));
                list.add(vo);
            }
        }
        return list;
    }

    @Override
    public AiChatSessionVo getSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ServiceException("会话ID不能为空");
        }
        AiAgentClient.AgentResp resp = agentClient.get("/chat/session/" + sessionId.trim());
        if (resp.getCode() != 200 || resp.getData() == null || !resp.getData().path("exists").asBoolean(false)) {
            return null;
        }
        JsonNode data = resp.getData();
        AiChatSessionVo vo = new AiChatSessionVo();
        vo.setId(data.path("id").asText(sessionId));
        vo.setTitle(data.path("title").asText(""));
        vo.setIntent(data.path("intent").asText(""));
        vo.setMessageCount(data.path("messages").size());
        vo.setUpdatedAt(data.path("updatedAt").asLong(0));
        List<Map<String, Object>> messages = new ArrayList<>();
        for (JsonNode msg : data.path("messages")) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("role", msg.path("role").asText(""));
            row.put("content", msg.path("content").asText(""));
            messages.add(row);
        }
        vo.setMessages(messages);
        return vo;
    }

    @Override
    public Map<String, Object> whoami() {
        Map<String, Object> data = new LinkedHashMap<>(buildIdentity());
        LoginUser user = LoginHelper.getLoginUser();
        Set<String> permissions = user == null || user.getMenuPermission() == null
            ? Set.of() : user.getMenuPermission();
        final boolean staff = isStaff(permissions);
        // 可见能力数：AI 规划时实际能看到多少条接口，比权限码列表直观得多
        long visible = ExamApiCatalog.ENTRIES.stream()
            .filter(e -> {
                String perm = e.getPermission() == null ? "" : e.getPermission().trim();
                if (!perm.isEmpty() && !permissions.contains(perm) && !permissions.contains("*:*:*")) {
                    return false;
                }
                return !"admin".equals(e.getAudience()) || staff;
            })
            .count();
        data.put("visibleApiCount", visible);
        data.put("totalApiCount", ExamApiCatalog.ENTRIES.size());
        data.put("staff", staff);
        return data;
    }

    /**
     * 当前用户的身份快照：角色 + 考试域权限
     *
     * <p>只给「考试域」权限码，系统里上百个权限全塞进模型上下文既浪费又容易被误读；
     * 角色是给人看的，权限码是给规划器判断可行性的。
     */
    private Map<String, Object> buildIdentity() {
        Map<String, Object> identity = new LinkedHashMap<>();
        LoginUser user = LoginHelper.getLoginUser();
        if (user == null) {
            return identity;
        }
        identity.put("userId", String.valueOf(user.getUserId()));
        identity.put("userName", user.getUsername());
        identity.put("nickName", user.getNickname());
        identity.put("isSuperAdmin", LoginHelper.isSuperAdmin());
        Set<String> roles = user.getRolePermission();
        identity.put("roles", roles == null ? List.of() : new ArrayList<>(roles));
        Set<String> examPerms = new TreeSet<>();
        if (user.getMenuPermission() != null) {
            for (String p : user.getMenuPermission()) {
                if (p != null && (p.startsWith("exam:") || p.startsWith("system:exam:"))) {
                    examPerms.add(p);
                }
            }
        }
        identity.put("examPermissions", new ArrayList<>(examPerms));
        identity.put("roleScope", roleScope(roles, examPerms));
        identity.put("staff", !examPerms.isEmpty() || LoginHelper.isSuperAdmin());
        identity.put("note", "没有对应权限码的动作不要规划：即使发出去，网关也会返回 403。");
        return identity;
    }

    /** 是否属于管理 / 教师侧：有任一考试域权限即视为工作人员 */
    private boolean isStaff(Set<String> permissions) {
        for (String p : permissions) {
            if (p != null && (p.startsWith("exam:") || p.startsWith("system:exam:") || "*:*:*".equals(p))) {
                return true;
            }
        }
        return false;
    }

    /** 角色归类：把各种角色标识收敛成 admin / teacher / student 三类，给规划器和设置面板用 */
    private String roleScope(Set<String> roles, Set<String> examPerms) {
        if (LoginHelper.isSuperAdmin()) {
            return "admin";
        }
        if (examPerms != null && !examPerms.isEmpty()) {
            return "teacher";
        }
        if (roles != null) {
            for (String role : roles) {
                String r = role == null ? "" : role.toLowerCase();
                if (r.contains("student") || r.contains("考生") || r.contains("学员")) {
                    return "student";
                }
            }
        }
        return "unknown";
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
