package org.dromara.exam.question.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.core.DropDownOptions;
import org.dromara.exam.question.domain.QuestionBank;
import org.dromara.exam.question.domain.bo.QuestionBo;
import org.dromara.exam.question.domain.bo.QuestionImportRow;
import org.dromara.exam.question.domain.bo.QuestionOptionSaveBo;
import org.dromara.exam.question.domain.vo.QuestionImportVo;
import org.dromara.exam.question.mapper.QuestionBankMapper;
import org.dromara.exam.question.service.IQuestionImportService;
import org.dromara.exam.question.service.IQuestionService;
import org.dromara.system.api.RemoteDictService;
import org.dromara.system.api.domain.vo.RemoteDictDataVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 试题批量导入Service业务层处理
 *
 * <p>流程：解析 Excel → 整批校验 → 整批入库。校验全部通过才写库，任意一行有问题就整批回滚，
 * 把每一行的行号和原因一次性回给用户，避免「导入一半成功一半失败」留下脏数据。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionImportServiceImpl implements IQuestionImportService {

    private static final String DICT_QUESTION_TYPE = "question_type";
    private static final String DICT_QUESTION_DIFFICULTY = "question_difficulty";
    private static final String DICT_QUESTION_STATUS = "question_status";

    /** 模板列序号，与 QuestionImportVo 的 @ExcelProperty index 一一对应 */
    private static final int COL_BANK_NAME = 0;
    private static final int COL_QUESTION_TYPE = 1;
    private static final int COL_DIFFICULTY = 10;
    private static final int COL_STATUS = 13;

    /** 答案录入形态，与前端 questionMeta.ts 的 answerMode 保持一致 */
    private static final String KIND_SINGLE = "single";
    private static final String KIND_MULTIPLE = "multiple";
    private static final String KIND_JUDGE = "judge";
    private static final String KIND_BLANK = "blank";
    private static final String KIND_TEXT = "text";
    private static final String KIND_CODE = "code";
    private static final String KIND_PAIRS = "pairs";

    /** 选项标识，与前端 OPTION_KEYS 一致 */
    private static final List<String> OPTION_KEYS = List.of("A", "B", "C", "D", "E", "F");

    /** 题型编码 → 答案形态 */
    private static final Map<String, String> KIND_BY_CODE = Map.ofEntries(
        Map.entry("SINGLE", KIND_SINGLE),
        Map.entry("MULTIPLE", KIND_MULTIPLE),
        Map.entry("JUDGE", KIND_JUDGE),
        Map.entry("BLANK", KIND_BLANK),
        Map.entry("SHORT_ANSWER", KIND_TEXT),
        Map.entry("ESSAY", KIND_TEXT),
        Map.entry("CODE", KIND_CODE),
        Map.entry("UPLOAD_FILE", KIND_TEXT),
        Map.entry("MATCH", KIND_PAIRS)
    );

    /**
     * 字典还没维护时的兜底（标签 → 编码），保证不配字典也能导入
     */
    private static final Map<String, String> FALLBACK_TYPE = Map.ofEntries(
        Map.entry("单选题", "SINGLE"),
        Map.entry("多选题", "MULTIPLE"),
        Map.entry("判断题", "JUDGE"),
        Map.entry("填空题", "BLANK"),
        Map.entry("简答题", "SHORT_ANSWER"),
        Map.entry("论述题", "ESSAY"),
        Map.entry("代码题", "CODE"),
        Map.entry("文件上传题", "UPLOAD_FILE"),
        Map.entry("匹配题", "MATCH")
    );
    private static final Map<String, String> FALLBACK_DIFFICULTY = Map.of("简单", "easy", "中等", "medium", "困难", "hard");
    private static final Map<String, String> FALLBACK_STATUS = Map.of("草稿", "draft", "启用", "enabled", "废弃", "disabled");

    /** 判断题正确答案的各种写法 */
    private static final Set<String> TRUE_WORDS = Set.of("正确", "对", "是", "√", "✔", "T", "TRUE", "Y", "YES", "A", "1");
    private static final Set<String> FALSE_WORDS = Set.of("错误", "错", "否", "×", "✗", "X", "F", "FALSE", "N", "NO", "B", "0");

    /** question.score 为 decimal(5,2)，最大 999.99 */
    private static final BigDecimal MAX_SCORE = new BigDecimal("999.99");

    private final IQuestionService questionService;

    private final QuestionBankMapper questionBankMapper;

    @DubboReference
    private RemoteDictService remoteDictService;

    /**
     * 批量导入试题
     *
     * @param rows          解析出来的数据行（带 Excel 行号）
     * @param defaultBankId 上传时选择的默认题库
     * @return 导入结果描述
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importQuestions(List<QuestionImportRow> rows, Long defaultBankId) {
        if (CollUtil.isEmpty(rows)) {
            throw new ServiceException("未解析到有效数据，请按模板填写后再上传");
        }
        if (ObjectUtil.isNotNull(defaultBankId) && ObjectUtil.isNull(questionBankMapper.selectById(defaultBankId))) {
            throw new ServiceException("默认题库不存在，请重新选择题库");
        }
        // 一次导入只查一次字典和题库，避免逐行回查
        DictHolder dict = loadDict();
        Map<String, List<Long>> bankMap = loadBankMap();

        List<QuestionBo> boList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (QuestionImportRow row : rows) {
            try {
                boList.add(buildQuestionBo(row.getData(), dict, bankMap, defaultBankId));
            } catch (ServiceException e) {
                errors.add(StrUtil.format("第{}行：{}", row.getRowNum(), e.getMessage()));
            } catch (Exception e) {
                log.error("试题导入行解析异常 row={}", row.getRowNum(), e);
                errors.add(StrUtil.format("第{}行：数据解析异常", row.getRowNum()));
            }
        }
        if (CollUtil.isNotEmpty(errors)) {
            throw new ServiceException(buildErrorMessage(errors));
        }
        for (QuestionBo bo : boList) {
            questionService.createQuestion(bo);
        }
        return StrUtil.format("恭喜您，全部导入成功！共{}条", boList.size());
    }

    /**
     * 构建导入模板的下拉可选项
     */
    @Override
    public List<DropDownOptions> buildTemplateOptions() {
        DictHolder dict = loadDict();
        List<DropDownOptions> options = new ArrayList<>();
        List<String> bankNames = new ArrayList<>();
        for (QuestionBank bank : questionBankMapper.selectList(Wrappers.<QuestionBank>lambdaQuery())) {
            if (StringUtils.isNotBlank(bank.getBankName())) {
                bankNames.add(bank.getBankName().trim());
            }
        }
        if (CollUtil.isNotEmpty(bankNames)) {
            options.add(new DropDownOptions(COL_BANK_NAME, bankNames));
        }
        if (CollUtil.isNotEmpty(dict.type.labels())) {
            options.add(new DropDownOptions(COL_QUESTION_TYPE, dict.type.labels()));
        }
        if (CollUtil.isNotEmpty(dict.difficulty.labels())) {
            options.add(new DropDownOptions(COL_DIFFICULTY, dict.difficulty.labels()));
        }
        if (CollUtil.isNotEmpty(dict.status.labels())) {
            options.add(new DropDownOptions(COL_STATUS, dict.status.labels()));
        }
        return options;
    }

    /* --------------------------------- 行数据转换 --------------------------------- */

    /**
     * 一行 Excel 转成一个待入库的试题
     */
    private QuestionBo buildQuestionBo(QuestionImportVo vo, DictHolder dict, Map<String, List<Long>> bankMap, Long defaultBankId) {
        QuestionBo bo = new QuestionBo();
        bo.setTitle(toHtml(required(vo.getTitle(), "题干不能为空")));
        String rawType = required(vo.getQuestionType(), "题型不能为空");
        String questionType = dict.type.resolve(rawType);
        if (StringUtils.isBlank(questionType)) {
            throw new ServiceException(StrUtil.format("题型「{}」不正确，请按模板下拉选择", rawType));
        }
        bo.setQuestionType(questionType);
        bo.setBankId(resolveBankId(vo.getBankName(), bankMap, defaultBankId));
        bo.setDifficulty(resolveDifficulty(vo.getDifficulty(), dict));
        bo.setScore(resolveScore(vo.getScore()));
        bo.setAnalysis(toHtml(vo.getAnalysis()));
        bo.setStatus(resolveStatus(vo.getStatus(), dict));
        buildAnswer(bo, vo, resolveKind(questionType, rawType, dict.type));
        return bo;
    }

    /**
     * 所属题库：Excel 里填了名称就按名称翻译（支持一行一个题库），没填就落到上传时选的默认题库
     */
    private Long resolveBankId(String bankName, Map<String, List<Long>> bankMap, Long defaultBankId) {
        if (StringUtils.isNotBlank(bankName)) {
            List<Long> ids = bankMap.get(bankName.trim());
            if (CollUtil.isEmpty(ids)) {
                throw new ServiceException(StrUtil.format("题库「{}」不存在，请先在题库管理中创建", bankName.trim()));
            }
            if (ids.size() > 1) {
                throw new ServiceException(StrUtil.format("题库名称「{}」重复，请改用上传时选择的题库", bankName.trim()));
            }
            return ids.get(0);
        }
        if (ObjectUtil.isNotNull(defaultBankId)) {
            return defaultBankId;
        }
        throw new ServiceException("题库名称为空，请在 Excel 中填写题库名称，或上传时选择题库");
    }

    private String resolveDifficulty(String text, DictHolder dict) {
        // 留空默认中等
        return resolveByDict(dict.difficulty, text, "难度", "中等", "medium");
    }

    private String resolveStatus(String text, DictHolder dict) {
        // 留空默认启用
        return resolveByDict(dict.status, text, "状态", "启用", "enabled");
    }

    /**
     * 中文名称 → 字典编码：留空取默认值，填了却认不出来直接报错，避免落库成空字符串
     */
    private String resolveByDict(DictItem item, String text, String fieldName, String defaultLabel, String defaultValue) {
        String value = item.resolve(text);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        if (StringUtils.isBlank(text)) {
            value = item.resolve(defaultLabel);
            return StringUtils.isNotBlank(value) ? value : defaultValue;
        }
        throw new ServiceException(StrUtil.format("{}「{}」不正确，请按模板下拉选择", fieldName, text.trim()));
    }

    private BigDecimal resolveScore(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        BigDecimal score = Convert.toBigDecimal(text.trim(), null);
        if (ObjectUtil.isNull(score) || score.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(StrUtil.format("分值「{}」不是合法数字", text.trim()));
        }
        if (score.compareTo(MAX_SCORE) > 0) {
            throw new ServiceException(StrUtil.format("分值不能超过{}", MAX_SCORE));
        }
        return score;
    }

    /**
     * 按题型分派答案解析
     */
    private void buildAnswer(QuestionBo bo, QuestionImportVo vo, String kind) {
        String answer = required(vo.getAnswer(), "正确答案不能为空");
        switch (kind) {
            case KIND_JUDGE -> buildJudgeAnswer(bo, answer);
            case KIND_SINGLE -> buildOptionAnswer(bo, vo, answer, true);
            case KIND_MULTIPLE -> buildOptionAnswer(bo, vo, answer, false);
            case KIND_BLANK -> bo.setAnswer(buildBlankAnswer(answer));
            case KIND_PAIRS -> bo.setAnswer(buildPairAnswer(answer));
            case KIND_CODE -> bo.setAnswer(buildCodeAnswer(vo, answer));
            default -> bo.setAnswer(buildTextAnswer(answer));
        }
    }

    /**
     * 判断题：选项固定为「正确 / 错误」，与编辑页的判断题形态保持一致
     */
    private void buildJudgeAnswer(QuestionBo bo, String answer) {
        bo.setOptions(List.of(buildOption("A", "正确"), buildOption("B", "错误")));
        String key = parseJudgeAnswer(answer);
        JSONObject json = JSONUtil.createObj();
        json.set("rightKeys", CollUtil.newArrayList(key));
        bo.setAnswer(json.toString());
    }

    /**
     * 单选 / 多选：选项取 Excel 里填了的选项列，正确答案按选项标识校验
     *
     * @param single 是否单选题（单选题只允许一个正确答案）
     */
    private void buildOptionAnswer(QuestionBo bo, QuestionImportVo vo, String answer, boolean single) {
        // 用 Arrays.asList：List.of 不允许 null，没填的选项列就是 null
        List<String> contents = Arrays.asList(
            vo.getOptionA(), vo.getOptionB(), vo.getOptionC(),
            vo.getOptionD(), vo.getOptionE(), vo.getOptionF());
        List<QuestionOptionSaveBo> options = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            if (StringUtils.isBlank(contents.get(i))) {
                continue;
            }
            options.add(buildOption(OPTION_KEYS.get(i), contents.get(i)));
        }
        if (options.size() < 2) {
            throw new ServiceException("单选 / 多选题至少需要填写两个选项");
        }
        bo.setOptions(options);
        List<String> rightKeys = parseRightKeys(answer, options);
        if (single && rightKeys.size() > 1) {
            throw new ServiceException("单选题只能有一个正确答案");
        }
        JSONObject json = JSONUtil.createObj();
        json.set("rightKeys", rightKeys);
        bo.setAnswer(json.toString());
    }

    /**
     * 填空：多个空用 | 分隔，同一个空的多种可接受写法用 ; 分隔
     */
    private String buildBlankAnswer(String answer) {
        JSONArray blanks = JSONUtil.createArray();
        for (String blank : answer.split("[|｜]")) {
            if (StringUtils.isBlank(blank)) {
                continue;
            }
            List<String> accepts = new ArrayList<>();
            for (String item : blank.split("[;；]")) {
                String text = item.trim();
                if (StringUtils.isNotBlank(text) && !accepts.contains(text)) {
                    accepts.add(text);
                }
            }
            if (accepts.isEmpty()) {
                throw new ServiceException("填空题的参考答案不能为空");
            }
            blanks.add(JSONUtil.createObj().set("answers", accepts));
        }
        if (blanks.isEmpty()) {
            throw new ServiceException("填空题的参考答案不能为空");
        }
        return JSONUtil.createObj().set("blanks", blanks).toString();
    }

    /**
     * 匹配题：多组用 ; 分隔，每组按「左项=右项」填写
     */
    private String buildPairAnswer(String answer) {
        JSONArray pairs = JSONUtil.createArray();
        for (String item : answer.split("[;；\n]")) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            int index = item.indexOf('=');
            if (index <= 0) {
                index = item.indexOf('：');
            }
            if (index <= 0 || index == item.length() - 1) {
                throw new ServiceException(StrUtil.format("匹配题的正确答案需按「左项=右项」填写：{}", item.trim()));
            }
            JSONObject pair = JSONUtil.createObj();
            pair.set("left", item.substring(0, index).trim());
            pair.set("right", item.substring(index + 1).trim());
            pairs.add(pair);
        }
        if (pairs.isEmpty()) {
            throw new ServiceException("匹配题的正确答案不能为空");
        }
        return JSONUtil.createObj().set("pairs", pairs).toString();
    }

    /**
     * 代码题：参考实现按纯文本保存，语言取「代码语言」列
     */
    private String buildCodeAnswer(QuestionImportVo vo, String answer) {
        JSONObject json = JSONUtil.createObj();
        json.set("language", StringUtils.isNotBlank(vo.getLanguage()) ? vo.getLanguage().trim() : StringUtils.EMPTY);
        json.set("answer", answer);
        return json.toString();
    }

    /**
     * 简答 / 论述 / 文件上传：参考答案按富文本保存
     */
    private String buildTextAnswer(String answer) {
        return JSONUtil.createObj().set("answer", toHtml(answer)).toString();
    }

    /* ---------------------------------- 解析工具 ---------------------------------- */

    /**
     * 判断题答案：正确 → A，错误 → B
     */
    private String parseJudgeAnswer(String answer) {
        String text = answer.trim();
        if (TRUE_WORDS.contains(text) || TRUE_WORDS.contains(text.toUpperCase(Locale.ROOT))) {
            return "A";
        }
        if (FALSE_WORDS.contains(text) || FALSE_WORDS.contains(text.toUpperCase(Locale.ROOT))) {
            return "B";
        }
        throw new ServiceException(StrUtil.format("判断题的正确答案只能填「正确」或「错误」：{}", text));
    }

    /**
     * 单选 / 多选答案：按选项标识解析，也允许填 1/2/3 这样的序号
     */
    private List<String> parseRightKeys(String answer, List<QuestionOptionSaveBo> options) {
        List<String> keys = new ArrayList<>();
        for (String part : answer.split("[,，;；/\\s]+")) {
            if (StringUtils.isBlank(part)) {
                continue;
            }
            String key = part.trim().toUpperCase(Locale.ROOT);
            if (key.matches("\\d+")) {
                int index = Integer.parseInt(key) - 1;
                if (index < 0 || index >= options.size()) {
                    throw new ServiceException(StrUtil.format("正确答案「{}」超出选项范围", part.trim()));
                }
                key = options.get(index).getOptionKey();
            }
            boolean matched = false;
            for (QuestionOptionSaveBo option : options) {
                if (option.getOptionKey().equals(key)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new ServiceException(StrUtil.format("正确答案「{}」不在已填写的选项中", part.trim()));
            }
            if (!keys.contains(key)) {
                keys.add(key);
            }
        }
        if (keys.isEmpty()) {
            throw new ServiceException("正确答案不能为空");
        }
        return keys;
    }

    /**
     * 题型 → 答案形态：先按编码，再按中文名称关键字兜底，认不出来就不让导入
     */
    private String resolveKind(String questionType, String rawText, DictItem dict) {
        String kind = KIND_BY_CODE.get(questionType.toUpperCase(Locale.ROOT));
        if (StringUtils.isNotBlank(kind)) {
            return kind;
        }
        String label = StringUtils.isNotBlank(dict.labelOf(questionType)) ? dict.labelOf(questionType) : rawText;
        if (label.contains("判断")) {
            return KIND_JUDGE;
        }
        if (label.contains("单选")) {
            return KIND_SINGLE;
        }
        if (label.contains("多选")) {
            return KIND_MULTIPLE;
        }
        if (label.contains("填空")) {
            return KIND_BLANK;
        }
        if (label.contains("匹配")) {
            return KIND_PAIRS;
        }
        if (label.contains("代码")) {
            return KIND_CODE;
        }
        if (label.contains("简答") || label.contains("论述") || label.contains("上传")) {
            return KIND_TEXT;
        }
        throw new ServiceException(StrUtil.format("暂不支持导入的题型：{}", rawText));
    }

    private QuestionOptionSaveBo buildOption(String key, String content) {
        QuestionOptionSaveBo option = new QuestionOptionSaveBo();
        option.setOptionKey(key);
        option.setOptionContent(toHtml(content));
        return option;
    }

    /**
     * 纯文本转成富文本：转义 HTML 特殊字符，保证题干里的 {@code <} {@code >} 不被当成标签
     */
    private String toHtml(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String escaped = text.trim()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
        return "<p>" + escaped.replace("\n", "<br/>") + "</p>";
    }

    private String required(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    /**
     * 组装错误提示，行数太多时只展示前 20 条
     */
    private String buildErrorMessage(List<String> errors) {
        StringBuilder sb = new StringBuilder(StrUtil.format("共{}行数据校验不通过，已全部回滚，请修正后重新上传：", errors.size()));
        List<String> shown = errors.size() > 20 ? errors.subList(0, 20) : errors;
        for (String error : shown) {
            sb.append("<br/>").append(error);
        }
        if (errors.size() > 20) {
            sb.append("<br/>...等共").append(errors.size()).append("条错误");
        }
        return sb.toString();
    }

    /* ---------------------------------- 字典 / 题库 --------------------------------- */

    /**
     * 题库名称 → 题库ID（同名题库会有多个ID，命中时提示用户改用上传时选择的题库）
     */
    private Map<String, List<Long>> loadBankMap() {
        List<QuestionBank> banks = questionBankMapper.selectList(Wrappers.<QuestionBank>lambdaQuery());
        Map<String, List<Long>> map = new HashMap<>();
        for (QuestionBank bank : banks) {
            if (StringUtils.isBlank(bank.getBankName())) {
                continue;
            }
            map.computeIfAbsent(bank.getBankName().trim(), key -> new ArrayList<>()).add(bank.getId());
        }
        return map;
    }

    private DictHolder loadDict() {
        DictHolder holder = new DictHolder();
        holder.type = loadDictItem(DICT_QUESTION_TYPE, FALLBACK_TYPE);
        holder.difficulty = loadDictItem(DICT_QUESTION_DIFFICULTY, FALLBACK_DIFFICULTY);
        holder.status = loadDictItem(DICT_QUESTION_STATUS, FALLBACK_STATUS);
        return holder;
    }

    /**
     * 读取字典，字典服务不可用或没配数据时用兜底常量，保证导入流程不中断
     */
    private DictItem loadDictItem(String dictType, Map<String, String> fallback) {
        DictItem item = new DictItem();
        try {
            List<RemoteDictDataVo> list = remoteDictService.selectDictDataByType(dictType);
            if (CollUtil.isNotEmpty(list)) {
                for (RemoteDictDataVo vo : list) {
                    item.put(vo.getDictLabel(), vo.getDictValue());
                }
                return item;
            }
        } catch (Exception e) {
            log.warn("读取字典 {} 失败，使用内置兜底值", dictType, e);
        }
        fallback.forEach(item::put);
        return item;
    }

    /**
     * 一次导入用到的三本字典
     */
    private static class DictHolder {
        private DictItem type;
        private DictItem difficulty;
        private DictItem status;
    }

    /**
     * 一本字典的「标签 → 编码」与合法编码集合
     */
    private static class DictItem {

        private final Map<String, String> labelToValue = new LinkedHashMap<>();

        private final Map<String, String> valueToLabel = new LinkedHashMap<>();

        private final Set<String> values = new HashSet<>();

        private void put(String label, String value) {
            if (StringUtils.isBlank(label) || StringUtils.isBlank(value)) {
                return;
            }
            labelToValue.put(label.trim(), value.trim());
            valueToLabel.put(value.trim(), label.trim());
            values.add(value.trim());
        }

        /**
         * 中文名称优先，其次按编码匹配，都命中不了返回空
         */
        private String resolve(String text) {
            if (StringUtils.isBlank(text)) {
                return StringUtils.EMPTY;
            }
            String key = text.trim();
            String value = labelToValue.get(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
            for (String item : values) {
                if (item.equalsIgnoreCase(key)) {
                    return item;
                }
            }
            return StringUtils.EMPTY;
        }

        private String labelOf(String value) {
            return valueToLabel.get(value);
        }

        private List<String> labels() {
            return new ArrayList<>(labelToValue.keySet());
        }
    }

}
