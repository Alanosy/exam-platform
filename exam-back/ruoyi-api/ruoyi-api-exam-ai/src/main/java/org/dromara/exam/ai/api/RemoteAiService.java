package org.dromara.exam.ai.api;

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
 * AI 服务（跨服务调用）
 *
 * <p>本接口是 Java 业务侧使用 AI 能力的**唯一入口**。底层模型编排跑在 Python 侧
 * （ruoyi-agent / ruoyi-exam-agent，注册到 Nacos 的 ruoyi-exam-agent），
 * 由 ruoyi-exam-ai 模块负责 HTTP 转发。业务模块不需要知道模型在哪、用的哪个供应商。
 *
 * <p>设计约束（踩过的坑，别改）：
 * <ol>
 *   <li>所有方法**不允许抛异常到调用方**。AI 是旁路能力，模型挂了不能连累主流程
 *       （阅卷、出题照常能用人工方式完成）。失败统一体现在返回值的 success / message 上。</li>
 *   <li>返回的永远是「建议」而不是「决定」。给分、改题、发布考试这些动作
 *       由业务侧决定是否采纳，AI 侧不做任何写库操作。</li>
 *   <li>{@link #enabled()} 决定前端要不要显示 AI 按钮，不要在页面里写死。</li>
 * </ol>
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public interface RemoteAiService {

    /**
     * AI 能力是否可用
     *
     * <p>不可用有两种：没启用（配置关了）、启用了但连不上 agent 服务。
     * 前端按这个值决定要不要渲染 AI 入口。
     *
     * @return true 表示可用
     */
    boolean enabled();

    /**
     * 模型清单（脱敏，不含密钥）
     *
     * @return 模型列表，agent 不可用时返回空列表
     */
    List<RemoteAiModelVo> listModels();

    /**
     * 主观题 AI 评分：只给建议分与理由
     *
     * <p>调用方是阅卷服务。评分结果**不会**直接成为最终得分，
     * 需要教师确认后才写库，这一步在阅卷服务内部完成。
     *
     * @param bo 判分材料（题干 / 参考答案 / 解析 / 作答 / 满分）
     * @return 建议分与理由，失败时 success=false 且带 message
     */
    RemoteMarkAiVo judgeMark(RemoteMarkAiBo bo);

    /**
     * 主观题 AI 批量评分
     *
     * <p>整场考试的主观题预评走这个：一次提交多道题，由 agent 侧并发处理，
     * 单条失败不影响其它（对应题的 success=false）。
     * 逐题调用 {@link #judgeMark} 在 200 份卷 × 5 主观题时会串行等待上千次，
     * 这个量级必须用批量。
     *
     * @param bos 判分材料，按此顺序返回结果
     * @return 与入参等长同序的结果列表；整体失败时返回空列表
     */
    List<RemoteMarkAiVo> judgeMarkBatch(List<RemoteMarkAiBo> bos);

    /**
     * 智能出题
     *
     * @param bo 出题条件（知识点 / 难度 / 题型 / 数量）
     * @return 生成的题目，失败时返回空列表
     */
    List<RemoteQuestionGenVo> generateQuestions(RemoteQuestionGenBo bo);

    /**
     * 试题质检：挑毛病，不是夸奖
     *
     * @param bo 待审查题目
     * @return 问题清单与质量分，失败时 success=false
     */
    RemoteQuestionAuditVo auditQuestion(RemoteQuestionAuditBo bo);

    /**
     * 试卷审查：知识点覆盖 / 难度分布 / 重复度 / 预计用时
     *
     * @param bo 试卷与题目清单
     * @return 审查结论，失败时 success=false
     */
    RemotePaperReviewVo reviewPaper(RemotePaperReviewBo bo);

    /**
     * 错题归因：定位薄弱知识点与错误类型
     *
     * @param bo 错题记录
     * @return 诊断结论，失败时 success=false
     */
    RemoteDiagnoseVo diagnose(RemoteDiagnoseBo bo);

    /**
     * 通用 Skill 执行入口
     *
     * <p>给后续新增能力留的口子：新 Skill 只在 Python 侧加提示词 + 注册，
     * Java 侧不用改接口就能调。业务代码优先用上面的语义化方法，
     * 这个方法只用于「临时调一个新能力」和运维自测。
     *
     * @param bo Skill 编码与入参
     * @return Skill 执行结果
     */
    RemoteSkillRunVo runSkill(RemoteSkillRunBo bo);
}
