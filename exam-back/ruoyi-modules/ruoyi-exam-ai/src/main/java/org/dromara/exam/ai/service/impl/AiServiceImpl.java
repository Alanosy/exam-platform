package org.dromara.exam.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.exam.ai.api.domain.RemoteAiIssueVo;
import org.dromara.exam.ai.api.domain.RemoteAiKnowledgeCoverageVo;
import org.dromara.exam.ai.api.domain.RemoteAiMatchPointVo;
import org.dromara.exam.ai.api.domain.RemoteAiModelVo;
import org.dromara.exam.ai.api.domain.RemoteAiOptionVo;
import org.dromara.exam.ai.api.domain.RemoteAiWeakPointVo;
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
import org.dromara.exam.ai.client.AiAgentClient;
import org.dromara.exam.ai.client.AiJson;
import org.dromara.exam.ai.config.AiAgentProperties;
import org.dromara.exam.ai.service.IAiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 能力实现：把业务语义翻译成 Skill 调用
 *
 * <p>每个方法做三件事：拼 input → 调 agent → 翻译返回值。
 * 全程不抛异常，失败一律转成 success=false + message，
 * 因为 AI 挂了不应该让阅卷页打不开、让题库页刷不出来。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements IAiService {

    /** Skill 编码，与 agent 侧 prompts/*.yaml 的 code 一一对应 */
    private static final String SKILL_MARK_SCORE = "mark_score";
    private static final String SKILL_QUESTION_GEN = "question_gen";
    private static final String SKILL_QUESTION_AUDIT = "question_audit";
    private static final String SKILL_PAPER_REVIEW = "paper_review";
    private static final String SKILL_DIAGNOSIS = "diagnosis";

    /** 可用状态缓存时长：避免每次点按钮都去 ping 一次 agent */
    private static final long PING_CACHE_MS = 10_000L;

    private final AiAgentClient client;
    private final AiAgentProperties properties;
    private final ObjectMapper objectMapper;

    private volatile Boolean pingOk;
    private volatile long pingAt;

    @Override
    public boolean enabled() {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (pingOk != null && now - pingAt < PING_CACHE_MS) {
            return pingOk;
        }
        boolean ok = client.ping();
        pingOk = ok;
        pingAt = now;
        return ok;
    }

    @Override
    public List<RemoteAiModelVo> listModels() {
        AiAgentClient.AgentResp resp = client.get("/model/list?tenant_id=" + tenant());
        if (resp.getCode() != 200) {
            log.warn("查询模型清单失败: {}", resp.getMsg());
            return List.of();
        }
        String primary = AiJson.str(resp.getData(), "primary");
        List<RemoteAiModelVo> out = new ArrayList<>();
        for (JsonNode node : AiJson.list(resp.getData(), "models")) {
            RemoteAiModelVo vo = new RemoteAiModelVo();
            vo.setCode(AiJson.str(node, "code"));
            vo.setName(AiJson.str(node, "name"));
            vo.setType(AiJson.str(node, "type"));
            vo.setSource(AiJson.str(node, "source"));
            vo.setPriority(AiJson.i(node, "priority"));
            vo.setState(AiJson.str(node, "state"));
            vo.setPrimary(StringUtils.isNotBlank(primary) && primary.equals(vo.getName()));
            out.add(vo);
        }
        return out;
    }

    @Override
    public RemoteMarkAiVo judgeMark(RemoteMarkAiBo bo) {
        if (bo == null) {
            return RemoteMarkAiVo.fail("判分材料为空");
        }
        if (!enabled()) {
            return RemoteMarkAiVo.fail("AI 能力不可用");
        }
        AiAgentClient.AgentResp resp = client.post(
            "/skill/" + SKILL_MARK_SCORE + "/run", skillBody(toMarkInput(bo), bo.getTenantId(), null));
        if (resp.getCode() != 200) {
            return RemoteMarkAiVo.fail(resp.getMsg());
        }
        return toMarkVo(resp.getData());
    }

    @Override
    public List<RemoteMarkAiVo> judgeMarkBatch(List<RemoteMarkAiBo> bos) {
        if (bos == null || bos.isEmpty()) {
            return List.of();
        }
        if (!enabled()) {
            // 整体不可用时按条数补失败结果，保证调用方拿到的列表长度一致
            List<RemoteMarkAiVo> failed = new ArrayList<>(bos.size());
            for (int i = 0; i < bos.size(); i++) {
                failed.add(RemoteMarkAiVo.fail("AI 能力不可用"));
            }
            return failed;
        }
        List<Map<String, Object>> items = new ArrayList<>(bos.size());
        for (RemoteMarkAiBo bo : bos) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("input", toMarkInput(bo));
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tenant_id", StringUtils.defaultIfBlank(bos.get(0).getTenantId(), tenant()));
        body.put("items", items);
        body.put("max_parallel", properties.getBatchParallel());
        body.put("skip_error", true);

        AiAgentClient.AgentResp resp = client.post("/skill/" + SKILL_MARK_SCORE + "/batch", body);
        if (resp.getCode() != 200) {
            log.warn("AI 批量评分失败: {}", resp.getMsg());
            List<RemoteMarkAiVo> failed = new ArrayList<>(bos.size());
            for (int i = 0; i < bos.size(); i++) {
                failed.add(RemoteMarkAiVo.fail(resp.getMsg()));
            }
            return failed;
        }

        // agent 并发执行，返回顺序不保证，按 index 还原
        List<JsonNode> results = new ArrayList<>(AiJson.list(resp.getData(), "results"));
        results.sort(Comparator.comparingInt(n -> AiJson.i(n, "index", 0)));

        List<RemoteMarkAiVo> out = new ArrayList<>(bos.size());
        for (int i = 0; i < bos.size(); i++) {
            if (i >= results.size()) {
                out.add(RemoteMarkAiVo.fail("AI 未返回该条结果"));
                continue;
            }
            JsonNode node = results.get(i);
            if (!Boolean.TRUE.equals(AiJson.bool(node, "ok", false))) {
                out.add(RemoteMarkAiVo.fail(AiJson.str(node, "error", "评分失败")));
                continue;
            }
            RemoteMarkAiVo vo = toMarkVo(node);
            if (node.hasNonNull("model")) {
                vo.setModel(AiJson.str(node, "model"));
            }
            out.add(vo);
        }
        return out;
    }

    @Override
    public List<RemoteQuestionGenVo> generateQuestions(RemoteQuestionGenBo bo) {
        if (bo == null) {
            return List.of();
        }
        if (!enabled()) {
            return List.of();
        }
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("question_type", StringUtils.defaultIfBlank(bo.getQuestionType(), "SINGLE"));
        input.put("difficulty", StringUtils.defaultIfBlank(bo.getDifficulty(), "medium"));
        input.put("knowledge_points", bo.getKnowledgePoints() == null ? List.of() : bo.getKnowledgePoints());
        input.put("count", bo.getCount() == null ? 5 : bo.getCount());
        input.put("score", bo.getScore() == null ? 5 : bo.getScore());
        input.put("rag_context", StringUtils.defaultIfBlank(bo.getRagContext(), ""));
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("requirement", StringUtils.defaultIfBlank(bo.getExtra(), ""));
        input.put("extra", extra);

        AiAgentClient.AgentResp resp = client.post("/skill/" + SKILL_QUESTION_GEN + "/run",
            skillBody(input, bo.getTenantId(), null));
        if (resp.getCode() != 200) {
            log.warn("AI 出题失败: {}", resp.getMsg());
            return List.of();
        }
        List<RemoteQuestionGenVo> out = new ArrayList<>();
        for (JsonNode node : AiJson.list(resp.getData(), "data")) {
            RemoteQuestionGenVo vo = new RemoteQuestionGenVo();
            vo.setQuestionType(AiJson.str(node, "question_type"));
            vo.setStem(AiJson.str(node, "stem"));
            vo.setAnswer(AiJson.str(node, "answer"));
            vo.setAnalysis(AiJson.str(node, "analysis"));
            vo.setKnowledgePoints(AiJson.strList(node, "knowledge_points"));
            vo.setDifficulty(AiJson.str(node, "difficulty"));
            vo.setScore(AiJson.bd(node, "score"));
            List<RemoteAiOptionVo> options = new ArrayList<>();
            for (JsonNode opt : AiJson.list(node, "options")) {
                RemoteAiOptionVo optionVo = new RemoteAiOptionVo();
                optionVo.setKey(AiJson.str(opt, "key"));
                optionVo.setContent(AiJson.str(opt, "content"));
                options.add(optionVo);
            }
            vo.setOptions(options);
            out.add(vo);
        }
        return out;
    }

    @Override
    public RemoteQuestionAuditVo auditQuestion(RemoteQuestionAuditBo bo) {
        if (bo == null) {
            return RemoteQuestionAuditVo.fail("待审题目为空");
        }
        if (!enabled()) {
            return RemoteQuestionAuditVo.fail("AI 能力不可用");
        }
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("question_type", StringUtils.defaultIfBlank(bo.getQuestionType(), ""));
        input.put("difficulty", StringUtils.defaultIfBlank(bo.getDifficulty(), ""));
        input.put("knowledge_points", bo.getKnowledgePoints() == null ? List.of() : bo.getKnowledgePoints());
        input.put("stem", bo.getStem());
        input.put("options", toOptionMaps(bo.getOptions()));
        input.put("answer", bo.getAnswer());
        input.put("analysis", bo.getAnalysis());
        input.put("focus", StringUtils.defaultIfBlank(bo.getFocus(), "全面审查"));

        AiAgentClient.AgentResp resp = client.post("/skill/" + SKILL_QUESTION_AUDIT + "/run",
            skillBody(input, bo.getTenantId(), null));
        if (resp.getCode() != 200) {
            return RemoteQuestionAuditVo.fail(resp.getMsg());
        }
        JsonNode data = resp.getData().path("data");
        RemoteQuestionAuditVo vo = new RemoteQuestionAuditVo();
        vo.setSuccess(true);
        vo.setPassed(AiJson.bool(data, "passed", false));
        vo.setQualityScore(AiJson.bd(data, "quality_score"));
        vo.setSummary(AiJson.str(data, "summary"));
        vo.setModel(AiJson.str(resp.getData(), "model"));
        vo.setIssues(toIssues(data));
        return vo;
    }

    @Override
    public RemotePaperReviewVo reviewPaper(RemotePaperReviewBo bo) {
        if (bo == null) {
            return RemotePaperReviewVo.fail("待审试卷为空");
        }
        if (!enabled()) {
            return RemotePaperReviewVo.fail("AI 能力不可用");
        }
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("title", StringUtils.defaultIfBlank(bo.getTitle(), "未命名试卷"));
        input.put("duration", bo.getDuration() == null ? 60 : bo.getDuration());
        input.put("pass_score", bo.getPassScore() == null ? 60 : bo.getPassScore());
        input.put("focus", StringUtils.defaultIfBlank(bo.getFocus(), "全面审查"));
        List<Map<String, Object>> questions = new ArrayList<>();
        if (bo.getQuestions() != null) {
            for (org.dromara.exam.ai.api.domain.RemoteAiPaperQuestionVo q : bo.getQuestions()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("question_id", q.getQuestionId());
                map.put("question_type", q.getQuestionType());
                map.put("stem", q.getStem());
                map.put("difficulty", q.getDifficulty());
                map.put("knowledge_points", q.getKnowledgePoints() == null ? List.of() : q.getKnowledgePoints());
                map.put("score", q.getScore());
                questions.add(map);
            }
        }
        input.put("questions", questions);

        AiAgentClient.AgentResp resp = client.post("/skill/" + SKILL_PAPER_REVIEW + "/run",
            skillBody(input, bo.getTenantId(), null));
        if (resp.getCode() != 200) {
            return RemotePaperReviewVo.fail(resp.getMsg());
        }
        JsonNode data = resp.getData().path("data");
        RemotePaperReviewVo vo = new RemotePaperReviewVo();
        vo.setSuccess(true);
        vo.setQuestionCount(AiJson.i(data, "question_count"));
        vo.setEstimatedMinutes(AiJson.i(data, "estimated_minutes"));
        vo.setVerdict(AiJson.str(data, "verdict"));
        vo.setModel(AiJson.str(resp.getData(), "model"));
        JsonNode dist = AiJson.obj(data, "difficulty_distribution");
        if (dist != null) {
            try {
                vo.setDifficultyDistribution(objectMapper.writeValueAsString(dist));
            } catch (Exception e) {
                vo.setDifficultyDistribution(dist.toString());
            }
        }
        List<RemoteAiKnowledgeCoverageVo> coverage = new ArrayList<>();
        for (JsonNode node : AiJson.list(data, "knowledge_coverage")) {
            RemoteAiKnowledgeCoverageVo item = new RemoteAiKnowledgeCoverageVo();
            item.setPoint(AiJson.str(node, "point"));
            item.setCount(AiJson.i(node, "count"));
            item.setRatio(AiJson.bd(node, "ratio"));
            coverage.add(item);
        }
        vo.setKnowledgeCoverage(coverage);
        vo.setIssues(toIssues(data));
        vo.setSuggestions(AiJson.strList(data, "suggestions"));
        return vo;
    }

    @Override
    public RemoteDiagnoseVo diagnose(RemoteDiagnoseBo bo) {
        if (bo == null) {
            return RemoteDiagnoseVo.fail("错题数据为空");
        }
        if (!enabled()) {
            return RemoteDiagnoseVo.fail("AI 能力不可用");
        }
        Map<String, Object> input = new LinkedHashMap<>();
        List<Map<String, Object>> wrongItems = new ArrayList<>();
        if (bo.getWrongItems() != null) {
            for (org.dromara.exam.ai.api.domain.RemoteAiWrongItemVo item : bo.getWrongItems()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("question_id", item.getQuestionId());
                map.put("question_type", item.getQuestionType());
                map.put("stem", item.getStem());
                map.put("knowledge_points", item.getKnowledgePoints() == null ? List.of() : item.getKnowledgePoints());
                map.put("answer_text", item.getAnswerText());
                map.put("standard_answer", item.getStandardAnswer());
                map.put("wrong_count", item.getWrongCount());
                wrongItems.add(map);
            }
        }
        input.put("wrong_items", wrongItems);
        Map<String, Object> mastered = new LinkedHashMap<>();
        mastered.put("points", bo.getMastered() == null ? List.of() : bo.getMastered());
        input.put("mastered", mastered);

        AiAgentClient.AgentResp resp = client.post("/skill/" + SKILL_DIAGNOSIS + "/run",
            skillBody(input, bo.getTenantId(), null));
        if (resp.getCode() != 200) {
            return RemoteDiagnoseVo.fail(resp.getMsg());
        }
        JsonNode data = resp.getData().path("data");
        RemoteDiagnoseVo vo = new RemoteDiagnoseVo();
        vo.setSuccess(true);
        vo.setAdvice(AiJson.str(data, "advice"));
        vo.setPriority(AiJson.strList(data, "priority"));
        vo.setModel(AiJson.str(resp.getData(), "model"));
        JsonNode dist = AiJson.obj(data, "error_distribution");
        if (dist != null) {
            vo.setErrorDistribution(dist.toString());
        }
        List<RemoteAiWeakPointVo> weakPoints = new ArrayList<>();
        for (JsonNode node : AiJson.list(data, "weak_points")) {
            RemoteAiWeakPointVo item = new RemoteAiWeakPointVo();
            item.setKnowledgePoint(AiJson.str(node, "knowledge_point"));
            item.setWrongCount(AiJson.i(node, "wrong_count"));
            item.setMastery(AiJson.bd(node, "mastery"));
            item.setErrorType(AiJson.str(node, "error_type"));
            item.setEvidence(AiJson.str(node, "evidence"));
            weakPoints.add(item);
        }
        vo.setWeakPoints(weakPoints);
        return vo;
    }

    @Override
    public RemoteSkillRunVo runSkill(RemoteSkillRunBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getSkillCode())) {
            return RemoteSkillRunVo.fail("Skill 编码为空");
        }
        if (!enabled()) {
            return RemoteSkillRunVo.fail("AI 能力不可用");
        }
        AiAgentClient.AgentResp resp = client.post(
            "/skill/" + bo.getSkillCode() + "/run",
            skillBody(bo.getInput() == null ? Map.of() : bo.getInput(), bo.getTenantId(), bo.getModelCode()));
        if (resp.getCode() != 200) {
            return RemoteSkillRunVo.fail(resp.getMsg());
        }
        RemoteSkillRunVo vo = new RemoteSkillRunVo();
        vo.setSuccess(true);
        vo.setSkillCode(bo.getSkillCode());
        vo.setModel(AiJson.str(resp.getData(), "model"));
        vo.setLatencyMs(AiJson.i(resp.getData(), "latency_ms") == null ? null : AiJson.i(resp.getData(), "latency_ms").longValue());
        vo.setAttemptChain(AiJson.strList(resp.getData(), "attempt_chain"));
        JsonNode data = resp.getData().path("data");
        vo.setData(data.isMissingNode() ? null : data);
        return vo;
    }

    // ------------------------------------------------------------------ 私有方法

    private String tenant() {
        return StringUtils.defaultIfBlank(properties.getDefaultTenant(), "000000");
    }

    private Map<String, Object> skillBody(Map<String, Object> input, String tenantId, String modelCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tenant_id", StringUtils.defaultIfBlank(tenantId, tenant()));
        body.put("input", input);
        if (StringUtils.isNotBlank(modelCode)) {
            body.put("model_code", modelCode);
        }
        return body;
    }

    private Map<String, Object> toMarkInput(RemoteMarkAiBo bo) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("question_type", StringUtils.defaultIfBlank(bo.getQuestionType(), "SHORT_ANSWER"));
        input.put("stem", StringUtils.defaultIfBlank(bo.getTitle(), ""));
        input.put("full_score", bo.getFullScore() == null ? BigDecimal.ZERO : bo.getFullScore());
        input.put("standard_answer", StringUtils.defaultIfBlank(bo.getStandardAnswer(), ""));
        input.put("rubric", StringUtils.defaultIfBlank(bo.getRubric(), ""));
        input.put("analysis", StringUtils.defaultIfBlank(bo.getAnalysis(), ""));
        input.put("answer_text", StringUtils.defaultIfBlank(bo.getAnswerText(), ""));
        return input;
    }

    private RemoteMarkAiVo toMarkVo(JsonNode wrapper) {
        JsonNode data = wrapper.path("data");
        RemoteMarkAiVo vo = new RemoteMarkAiVo();
        vo.setSuccess(true);
        vo.setScore(AiJson.bd(data, "score"));
        vo.setReason(AiJson.str(data, "reason"));
        vo.setComment(AiJson.str(data, "comment"));
        vo.setConfidence(AiJson.bd(data, "confidence"));
        vo.setNeedHuman(AiJson.bool(data, "need_human", false));
        vo.setModel(AiJson.str(wrapper, "model"));
        List<RemoteAiMatchPointVo> points = new ArrayList<>();
        for (JsonNode node : AiJson.list(data, "matched_points")) {
            RemoteAiMatchPointVo item = new RemoteAiMatchPointVo();
            item.setPoint(AiJson.str(node, "point"));
            item.setGot(AiJson.bool(node, "got", false));
            item.setScore(AiJson.bd(node, "score"));
            points.add(item);
        }
        vo.setMatchedPoints(points);
        return vo;
    }

    private List<Map<String, Object>> toOptionMaps(List<RemoteAiOptionVo> options) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (options == null) {
            return out;
        }
        for (RemoteAiOptionVo option : options) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key", option.getKey());
            map.put("content", option.getContent());
            out.add(map);
        }
        return out;
    }

    private List<RemoteAiIssueVo> toIssues(JsonNode data) {
        List<RemoteAiIssueVo> out = new ArrayList<>();
        for (JsonNode node : AiJson.list(data, "issues")) {
            RemoteAiIssueVo issue = new RemoteAiIssueVo();
            issue.setLevel(AiJson.str(node, "level"));
            issue.setType(AiJson.str(node, "type"));
            issue.setDetail(AiJson.str(node, "detail"));
            issue.setSuggestion(AiJson.str(node, "suggestion"));
            out.add(issue);
        }
        return out;
    }
}
