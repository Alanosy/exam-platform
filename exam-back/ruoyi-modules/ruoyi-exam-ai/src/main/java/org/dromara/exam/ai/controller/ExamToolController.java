package org.dromara.exam.ai.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.domain.R;
import org.dromara.exam.ai.client.AiAgentClient;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RemoteRecordVo;
import org.dromara.exam.manage.api.RemoteExamService;
import org.dromara.exam.manage.api.domain.RemoteExamVo;
import org.dromara.exam.question.api.RemoteQuestionService;
import org.dromara.exam.question.api.domain.RemoteQuestionBankVo;
import org.dromara.exam.question.api.domain.RemoteQuestionSaveBo;
import org.dromara.exam.question.api.domain.RemoteQuestionSaveItem;
import org.dromara.exam.question.api.domain.RemoteQuestionSaveOption;
import org.dromara.exam.question.api.domain.RemoteQuestionSearchBo;
import org.dromara.exam.question.api.domain.RemoteQuestionVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 工具端点
 *
 * <p>Python Agent（ruoyi-exam-agent）通过 {@code /api/exam-tool/**} 回调 Java 侧业务能力，
 * 这里的每个方法对应 Agent 工具清单里的一个工具（见 agent 的 app/tools/catalog.py）。
 *
 * <p><b>为什么不让 Agent 直连数据库</b>：租户、逻辑删除、选项与知识点关联的写入规则
 * 都在业务服务里，Agent 自己写一套必然会长歪；而且答题库与主库是分库的，
 * 跨库 JOIN 本来就不该出现在任何一层。
 *
 * <p><b>鉴权</b>：这些端点不经过网关（Agent 直连 9220），所以 {@code @SaIgnore} 掉登录校验，
 * 内网通行证由 {@link org.dromara.exam.ai.config.ExamToolSameTokenConfig} 补，
 * 真正的把关是简单的共享令牌 {@code X-Agent-Token}。没配令牌时放行，方便本地联调。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@SaIgnore
@RestController
@RequestMapping("/api/exam-tool")
public class ExamToolController {

    /**
     * 跨服务一律走 Dubbo 引用，不能用构造器注入：
     * {@code Remote*Service} 是接口，Spring 容器里根本没有实现类，
     * 用 final 字段 + &#64;RequiredArgsConstructor 会直接「找不到 bean」起不来。
     */
    // check = false：AI 服务不该因为题库 / 考试 / 答题某个提供者没起就整体起不来，
    // 缺哪个能力就在对应工具上报错，其余对话照常可用
    @DubboReference(check = false)
    private RemoteQuestionService remoteQuestionService;

    @DubboReference(check = false)
    private RemoteExamService remoteExamService;

    @DubboReference(check = false)
    private RemoteExamAnswerService remoteExamAnswerService;

    @Value("${exam-tool.token:}")
    private String toolToken;

    // ---------------------------------------------------------------- 题库

    /**
     * 题库列表（按名称模糊匹配）
     */
    @PostMapping("/bank/list")
    public R<Map<String, Object>> bankList(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        Map<String, Object> params = body == null ? new HashMap<>() : body;
        String keyword = str(params.get("keyword"));
        Integer limit = intValue(params.get("limit"), 20);
        List<RemoteQuestionBankVo> banks = remoteQuestionService.listBanks(keyword, limit);
        List<Map<String, Object>> items = new ArrayList<>(banks.size());
        for (RemoteQuestionBankVo bank : banks) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(bank.getId()));
            item.put("name", bank.getName());
            item.put("questionCount", bank.getQuestionCount());
            item.put("categoryName", bank.getCategoryName());
            items.add(item);
        }
        return R.ok(Map.of("items", items));
    }

    /**
     * 新建题库
     */
    @PostMapping("/bank/create")
    public R<Map<String, Object>> bankCreate(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        Map<String, Object> params = body == null ? new HashMap<>() : body;
        String bankName = str(params.get("bankName"));
        Long id = remoteQuestionService.createBank(bankName);
        if (id == null) {
            return R.fail("题库创建失败");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", String.valueOf(id));
        data.put("name", bankName);
        return R.ok(data);
    }

    // ---------------------------------------------------------------- 试题

    /**
     * 检索试题
     */
    @PostMapping("/question/search")
    public R<Map<String, Object>> questionSearch(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        Map<String, Object> params = body == null ? new HashMap<>() : body;
        RemoteQuestionSearchBo bo = new RemoteQuestionSearchBo();
        bo.setKeyword(str(params.get("keyword")));
        bo.setBankId(longValue(params.get("bankId")));
        bo.setQuestionType(str(params.get("questionType")));
        bo.setDifficulty(str(params.get("difficulty")));
        bo.setLimit(intValue(params.get("limit"), 20));
        List<RemoteQuestionVo> questions = remoteQuestionService.searchQuestions(bo);
        List<Map<String, Object>> items = new ArrayList<>(questions.size());
        for (RemoteQuestionVo q : questions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(q.getQuestionId()));
            item.put("title", q.getTitle());
            item.put("questionType", q.getQuestionType());
            item.put("difficulty", q.getDifficulty());
            items.add(item);
        }
        return R.ok(Map.of("items", items));
    }

    /**
     * 批量保存 AI 生成的试题
     */
    @PostMapping("/question/save-batch")
    public R<Map<String, Object>> questionSaveBatch(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        if (body == null || body.get("questions") == null) {
            return R.fail("questions 不能为空");
        }
        RemoteQuestionSaveBo bo = new RemoteQuestionSaveBo();
        bo.setBankId(longValue(body.get("bankId")));
        bo.setStatus(str(body.get("status")));
        bo.setQuestions(toSaveItems(body.get("questions")));
        List<Long> ids = remoteQuestionService.saveQuestions(bo);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ids", ids.stream().map(String::valueOf).toList());
        data.put("count", ids.size());
        data.put("bankId", String.valueOf(bo.getBankId()));
        log.info("AI 试题入库 bankId={} 成功 {} 条", bo.getBankId(), ids.size());
        return R.ok(data);
    }

    // ---------------------------------------------------------------- 考试

    /**
     * 按名称关键词查找考试
     */
    @PostMapping("/exam/find")
    public R<Map<String, Object>> examFind(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        Map<String, Object> params = body == null ? new HashMap<>() : body;
        List<RemoteExamVo> exams = remoteExamService.searchExams(str(params.get("keyword")), intValue(params.get("limit"), 10));
        List<Map<String, Object>> items = new ArrayList<>(exams.size());
        for (RemoteExamVo exam : exams) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(exam.getExamId()));
            item.put("name", exam.getExamName());
            item.put("status", exam.getStatus());
            items.add(item);
        }
        return R.ok(Map.of("items", items));
    }

    /**
     * 考试答卷统计
     *
     * <p>聚合在 Java 侧做完再回给 Agent：答卷记录在独立的答题库，
     * 让 Agent 拿明细自己在内存里算，既慢又会跨库。
     */
    @PostMapping("/stat/exam-answers")
    public R<Map<String, Object>> examAnswerStats(
        @RequestHeader(value = "X-Agent-Token", required = false) String token,
        @RequestBody(required = false) Map<String, Object> body
    ) {
        if (!checkToken(token)) {
            return R.fail(403, "令牌无效");
        }
        Map<String, Object> params = body == null ? new HashMap<>() : body;
        Long examId = longValue(params.get("examId"));
        if (examId == null) {
            return R.fail("examId 不能为空");
        }
        List<RemoteRecordVo> records = remoteExamAnswerService.listRecordsByExam(examId, null);
        RemoteExamVo exam = remoteExamService.queryExam(examId);

        int total = records == null ? 0 : records.size();
        int submitted = 0;
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = null;
        BigDecimal min = null;
        int passed = 0;
        int[] bands = new int[5]; // <60 / 60-69 / 70-79 / 80-89 / 90-100
        if (records != null) {
            for (RemoteRecordVo record : records) {
                if (!"submitted".equals(record.getStatus())) {
                    continue;
                }
                submitted++;
                BigDecimal score = record.getTotalScore() == null ? BigDecimal.ZERO : record.getTotalScore();
                sum = sum.add(score);
                max = max == null || score.compareTo(max) > 0 ? score : max;
                min = min == null || score.compareTo(min) < 0 ? score : min;
                if (record.getPassed() != null && record.getPassed() > 0) {
                    passed++;
                }
                int idx = score.intValue() < 60 ? 0 : Math.min((score.intValue() - 60) / 10 + 1, 4);
                bands[idx]++;
            }
        }
        BigDecimal avg = submitted == 0 ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(submitted), 1, RoundingMode.HALF_UP);
        String passRate = submitted == 0 ? "0%" : Math.round(passed * 100.0 / submitted) + "%";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("examId", String.valueOf(examId));
        data.put("examName", exam == null ? ("考试 " + examId) : exam.getExamName());
        data.put("total", total);
        data.put("submitted", submitted);
        data.put("avgScore", avg.doubleValue());
        data.put("maxScore", max == null ? 0 : max.doubleValue());
        data.put("minScore", min == null ? 0 : min.doubleValue());
        data.put("passRate", passRate);
        List<Map<String, Object>> scoreBands = List.of(
            band("60 分以下", bands[0]),
            band("60-69", bands[1]),
            band("70-79", bands[2]),
            band("80-89", bands[3]),
            band("90 分以上", bands[4])
        );
        data.put("scoreBands", scoreBands);
        return R.ok(data);
    }

    private Map<String, Object> band(String name, int count) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("band", name);
        item.put("count", count);
        return item;
    }

    // ---------------------------------------------------------------- 内部

    /**
     * Agent 侧的入参是 JSON，选项结构直接沿用 AI 生成的 {key, content}
     */
    @SuppressWarnings("unchecked")
    private List<RemoteQuestionSaveItem> toSaveItems(Object raw) {
        List<RemoteQuestionSaveItem> items = new ArrayList<>();
        if (!(raw instanceof List<?> list)) {
            return items;
        }
        for (Object element : list) {
            if (!(element instanceof Map<?, ?> map)) {
                continue;
            }
            Map<String, Object> row = (Map<String, Object>) map;
            RemoteQuestionSaveItem item = new RemoteQuestionSaveItem();
            item.setQuestionType(str(row.get("questionType")));
            item.setStem(str(row.get("stem")));
            item.setAnalysis(str(row.get("analysis")));
            item.setAnswer(str(row.get("answer")));
            item.setDifficulty(str(row.get("difficulty")));
            item.setScore(row.get("score") == null ? null : new BigDecimal(String.valueOf(row.get("score"))));
            Object options = row.get("options");
            if (options instanceof List<?> optionList) {
                List<RemoteQuestionSaveOption> optionItems = new ArrayList<>();
                for (Object option : optionList) {
                    if (!(option instanceof Map<?, ?> optionMap)) {
                        continue;
                    }
                    Map<String, Object> optionRow = (Map<String, Object>) optionMap;
                    RemoteQuestionSaveOption saveOption = new RemoteQuestionSaveOption();
                    saveOption.setKey(str(optionRow.get("key")));
                    saveOption.setContent(str(optionRow.get("content")));
                    optionItems.add(saveOption);
                }
                item.setOptions(optionItems);
            }
            Object points = row.get("knowledgePoints");
            if (points instanceof List<?> pointList) {
                item.setKnowledgePoints(pointList.stream().map(String::valueOf).toList());
            }
            items.add(item);
        }
        return items;
    }

    private boolean checkToken(String token) {
        // 没配令牌就放行：本地联调时不想为了一次调用去改配置
        return toolToken == null || toolToken.isEmpty() || toolToken.equals(token);
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Agent 传的 ID 一律是字符串（雪花 ID 在 JSON 里走数字会丢精度），这里统一转成 Long
     */
    private Long longValue(Object value) {
        if (value == null || "".equals(value)) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer intValue(Object value, int defaultValue) {
        if (value == null || "".equals(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
