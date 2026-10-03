package org.dromara.exam.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.ai.api.domain.RemoteAiModelVo;
import org.dromara.exam.ai.api.domain.RemoteDiagnoseBo;
import org.dromara.exam.ai.api.domain.RemoteDiagnoseVo;
import org.dromara.exam.ai.api.domain.RemoteMarkAiBo;
import org.dromara.exam.ai.api.domain.RemoteMarkAiVo;
import org.dromara.exam.ai.api.domain.RemotePaperReviewBo;
import org.dromara.exam.ai.api.domain.RemotePaperReviewVo;
import org.dromara.exam.ai.api.domain.RemoteQuestionAuditBo;
import org.dromara.exam.ai.api.domain.RemoteQuestionAuditVo;
import org.dromara.exam.ai.api.domain.RemoteQuestionGenBo;
import org.dromara.exam.ai.api.domain.RemoteQuestionGenVo;
import org.dromara.exam.ai.api.domain.RemoteSkillRunBo;
import org.dromara.exam.ai.api.domain.RemoteSkillRunVo;
import org.dromara.exam.ai.service.IAiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI 能力接口
 *
 * <p>前端直接调用走这里，跨服务调用走 Dubbo（RemoteAiService）。
 * 两者共用 IAiService，不存在两份逻辑。
 *
 * <p>路由写 {@code {"", "/ai"}} 是因为网关可能按服务名 StripPrefix，
 * 也可能直接透传，两种都要能匹配上。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"", "/ai"})
public class AiController extends BaseController {

    private final IAiService aiService;

    /**
     * AI 能力是否可用：前端按这个值决定要不要渲染 AI 入口
     *
     * <p><b>必须包成对象返回，不能返回裸布尔。</b>前端 {@code AiEnabledVO} 是
     * {@code {enabled: boolean}}，读到的是 {@code data.enabled}；直接返回
     * {@code R<Boolean>} 时 {@code data} 是 true/false，取 {@code .enabled}
     * 恒为 undefined，页面就会一直显示「AI 服务不可用」。
     */
    @SaCheckLogin
    @GetMapping("/enabled")
    public R<Map<String, Object>> enabled() {
        return R.ok(Map.of("enabled", aiService.enabled()));
    }

    /**
     * 模型清单（脱敏，不含密钥）
     */
    @SaCheckPermission("exam:ai:list")
    @GetMapping("/model/list")
    public R<List<RemoteAiModelVo>> modelList() {
        return R.ok(aiService.listModels());
    }

    /**
     * 主观题 AI 评分：只给建议分，不写库
     */
    @SaCheckPermission("exam:ai:edit")
    @Log(title = "AI 评分", businessType = BusinessType.OTHER)
    @PostMapping("/mark/score")
    public R<RemoteMarkAiVo> markScore(@RequestBody RemoteMarkAiBo bo) {
        fillTenant(bo);
        return R.ok(aiService.judgeMark(bo));
    }

    /**
     * 主观题 AI 批量评分
     */
    @SaCheckPermission("exam:ai:edit")
    @Log(title = "AI 批量评分", businessType = BusinessType.OTHER)
    @PostMapping("/mark/batch")
    public R<List<RemoteMarkAiVo>> markBatch(@RequestBody List<RemoteMarkAiBo> bos) {
        if (bos != null) {
            bos.forEach(this::fillTenant);
        }
        return R.ok(aiService.judgeMarkBatch(bos));
    }

    /**
     * 智能出题
     */
    @SaCheckPermission("exam:ai:edit")
    @Log(title = "AI 出题", businessType = BusinessType.OTHER)
    @PostMapping("/question/generate")
    public R<List<RemoteQuestionGenVo>> generate(@RequestBody RemoteQuestionGenBo bo) {
        if (bo != null && bo.getTenantId() == null) {
            bo.setTenantId(TenantHelper.getTenantId());
        }
        return R.ok(aiService.generateQuestions(bo));
    }

    /**
     * 试题质检
     */
    @SaCheckPermission("exam:ai:edit")
    @Log(title = "AI 试题质检", businessType = BusinessType.OTHER)
    @PostMapping("/question/audit")
    public R<RemoteQuestionAuditVo> audit(@RequestBody RemoteQuestionAuditBo bo) {
        if (bo != null && bo.getTenantId() == null) {
            bo.setTenantId(TenantHelper.getTenantId());
        }
        return R.ok(aiService.auditQuestion(bo));
    }

    /**
     * 试卷审查
     */
    @SaCheckPermission("exam:ai:edit")
    @Log(title = "AI 试卷审查", businessType = BusinessType.OTHER)
    @PostMapping("/paper/review")
    public R<RemotePaperReviewVo> review(@RequestBody RemotePaperReviewBo bo) {
        if (bo != null && bo.getTenantId() == null) {
            bo.setTenantId(TenantHelper.getTenantId());
        }
        return R.ok(aiService.reviewPaper(bo));
    }

    /**
     * 错题归因
     */
    @SaCheckLogin
    @Log(title = "AI 错题归因", businessType = BusinessType.OTHER)
    @PostMapping("/learn/diagnose")
    public R<RemoteDiagnoseVo> diagnose(@RequestBody RemoteDiagnoseBo bo) {
        if (bo != null && bo.getTenantId() == null) {
            bo.setTenantId(TenantHelper.getTenantId());
        }
        return R.ok(aiService.diagnose(bo));
    }

    /**
     * 通用 Skill 执行（运维自测与临时调用新能力用）
     */
    @SaCheckPermission("exam:ai:list")
    @PostMapping("/skill/run")
    public R<RemoteSkillRunVo> runSkill(@RequestBody RemoteSkillRunBo bo) {
        if (bo != null && bo.getTenantId() == null) {
            bo.setTenantId(TenantHelper.getTenantId());
        }
        return R.ok(aiService.runSkill(bo));
    }

    private void fillTenant(RemoteMarkAiBo bo) {
        if (bo != null && bo.getTenantId() == null) {
            bo.setTenantId(TenantHelper.getTenantId());
        }
    }
}
