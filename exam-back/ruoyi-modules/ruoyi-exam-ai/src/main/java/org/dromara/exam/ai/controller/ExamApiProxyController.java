package org.dromara.exam.ai.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.exam.ai.config.ExamApiCatalog;
import org.dromara.exam.ai.domain.vo.ApiEntryVo;
import org.dromara.system.api.RemotePermissionService;
import org.dromara.system.api.RemoteUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.dubbo.config.annotation.DubboReference;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 系统接口代理：让 Agent 能「以当前用户的身份」调用考试域的接口
 *
 * <p>这是整套 AI 能力的安全边界，规则只有四条：
 *
 * <ol>
 *   <li><b>只走网关</b>：请求带着用户自己的令牌打到网关，登录态、角色、数据权限
 *       全部由网关和下游服务的 {@code @SaCheckPermission} 判定。
 *       学生问「全年级排名」会拿不到数据 —— 因为网关不给他，不是因为 AI 拦他。
 *       换个说法：Agent 完全没有「自己的权限」，它只是用户的手。</li>
 *   <li><b>路径白名单</b>：只允许考试域业务前缀（见 {@code exam-tool.api.allow}），
 *       用户管理、租户、菜单、监控、代码生成一律不在名单里，AI 再怎么会说也调不到。</li>
 *   <li><b>写操作二次白名单</b>：非 GET 还必须命中 {@code exam-tool.api.write-allow}，
 *       并且 Agent 侧还会再弹一次确认卡（{@code confirm_write}）才真的发出去。</li>
 *   <li><b>全程留痕</b>：每一次代调都记 info 日志，谁、调了什么、返回什么码。</li>
 * </ol>
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@SaIgnore
@RestController
@RequestMapping("/api/exam-tool/api")
public class ExamApiProxyController {

    @DubboReference(check = false)
    private RemotePermissionService remotePermissionService;

    @DubboReference(check = false)
    private RemoteUserService remoteUserService;

    @Value("${exam-tool.token:}")
    private String toolToken;

    @Value("${exam-tool.gateway-url:http://127.0.0.1:8080}")
    private String gatewayUrl;

    @Value("${exam-tool.api.enabled:true}")
    private boolean apiEnabled;

    @Value("${exam-tool.api.timeout-ms:8000}")
    private int timeoutMs;

    /** 允许代调的路径前缀 */
    @Value("${exam-tool.api.allow:/exam/,/invite/,/question/,/paper/,/answer/,/mark/,/stat/,/practice/,/proctor/,/cert/,/system/dict/}")
    private List<String> allowPrefixes;

    /** 显式拒绝的前缀（优先级最高） */
    @Value("${exam-tool.api.deny:}")
    private List<String> denyPrefixes;

    /** 非 GET 请求还需要命中的前缀 */
    @Value("${exam-tool.api.write-allow:/question/create,/mark/score,/cert/record/issue,/practice/wrong/,/exam/}")
    private List<String> writeAllowPrefixes;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    /**
     * 系统能力清单：模型靠它判断「这件事系统里到底能不能做」
     */
    @PostMapping("/manifest")
    public R<Map<String, Object>> manifest(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", ExamApiCatalog.ENTRIES);
        data.put("count", ExamApiCatalog.ENTRIES.size());
        data.put("allowedPrefixes", allowPrefixes);
        data.put("writeAllowedPrefixes", writeAllowPrefixes);
        data.put("note", "调用这些接口时必须使用 /api/exam-tool/api/call，并带上用户令牌；"
            + "权限由网关按当前用户角色判定，越权会返回 401/403。");
        return R.ok(data);
    }

    /**
     * 我是谁：给 Agent 一个「当前用户能干什么」的客观事实
     *
     * <p>用它有两个好处：规划时能先过滤掉明显越权的动作，
     * 执行失败时也能把「你没这个权限」讲成人话，而不是甩一个 403。
     */
    @PostMapping("/whoami")
    public R<Map<String, Object>> whoami(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        Map<String, Object> params = body == null ? new LinkedHashMap<>() : body;
        Object rawUserId = params.get("userId");
        Long userId = toLong(rawUserId);
        Map<String, Object> data = new LinkedHashMap<>();
        if (userId == null) {
            return R.fail("userId 不能为空");
        }
        data.put("userId", String.valueOf(userId));
        try {
            String nickname = remoteUserService.selectNicknameById(userId);
            data.put("nickName", nickname == null ? "" : nickname);
        } catch (Exception e) {
            log.warn("查询用户昵称失败 userId={}: {}", userId, e.getMessage());
        }
        // 角色与权限：模型据此判断「这个用户大概能做什么」，真正的拦截在网关
        Set<String> roles = safeCall(() -> remotePermissionService.getRolePermission(userId));
        Set<String> permissions = safeCall(() -> remotePermissionService.getMenuPermission(userId));
        data.put("roles", roles == null ? List.of() : new ArrayList<>(roles));
        data.put("permissionCount", permissions == null ? 0 : permissions.size());
        // 只回传考试域权限，避免把上百个系统权限码塞进模型上下文
        Set<String> examPerms = new TreeSet<>();
        if (permissions != null) {
            for (String p : permissions) {
                if (p != null && (p.startsWith("exam:") || p.startsWith("system:exam:"))) {
                    examPerms.add(p);
                }
            }
        }
        data.put("examPermissions", new ArrayList<>(examPerms));
        return R.ok(data);
    }

