package org.dromara.exam.question.dubbo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.question.api.RemoteQuestionService;
import org.dromara.exam.question.api.domain.RemoteQuestionBankVo;
import org.dromara.exam.question.api.domain.RemoteQuestionOptionVo;
import org.dromara.exam.question.api.domain.RemoteQuestionSaveBo;
import org.dromara.exam.question.api.domain.RemoteQuestionSaveItem;
import org.dromara.exam.question.api.domain.RemoteQuestionSaveOption;
import org.dromara.exam.question.api.domain.RemoteQuestionSearchBo;
import org.dromara.exam.question.api.domain.RemoteQuestionVo;
import org.dromara.exam.question.domain.KnowledgePoint;
import org.dromara.exam.question.domain.Question;
import org.dromara.exam.question.domain.QuestionBank;
import org.dromara.exam.question.domain.QuestionOption;
import org.dromara.exam.question.domain.bo.QuestionBo;
import org.dromara.exam.question.domain.bo.QuestionOptionSaveBo;
import org.dromara.exam.question.mapper.KnowledgePointMapper;
import org.dromara.exam.question.mapper.QuestionBankCategoryMapper;
import org.dromara.exam.question.mapper.QuestionBankMapper;
import org.dromara.exam.question.mapper.QuestionMapper;
import org.dromara.exam.question.mapper.QuestionOptionMapper;
import org.dromara.exam.question.domain.QuestionBankCategory;
import org.dromara.exam.question.service.IKnowledgePointService;
import org.dromara.exam.question.service.IQuestionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 试题服务对外实现（供答题服务取题目与判分，供 AI 服务检索与写入）
 *
 * <p>AI 相关的三个方法（listBanks / createBank / saveQuestions）刻意放在这里而不是
 * 让 AI 服务直连数据库：题库、选项、知识点关联这套写入规则只有一份实现，
 * 换个调用方不会长出第二套「保存试题」的逻辑。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteQuestionServiceImpl implements RemoteQuestionService {

    /** Dubbo 调用没有登录上下文时的兜底创建人（admin） */
    private static final Long DEFAULT_CREATOR_ID = 1L;

    private final QuestionMapper questionMapper;

    private final QuestionOptionMapper questionOptionMapper;

    private final QuestionBankMapper questionBankMapper;

    private final QuestionBankCategoryMapper questionBankCategoryMapper;

    private final KnowledgePointMapper knowledgePointMapper;

    private final IQuestionService questionService;

    private final IKnowledgePointService knowledgePointService;

    /**
     * 按试题ID批量查询题目，顺序与入参保持一致
     */
    @Override
    public List<RemoteQuestionVo> listByIds(Collection<Long> questionIds) {
        if (CollUtil.isEmpty(questionIds)) {
            return List.of();
        }
        List<Long> ids = questionIds.stream().filter(id -> id != null).distinct().toList();
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        List<Question> questions = questionMapper.selectList(
            Wrappers.lambdaQuery(Question.class).in(Question::getId, ids));
        if (CollUtil.isEmpty(questions)) {
            return List.of();
        }
        // 一次查回所有选项，避免在循环里逐题查库
        List<QuestionOption> options = questionOptionMapper.selectList(
            Wrappers.lambdaQuery(QuestionOption.class)
                .in(QuestionOption::getQuestionId, ids)
                .orderByAsc(QuestionOption::getSort)
                .orderByAsc(QuestionOption::getId));
        Map<Long, List<QuestionOption>> optionMap = options.stream()
            .collect(Collectors.groupingBy(QuestionOption::getQuestionId));

        List<RemoteQuestionVo> vos = new ArrayList<>(questions.size());
        for (Question question : questions) {
            RemoteQuestionVo vo = MapstructUtils.convert(question, RemoteQuestionVo.class);
            if (vo == null) {
                continue;
            }
            vo.setQuestionId(question.getId());
            List<QuestionOption> own = optionMap.get(question.getId());
            if (CollUtil.isNotEmpty(own)) {
                List<RemoteQuestionOptionVo> optionVos = own.stream()
                    .map(item -> {
                        RemoteQuestionOptionVo optionVo = new RemoteQuestionOptionVo();
                        optionVo.setOptionKey(item.getOptionKey());
                        optionVo.setOptionContent(item.getOptionContent());
                        optionVo.setSort(item.getSort());
                        return optionVo;
                    })
                    .sorted(Comparator.comparing(RemoteQuestionOptionVo::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
                vo.setOptions(optionVos);
            }
            vos.add(vo);
        }
        // 按传入顺序还原，方便答题页直接按试卷顺序渲染
        Map<Long, RemoteQuestionVo> voMap = vos.stream().collect(Collectors.toMap(RemoteQuestionVo::getQuestionId, item -> item, (a, b) -> a));
        return ids.stream().map(voMap::get).filter(item -> item != null).toList();
    }

    /**
     * 按名称关键词模糊查询题库（带题目数量）
     */
    @Override
    public List<RemoteQuestionBankVo> listBanks(String keyword, Integer limit, String tenantId) {
        // Dubbo 调用没有登录上下文，租户隔离靠调用方传入的 tenantId 临时切租户；
        // 不传就会查出所有租户的题库，用户在自己的库里看不到题就是这么来的。
        if (StringUtils.isBlank(tenantId)) {
            return List.of();
        }
        return TenantHelper.dynamic(tenantId, () -> doListBanks(keyword, limit));
    }

    private List<RemoteQuestionBankVo> doListBanks(String keyword, Integer limit) {
        int size = (limit == null || limit < 1) ? 20 : Math.min(limit, 100);
        List<QuestionBank> banks = questionBankMapper.selectList(
            Wrappers.lambdaQuery(QuestionBank.class)
                .like(StringUtils.isNotBlank(keyword), QuestionBank::getBankName, StringUtils.trimToEmpty(keyword))
                .orderByAsc(QuestionBank::getId)
                .last("limit " + size));
        if (CollUtil.isEmpty(banks)) {
            return List.of();
        }
        Map<Long, Long> countMap = countByBank();
        Map<Long, String> categoryMap = categoryNames();
        return banks.stream().map(bank -> {
            RemoteQuestionBankVo vo = new RemoteQuestionBankVo();
            vo.setId(bank.getId());
            vo.setName(bank.getBankName());
            vo.setQuestionCount(countMap.getOrDefault(bank.getId(), 0L));
            vo.setCategoryName(categoryMap.get(bank.getCategoryId()));
            return vo;
        }).toList();
    }

    /**
     * 新建题库：AI 对话里「这个库不存在，帮我建一个」时走这里
     *
     * <p>不走 QuestionBankService.insertByBo 是因为它取的是当前登录用户，
     * 而 Dubbo 调用没有登录上下文；这里直接落库并允许调用方指定创建人。
     */
    @Override
    public Long createBank(String bankName, String tenantId) {
        if (StringUtils.isBlank(bankName) || StringUtils.isBlank(tenantId)) {
            return null;
        }
        return TenantHelper.dynamic(tenantId, () -> doCreateBank(bankName));
    }

    private Long doCreateBank(String bankName) {
        QuestionBank bank = new QuestionBank();
        bank.setBankName(bankName.trim());
        bank.setBankDesc("AI 助手自动创建");
        bank.setStatus("1");
        bank.setVisibility("private");
        bank.setDelFlag(0L);
        // creator_id 在表里是 NOT NULL 且没有默认值，Dubbo 调用又拿不到登录上下文，
        // 不显式赋值会直接踩「Column 'creator_id' cannot be null」，
        // 表现到 Agent 侧就是一句没头没脑的「RPC异常」。
        bank.setCreatorId(currentUserId());
        try {
            if (questionBankMapper.insert(bank) > 0) {
                return bank.getId();
            }
        } catch (Exception e) {
            // 兜住并留栈：Dubbo 侧的统一异常处理只会回一句「RPC异常」，
            // 没有这行日志就只能靠猜
            log.error("AI 新建题库失败，bankName={}", bankName, e);
        }
        return null;
    }

    /** 当前用户 ID；Dubbo 调用没有登录上下文，取不到就落到默认管理员 */
    private Long currentUserId() {
        try {
            Long userId = LoginHelper.getUserId();
            return ObjectUtil.isNotNull(userId) ? userId : DEFAULT_CREATOR_ID;
        } catch (Exception e) {
            return DEFAULT_CREATOR_ID;
        }
    }

    /**
     * 按条件检索试题
     */
    @Override
    public List<RemoteQuestionVo> searchQuestions(RemoteQuestionSearchBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getTenantId())) {
            return List.of();
        }
        return TenantHelper.dynamic(bo.getTenantId(), () -> doSearchQuestions(bo));
    }

    private List<RemoteQuestionVo> doSearchQuestions(RemoteQuestionSearchBo bo) {
        int size = (bo.getLimit() == null || bo.getLimit() < 1) ? 20 : Math.min(bo.getLimit(), 100);
        List<Question> questions = questionMapper.selectList(
            Wrappers.lambdaQuery(Question.class)
                .like(StringUtils.isNotBlank(bo.getKeyword()), Question::getTitle, StringUtils.trimToEmpty(bo.getKeyword()))
                .eq(ObjectUtil.isNotNull(bo.getBankId()), Question::getBankId, bo.getBankId())
                .eq(StringUtils.isNotBlank(bo.getQuestionType()), Question::getQuestionType, bo.getQuestionType())
                .eq(StringUtils.isNotBlank(bo.getDifficulty()), Question::getDifficulty, bo.getDifficulty())
                .orderByDesc(Question::getId)
                .last("limit " + size));
        if (CollUtil.isEmpty(questions)) {
            return List.of();
        }
        List<Long> ids = questions.stream().map(Question::getId).toList();
        return listByIds(ids);
    }

    /**
     * 批量保存 AI 生成的试题
     *
     * <p>逐题保存而不是拼一条批量 insert：createQuestion 里要写选项、兜底状态、
     * 反推答案，这些规则复用已有的单题入口最稳妥。单题失败不影响其它题。
     */
    @Override
    public List<Long> saveQuestions(RemoteQuestionSaveBo bo) {
        if (bo == null || ObjectUtil.isNull(bo.getBankId()) || CollUtil.isEmpty(bo.getQuestions())) {
            return List.of();
        }
        // 租户隔离靠调用方传入的 tenantId 临时切租户；不传 tenantId 的话
        // 写进去的题 tenant_id 为空，用户在自己的库里根本看不到。
        if (StringUtils.isBlank(bo.getTenantId())) {
            log.warn("AI 试题入库被拒绝：tenantId 为空，bankId={}", bo.getBankId());
            return List.of();
        }
        return TenantHelper.dynamic(bo.getTenantId(), () -> doSaveQuestions(bo));
    }

    private List<Long> doSaveQuestions(RemoteQuestionSaveBo bo) {
        String status = StringUtils.defaultIfBlank(bo.getStatus(), "0");
        // question.create_user 同样是 NOT NULL：调用方没传就落到当前用户 / 管理员，
        // 否则整批试题都会因为一个空字段写不进去
        Long creator = ObjectUtil.isNotNull(bo.getCreateUser()) ? bo.getCreateUser() : currentUserId();
        List<Long> ids = new ArrayList<>(bo.getQuestions().size());
        Throwable last = null;
        for (RemoteQuestionSaveItem item : bo.getQuestions()) {
            if (item == null || StringUtils.isBlank(item.getStem())) {
                continue;
            }
            try {
                Long questionId = saveOne(bo.getBankId(), status, creator, item);
                if (questionId != null) {
                    ids.add(questionId);
                    bindKnowledge(questionId, item.getKnowledgePoints());
                }
            } catch (Exception e) {
                // 生成 10 道里有 1 道字段不合法时，不该让另外 9 道一起丢
                last = e;
                log.warn("AI 试题入库失败 bankId={} stem={} err={}", bo.getBankId(),
                    StringUtils.substring(item.getStem(), 0, 30), e.getMessage());
            }
        }
        if (ids.isEmpty() && last != null) {
            // 整批失败时必须留一条带堆栈的日志：Dubbo 侧只会回一句「RPC异常」，
            // 而 Agent 拿到的 ids 是空的，没有这行就只能靠猜
            log.error("AI 试题入库全部失败 bankId={} 共 {} 道", bo.getBankId(), bo.getQuestions().size(), last);
        }
        return ids;
    }

    private Long saveOne(Long bankId, String status, Long createUser, RemoteQuestionSaveItem item) {
        QuestionBo questionBo = new QuestionBo();
        questionBo.setBankId(bankId);
        questionBo.setTitle(item.getStem().trim());
        questionBo.setQuestionType(StringUtils.defaultIfBlank(item.getQuestionType(), "SINGLE"));
        questionBo.setDifficulty(StringUtils.defaultIfBlank(item.getDifficulty(), "medium"));
        questionBo.setScore(item.getScore() == null ? new BigDecimal("5") : item.getScore());
        questionBo.setAnalysis(item.getAnalysis());
        questionBo.setAnswer(item.getAnswer());
        questionBo.setStatus(status);
        questionBo.setCreateUser(createUser);
        List<RemoteQuestionSaveOption> options = item.getOptions();
        if (CollUtil.isNotEmpty(options)) {
            List<QuestionOptionSaveBo> optionBos = new ArrayList<>(options.size());
            long sort = 1;
            for (RemoteQuestionSaveOption option : options) {
                if (option == null || StringUtils.isBlank(option.getKey())) {
                    continue;
                }
                QuestionOptionSaveBo optionBo = new QuestionOptionSaveBo();
                optionBo.setOptionKey(option.getKey().trim());
                optionBo.setOptionContent(option.getContent());
                optionBo.setSort(sort++);
                optionBos.add(optionBo);
            }
            questionBo.setOptions(optionBos);
        }
        return questionService.createQuestion(questionBo);
    }

    /**
     * 按名称精确匹配已存在的知识点并关联
     *
     * <p>匹配不到就跳过，不自动创建：知识点是全局共享的教学资产，
     * 让 AI 随手造节点会把树搞乱，宁可少关联也不要污染。
     */
    private void bindKnowledge(Long questionId, List<String> names) {
        if (CollUtil.isEmpty(names)) {
            return;
        }
        List<String> valid = names.stream()
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(valid)) {
            return;
        }
        List<KnowledgePoint> points = knowledgePointMapper.selectList(
            Wrappers.lambdaQuery(KnowledgePoint.class)
                .in(KnowledgePoint::getName, valid)
                .ne(KnowledgePoint::getParentId, 0L));
        if (CollUtil.isEmpty(points)) {
            return;
        }
        List<Long> knowledgeIds = points.stream().map(KnowledgePoint::getId).distinct().toList();
        knowledgePointService.saveQuestionKnowledge(questionId, knowledgeIds);
    }

    /**
     * 题库 -> 题目数量
     *
     * <p>聚合放在 SQL 里做：把整表试题捞出来在内存里数，数据量一大就是灾难。
     */
    private Map<Long, Long> countByBank() {
        List<Map<String, Object>> rows = questionMapper.selectMaps(
            new QueryWrapper<Question>()
                .select("bank_id AS bankId", "COUNT(1) AS cnt")
                .groupBy("bank_id"));
        Map<Long, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object bankId = row.containsKey("bankId") ? row.get("bankId") : row.get("bank_id");
            Object cnt = row.containsKey("cnt") ? row.get("cnt") : row.get("COUNT(1)");
            if (bankId == null) {
                continue;
            }
            map.put(Long.parseLong(String.valueOf(bankId)), Long.parseLong(String.valueOf(cnt == null ? 0 : cnt)));
        }
        return map;
    }

    /**
     * 分类ID -> 分类名称（一次查回，避免逐库查）
     */
    private Map<Long, String> categoryNames() {
        List<QuestionBankCategory> categories = questionBankCategoryMapper.selectList(
            Wrappers.lambdaQuery(QuestionBankCategory.class).select(QuestionBankCategory::getId, QuestionBankCategory::getCategoryName));
        return categories.stream()
            .filter(c -> c.getId() != null && StringUtils.isNotBlank(c.getCategoryName()))
            .collect(Collectors.toMap(QuestionBankCategory::getId, QuestionBankCategory::getCategoryName, (a, b) -> a));
    }
}
