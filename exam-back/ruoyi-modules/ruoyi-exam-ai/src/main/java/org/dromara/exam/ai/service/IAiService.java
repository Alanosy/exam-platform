package org.dromara.exam.ai.service;

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

import java.util.List;

/**
 * AI 能力服务
 *
 * <p>把「业务语义」翻译成「Skill 调用」的一层：业务侧说「给这道题评分」，
 * 这里知道该调哪个 Skill、入参怎么拼、返回值怎么翻译成 Java VO。
 *
 * <p>新增一个 AI 能力只需要在这里加一个方法 + 一段映射，
 * 不用动 Dubbo 接口以外的任何东西。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public interface IAiService {

    /**
     * AI 能力是否可用
     */
    boolean enabled();

    /**
     * 模型清单（脱敏）
     */
    List<RemoteAiModelVo> listModels();

    /**
     * 主观题评分
     */
    RemoteMarkAiVo judgeMark(RemoteMarkAiBo bo);

    /**
     * 主观题批量评分
     */
    List<RemoteMarkAiVo> judgeMarkBatch(List<RemoteMarkAiBo> bos);

    /**
     * 智能出题
     */
    List<RemoteQuestionGenVo> generateQuestions(RemoteQuestionGenBo bo);

    /**
     * 试题质检
     */
    RemoteQuestionAuditVo auditQuestion(RemoteQuestionAuditBo bo);

    /**
     * 试卷审查
     */
    RemotePaperReviewVo reviewPaper(RemotePaperReviewBo bo);

    /**
     * 错题归因
     */
    RemoteDiagnoseVo diagnose(RemoteDiagnoseBo bo);

    /**
     * 通用 Skill 执行
     */
    RemoteSkillRunVo runSkill(RemoteSkillRunBo bo);
}