    /**
     * 代调一个系统接口
     *
     * <p>入参：{ method, path, query?, body?, token, clientId }
     * 其中 token / clientId 是当前登录用户的，Java 侧原样带上去，网关负责鉴权。
     */
    @PostMapping("/call")
    public R<Map<String, Object>> call(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        if (!apiEnabled) {
            return R.fail("接口代理已关闭（exam-tool.api.enabled=false）");
        }
        if (body == null) {
            return R.fail("请求体不能为空");
        }

        String method = str(body.get("method"), "GET").toUpperCase();
        String path = str(body.get("path"), "");
        String userToken = str(body.get("token"), "");
        String clientId = str(body.get("clientId"), "");
        String userId = str(body.get("userId"), "");

        if (path.isEmpty() || !path.startsWith("/")) {
            return R.fail("path 非法");
        }
        if (!List.of("GET", "POST", "PUT", "DELETE").contains(method)) {
            return R.fail("不支持的请求方法：" + method);
        }
        if (userToken.isEmpty()) {
            // 没有用户令牌 = 无法判定身份与权限，一律拒绝，绝不退化成「系统身份」
            return R.fail(403, "缺少用户令牌，无法判定权限");
        }

        String deny = hitPrefix(path, denyPrefixes);
        if (deny != null) {
            log.warn("AI 代调被拒绝（deny 命中）userId={} {} {}", userId, method, path);
            return R.fail(403, "该接口不允许 AI 代调：" + path);
        }
        if (hitPrefix(path, allowPrefixes) == null) {
            log.warn("AI 代调被拒绝（不在白名单）userId={} {} {}", userId, method, path);
            return R.fail(403, "该接口不在 AI 可调用范围内：" + path);
        }
        if (!"GET".equals(method) && hitPrefix(path, writeAllowPrefixes) == null) {
            log.warn("AI 代调被拒绝（写操作未授权）userId={} {} {}", userId, method, path);
            return R.fail(403, "写操作只允许在授权范围内：" + path);
        }

        String url = buildUrl(path, body.get("query"));
        String payload = null;
        if (!"GET".equals(method) && body.get("body") != null) {
            payload = toJson(body.get("body"));
        }

        long start = System.currentTimeMillis();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Authorization", userToken)
                .header("Accept", "application/json");
            if (!clientId.isEmpty()) {
                // 网关会校验 clientid 与令牌里的一致，缺了会被判「客户端ID与Token不匹配」
                builder.header("clientid", clientId);
            }
            if (payload == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
            }
            HttpResponse<String> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            String respBody = resp.body();
            log.info("AI 代调 userId={} {} {} -> {} ({}ms)", userId, method, path, status, System.currentTimeMillis() - start);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", status);
            data.put("ok", status >= 200 && status < 300);
            data.put("path", path);
            data.put("method", method);
            Object parsed = tryParse(respBody);
            if (parsed instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) map;
                data.put("code", typed.get("code"));
                data.put("msg", typed.get("msg"));
                data.put("data", typed.get("data"));
                data.put("rows", typed.get("rows"));
                data.put("total", typed.get("total"));
            } else {
                data.put("raw", respBody == null ? "" : clip(respBody, 2000));
            }
            return R.ok(data);
        } catch (Exception e) {
            log.warn("AI 代调失败 userId={} {} {}: {}", userId, method, path, e.getMessage());
            return R.fail("代调失败：" + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- 内部

    private boolean checkToken(String token) {
        return toolToken == null || toolToken.isEmpty() || toolToken.equals(token);
    }

    private String hitPrefix(String path, List<String> prefixes) {
        if (prefixes == null) {
            return null;
        }
        for (String prefix : prefixes) {
            String p = prefix == null ? "" : prefix.trim();
            if (!p.isEmpty() && path.startsWith(p)) {
                return p;
            }
        }
        return null;
    }

    private String buildUrl(String path, Object query) {
        String base = gatewayUrl == null ? "http://127.0.0.1:8080" : gatewayUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String url = base + path;
        if (!(query instanceof Map<?, ?> map) || map.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        sb.append(path.contains("?") ? '&' : '?');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            if (!first) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(String.valueOf(entry.getKey()), StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }

    /**
     * 极简 JSON 序列化：只用来把 Agent 传来的 Map 原样转发，
     * 不引第三方也不碰业务对象，避免序列化出意外的字段。
     */
    private String toJson(Object value) {
        if (value instanceof String s) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        writeJson(sb, value);
        return sb.toString();
    }

    private void writeJson(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String s) {
            sb.append('"').append(s.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                sb.append('"').append(String.valueOf(e.getKey())).append("\":");
                writeJson(sb, e.getValue());
                first = false;
            }
            sb.append('}');
        } else if (value instanceof Iterable<?> it) {
            sb.append('[');
            boolean first = true;
            for (Object item : it) {
                if (!first) {
                    sb.append(',');
                }
                writeJson(sb, item);
                first = false;
            }
            sb.append(']');
        } else {
            sb.append('"').append(String.valueOf(value).replace("\"", "\\\"")).append('"');
        }
    }

    private Object tryParse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String s = text.trim();
        if (!s.startsWith("{") && !s.startsWith("[")) {
            return null;
        }
        // 用 Jackson 会多一层依赖绑定，这里只需要把 R<T> 的几个字段透出，
        // 交给调用方自己判断即可；解析失败就回原文。
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(s, Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Set<String> safeCall(java.util.function.Supplier<Set<String>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("远程查询失败: {}", e.getMessage());
            return null;
        }
    }

    private String clip(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }

    private String str(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }

    private Long toLong(Object value) {
        if (value == null || "".equals(value)) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 清单里是否存在该能力（供 Agent 侧做静态校验） */
    static ApiEntryVo findEntry(String code) {
        for (ApiEntryVo entry : ExamApiCatalog.ENTRIES) {
            if (entry.getCode().equals(code)) {
                return entry;
            }
        }
        return null;
    }
}
