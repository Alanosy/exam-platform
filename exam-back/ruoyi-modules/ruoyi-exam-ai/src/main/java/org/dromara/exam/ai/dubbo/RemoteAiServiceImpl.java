package org.dromara.exam.ai.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.exam.ai.api.RemoteAiService;
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
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 服务对外实现
 *
 * <p>阅卷、题库、试卷等模块通过 Dubbo 调这里。
 * 接口设计上 AI 只出「建议」，写库动作全在调用方，这里不碰任何业务表。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteAiServiceImpl implements RemoteAiService {

    private final IAiService aiService;

    @Override
    public boolean enabled() {
        try {
            return aiService.enabled();
        } catch (Exception e) {
            log.warn("判断 AI 可用性失败 {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<RemoteAiModelVo> listModels() {
        try {
            return aiService.listModels();
        } catch (Exception e) {
            log.warn("查询模型清单失败 {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public RemoteMarkAiVo judgeMark(RemoteMarkAiBo bo) {
        try {
            return aiService.judgeMark(bo);
        } catch (Exception e) {
            log.warn("AI 评分失败 {}", e.getMessage());
            return RemoteMarkAiVo.fail("AI 评分异常: " + e.getMessage());
        }
    }

    @Override
    public List<RemoteMarkAiVo> judgeMarkBatch(List<RemoteMarkAiBo> bos) {
        try {
            return aiService.judgeMarkBatch(bos);
        } catch (Exception e) {
            log.warn("AI 批量评分失败 {}", e.getMessage());
            List<RemoteMarkAiVo> failed = new java.util.ArrayList<>();
            int size = bos == null ? 0 : bos.size();
            for (int i = 0; i < size; i++) {
                failed.add(RemoteMarkAiVo.fail("AI 批量评分异常: " + e.getMessage()));
            }
            return failed;
        }
    }

    @Override
    public List<RemoteQuestionGenVo> generateQuestions(RemoteQuestionGenBo bo) {
        try {
            return aiService.generateQuestions(bo);
        } catch (Exception e) {
            log.warn("AI 出题失败 {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public RemoteQuestionAuditVo auditQuestion(RemoteQuestionAuditBo bo) {
        try {
            return aiService.auditQuestion(bo);
        } catch (Exception e) {
            log.warn("AI 质检失败 {}", e.getMessage());
            return RemoteQuestionAuditVo.fail("AI 质检异常: " + e.getMessage());
        }
    }

    @Override
    public RemotePaperReviewVo reviewPaper(RemotePaperReviewBo bo) {
        try {
            return aiService.reviewPaper(bo);
        } catch (Exception e) {
            log.warn("AI 试卷审查失败 {}", e.getMessage());
            return RemotePaperReviewVo.fail("AI 试卷审查异常: " + e.getMessage());
        }
    }

    @Override
    public RemoteDiagnoseVo diagnose(RemoteDiagnoseBo bo) {
        try {
            return aiService.diagnose(bo);
        } catch (Exception e) {
            log.warn("AI 错题归因失败 {}", e.getMessage());
            return RemoteDiagnoseVo.fail("AI 错题归因异常: " + e.getMessage());
        }
    }

    @Override
    public RemoteSkillRunVo runSkill(RemoteSkillRunBo bo) {
        try {
            return aiService.runSkill(bo);
        } catch (Exception e) {
            log.warn("Skill {} 执行失败 {}", bo == null ? null : bo.getSkillCode(), e.getMessage());
            return RemoteSkillRunVo.fail("Skill 执行异常: " + e.getMessage());
        }
    }
}
