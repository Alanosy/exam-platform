package org.dromara.exam.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.exam.ai.domain.bo.AiChatBo;
import org.dromara.exam.ai.domain.vo.AiChatSessionVo;
import org.dromara.exam.ai.domain.vo.AiChatVo;
import org.dromara.exam.ai.service.IChatService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对话式 AI 助手
 *
 * <p>前端悬浮聊天窗走这里。与 {@link AiController} 里那些「一次性 AI 能力接口」
 * （出题 / 阅卷 / 审查）的区别：这里是有会话状态的 Agent——
 * 能中断问人、能连续调多个工具、能展示运行流程。
 *
 * <p>权限只卡登录：考生也该能用它问「这场考试怎么考」，
 * 真正的写操作由 Agent 侧的确认卡兜底，越权的业务调用会被网关挡回来。
 *
 * <p>路由写 {@code {"/chat", "/ai/chat"}} 是因为网关可能按服务名 StripPrefix，
 * 也可能直接透传，两种都要能匹配上。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"/chat", "/ai/chat"})
public class ChatController {

    private final IChatService chatService;

    /**
     * 跑一轮对话
     */
    @SaCheckLogin
    @Log(title = "AI 对话", businessType = BusinessType.OTHER)
    @PostMapping("")
    public R<AiChatVo> chat(@RequestBody(required = false) AiChatBo bo) {
        return R.ok(chatService.chat(bo));
    }

    /**
     * 历史会话列表（只看得到自己的）
     */
    @SaCheckLogin
    @GetMapping("/sessions")
    public R<List<AiChatSessionVo>> sessions() {
        return R.ok(chatService.listSessions());
    }

    /**
     * 会话详情：点历史会话继续聊
     */
    @SaCheckLogin
    @GetMapping("/session/{sessionId}")
    public R<AiChatSessionVo> session(@PathVariable("sessionId") String sessionId) {
        return R.ok(chatService.getSession(sessionId));
    }
}
