package org.dromara.exam.question.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.exam.question.domain.bo.QuestionBo;
import org.dromara.exam.question.domain.bo.QuestionRandomBo;
import org.dromara.exam.question.domain.bo.QuestionOptionSaveBo;
import org.dromara.exam.question.domain.vo.KnowledgePointVo;
import org.dromara.exam.question.domain.vo.QuestionVo;
import org.dromara.exam.question.service.IKnowledgePointService;
import org.dromara.exam.question.domain.Question;
import org.dromara.exam.question.domain.QuestionBank;
import org.dromara.exam.question.domain.QuestionOption;
import org.dromara.exam.question.mapper.QuestionMapper;
import org.dromara.exam.question.mapper.QuestionBankMapper;
import org.dromara.exam.question.mapper.QuestionOptionMapper;
import org.dromara.exam.question.service.IQuestionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;

/**
 * 试题主Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionServiceImpl implements IQuestionService {

    private final QuestionMapper baseMapper;

    private final QuestionOptionMapper questionOptionMapper;

    private final QuestionBankMapper questionBankMapper;

    private final IKnowledgePointService knowledgePointService;

    /**
     * 查询试题主
     *
     * @param id 主键
     * @return 试题主
     */
    @Override
    public QuestionVo queryById(Long id){
        QuestionVo vo = baseMapper.selectVoById(id);
        if (ObjectUtil.isNull(vo)) {
            return null;
        }
        // 选项随详情一起返回，避免编辑页再单独查一次选项列表
        LambdaQueryWrapper<QuestionOption> lqw = Wrappers.lambdaQuery();
        lqw.eq(QuestionOption::getQuestionId, id);
        lqw.orderByAsc(QuestionOption::getSort);
        vo.setOptions(questionOptionMapper.selectVoList(lqw));
        // 知识点随详情一起返回，编辑页直接回显，不用再单独查一次
        List<KnowledgePointVo> points = knowledgePointService.mapByQuestionIds(List.of(id)).get(id);
        if (CollUtil.isNotEmpty(points)) {
            vo.setKnowledgeIds(points.stream().map(KnowledgePointVo::getId).toList());
            vo.setKnowledgeNames(points.stream().map(KnowledgePointVo::getName).toList());
        }
        return vo;
    }

    /**
     * 分页查询试题主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题主分页列表
     */
    @Override
    public TableDataInfo<QuestionVo> queryPageList(QuestionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Question> lqw = buildQueryWrapper(bo);
        Page<QuestionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        fillKnowledge(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的试题主列表
     *
     * @param bo 查询条件
     * @return 试题主列表
     */
    @Override
    public List<QuestionVo> queryList(QuestionBo bo) {
        LambdaQueryWrapper<Question> lqw = buildQueryWrapper(bo);
        List<QuestionVo> rows = baseMapper.selectVoList(lqw);
        fillKnowledge(rows);
        return rows;
    }

    /**
     * 批量回填知识点
     *
     * <p>列表页要显示每题挂了哪些知识点，这里按页内试题ID一次性查回来再拼，
     * 避免 N+1（一页 10 道题只多一次查询）。
     *
     * @param rows 试题VO列表
     */
    private void fillKnowledge(List<QuestionVo> rows) {
        if (CollUtil.isEmpty(rows)) {
            return;
        }
        List<Long> ids = rows.stream().map(QuestionVo::getId).filter(ObjectUtil::isNotNull).toList();
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        Map<Long, List<KnowledgePointVo>> map = knowledgePointService.mapByQuestionIds(ids);
        for (QuestionVo row : rows) {
            List<KnowledgePointVo> points = map.get(row.getId());
            if (CollUtil.isEmpty(points)) {
                continue;
            }
            row.setKnowledgeIds(points.stream().map(KnowledgePointVo::getId).toList());
            row.setKnowledgeNames(points.stream().map(KnowledgePointVo::getName).toList());
        }
    }

    private LambdaQueryWrapper<Question> buildQueryWrapper(QuestionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Question> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Question::getId);
        lqw.eq(bo.getBankId() != null, Question::getBankId, bo.getBankId());
        // 题干是富文本，前端按关键词检索，这里统一走模糊匹配
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), Question::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getQuestionType()), Question::getQuestionType, bo.getQuestionType());
        lqw.eq(StringUtils.isNotBlank(bo.getDifficulty()), Question::getDifficulty, bo.getDifficulty());
        lqw.eq(bo.getScore() != null, Question::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getAnalysis()), Question::getAnalysis, bo.getAnalysis());
        lqw.eq(StringUtils.isNotBlank(bo.getAnswer()), Question::getAnswer, bo.getAnswer());
        lqw.eq(bo.getCreateUser() != null, Question::getCreateUser, bo.getCreateUser());
        lqw.eq(bo.getStatus() != null, Question::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增试题主
     *
     * @param bo 试题主
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(QuestionBo bo) {
        Question add = MapstructUtils.convert(bo, Question.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 新增试题（含选项）
     *
     * @param bo 试题主（含选项）
     * @return 新建试题的主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(QuestionBo bo) {
        // 创建人未传时取当前登录用户
        if (ObjectUtil.isNull(bo.getCreateUser())) {
            bo.setCreateUser(LoginHelper.getUserId());
        }
        if (ObjectUtil.isNull(bo.getStatus())) {
            bo.setStatus("enabled");
        }
        // 客观题选项校验，答案为空时按选项勾选反推正确答案
        fillAnswerByOptions(bo);

        Question add = MapstructUtils.convert(bo, Question.class);
        validEntityBeforeSave(add);
        if (baseMapper.insert(add) <= 0) {
            throw new ServiceException("新增试题失败");
        }
        bo.setId(add.getId());
        saveOptions(add.getId(), bo.getOptions());
        // 知识点传了才写，避免局部接口误清空
        if (bo.getKnowledgeIds() != null) {
            knowledgePointService.saveQuestionKnowledge(add.getId(), bo.getKnowledgeIds());
        }
        // 富文本里的图片已经以 <img src> 的形式存在 title / options / analysis 里，随本行一起入库
        return add.getId();
    }

    /**
     * 校验选项并在答案为空时反推正确答案
     *
     * <p>question_option 表不存「是否正确答案」，正确答案统一落在 question.answer，
     * 因此前端未传 answer 时，这里按选项的 isRight 拼出 {@code {"rightKeys":["A"]}}。
     */
    private void fillAnswerByOptions(QuestionBo bo) {
        List<QuestionOptionSaveBo> options = bo.getOptions();
        if (CollUtil.isEmpty(options)) {
            // 简答、论述、填空等非客观题没有选项，直接放行
            return;
        }
        if (options.size() < 2) {
            throw new ServiceException("客观题至少需要两个选项");
        }
        Set<String> keys = new HashSet<>();
        for (QuestionOptionSaveBo option : options) {
            if (StringUtils.isBlank(option.getOptionKey())) {
                throw new ServiceException("选项标识不能为空");
            }
            if (!keys.add(option.getOptionKey())) {
                throw new ServiceException("选项标识重复：" + option.getOptionKey());
            }
            if (StringUtils.isBlank(option.getOptionContent())) {
                throw new ServiceException("选项 " + option.getOptionKey() + " 的内容不能为空");
            }
        }
        if (StringUtils.isNotBlank(bo.getAnswer())) {
            return;
        }
        List<String> rightKeys = new ArrayList<>();
        for (QuestionOptionSaveBo option : options) {
            if (Boolean.TRUE.equals(option.getIsRight())) {
                rightKeys.add(option.getOptionKey());
            }
        }
        String questionType = bo.getQuestionType();
        if (rightKeys.isEmpty()) {
            throw new ServiceException("请设置正确答案");
        }
        boolean single = "SINGLE".equals(questionType) || "JUDGE".equals(questionType);
        if (single && rightKeys.size() > 1) {
            throw new ServiceException("单选题只能有一个正确答案");
        }
        JSONObject answer = JSONUtil.createObj();
        answer.set("rightKeys", rightKeys);
        bo.setAnswer(answer.toString());
    }

    /**
     * 批量写入试题选项
     *
     * @param questionId 试题ID
     * @param options    选项列表
     */
    private void saveOptions(Long questionId, List<QuestionOptionSaveBo> options) {
        if (CollUtil.isEmpty(options)) {
            return;
        }
        int index = 1;
        for (QuestionOptionSaveBo bo : options) {
            QuestionOption entity = new QuestionOption();
            entity.setQuestionId(questionId);
            entity.setOptionKey(bo.getOptionKey());
            entity.setOptionContent(bo.getOptionContent());
            entity.setSort(ObjectUtil.isNotNull(bo.getSort()) ? bo.getSort() : (long) index);
            questionOptionMapper.insert(entity);
            index++;
        }
    }

    /**
     * 全量替换试题选项
     *
     * <p>编辑页会把当前所有选项一次性提交上来，旧的选项先逻辑删除再按新列表写入，
     * 这样增、删、改、拖动排序都能体现在一次保存里。
     *
     * @param questionId 试题ID
     * @param options    新的选项列表
     */
    private void replaceOptions(Long questionId, List<QuestionOptionSaveBo> options) {
        LambdaQueryWrapper<QuestionOption> lqw = Wrappers.lambdaQuery();
        lqw.eq(QuestionOption::getQuestionId, questionId);
        // QuestionOption 带 @TableLogic，这里会转成逻辑删除
        questionOptionMapper.delete(lqw);
        saveOptions(questionId, options);
    }

    /**
     * 修改试题主
     *
     * <p>选项随试题一并全量更新：前端提交的是当前完整列表，旧的会被逻辑删除。
     * 富文本里的图片地址就在各字段的 HTML 里，跟着一起更新，无需额外处理。
     *
     * @param bo 试题主
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(QuestionBo bo) {
        Question update = MapstructUtils.convert(bo, Question.class);
        validEntityBeforeSave(update);
        boolean flag = baseMapper.updateById(update) > 0;
        if (!flag) {
            return false;
        }
        // 选项传了才更新，避免局部更新接口误清空
        if (bo.getOptions() != null) {
            replaceOptions(bo.getId(), bo.getOptions());
        }
        // 知识点同理：编辑页提交的是完整列表，走全量覆盖
        if (bo.getKnowledgeIds() != null) {
            knowledgePointService.saveQuestionKnowledge(bo.getId(), bo.getKnowledgeIds());
        }
        return true;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Question entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除试题主信息
     *
     * <p>级联逻辑：主表、选项表统一逻辑删除。富文本里的图片地址随 HTML 一起被删掉，
     * 桶里的文件不再级联清理。
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        if (!flag) {
            return false;
        }
        // 级联逻辑删除选项
        LambdaQueryWrapper<QuestionOption> optionLqw = Wrappers.lambdaQuery();
        optionLqw.in(QuestionOption::getQuestionId, ids);
        questionOptionMapper.delete(optionLqw);
        // 知识点关联没有逻辑删除字段，直接清掉，避免留下悬空关联
        knowledgePointService.deleteByQuestionIds(ids);
        return true;
    }

    /**
     * 批量切换试题所属题库
     *
     * @param ids    试题主键集合
     * @param bankId 目标题库ID
     * @return 是否移动成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBank(Collection<Long> ids, Long bankId) {
        if (CollUtil.isEmpty(ids)) {
            throw new ServiceException("请选择需要移动的试题");
        }
        if (ObjectUtil.isNull(bankId)) {
            throw new ServiceException("请选择目标题库");
        }
        QuestionBank bank = questionBankMapper.selectById(bankId);
        if (ObjectUtil.isNull(bank)) {
            throw new ServiceException("目标题库不存在");
        }
        LambdaUpdateWrapper<Question> lqw = Wrappers.lambdaUpdate();
        lqw.in(Question::getId, ids);
        lqw.set(Question::getBankId, bankId);
        return baseMapper.update(null, lqw) > 0;
    }

    /**
     * 按ID批量查询试题，保持传入顺序
     */
    @Override
    public List<QuestionVo> queryByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Question> lqw = Wrappers.lambdaQuery();
        lqw.in(Question::getId, ids);
        List<QuestionVo> list = baseMapper.selectVoList(lqw);
        // selectVoList 的 in 查询不保证顺序，这里按传入的 ids 重排，保证组卷回显顺序稳定
        Map<Long, Integer> order = new HashMap<>();
        int index = 0;
        for (Long id : new LinkedHashSet<>(ids)) {
            order.put(id, index++);
        }
        list.sort(Comparator.comparingInt(vo -> order.getOrDefault(vo.getId(), Integer.MAX_VALUE)));
        return list;
    }

    /**
     * 随机抽题
     *
     * <p>先按条件查出候选题（只取启用状态的题），再打乱顺序取前 count 条。
     * 候选数量不足 count 时返回全部候选题，由前端提示「可选题不足」。
     */
    @Override
    public List<QuestionVo> randomQuestions(QuestionRandomBo bo) {
        int count = bo.getCount() == null || bo.getCount() <= 0 ? 10 : Math.min(bo.getCount(), 500);

        LambdaQueryWrapper<Question> lqw = Wrappers.lambdaQuery();
        lqw.eq(ObjectUtil.isNotNull(bo.getBankId()), Question::getBankId, bo.getBankId());
        lqw.eq(StringUtils.isNotBlank(bo.getQuestionType()), Question::getQuestionType, bo.getQuestionType());
        lqw.eq(StringUtils.isNotBlank(bo.getDifficulty()), Question::getDifficulty, bo.getDifficulty());
        // 只抽启用状态的题：历史数据里启用可能是 "1" 也可能是 "enabled"，这里两种都认
        lqw.in(Question::getStatus, List.of("1", "enabled"));
        if (CollUtil.isNotEmpty(bo.getExcludeIds())) {
            lqw.notIn(Question::getId, bo.getExcludeIds());
        }
        List<Question> candidates = baseMapper.selectList(lqw);
        if (CollUtil.isEmpty(candidates)) {
            return new ArrayList<>();
        }
        Collections.shuffle(candidates);
        List<Long> picked = candidates.stream().limit(count).map(Question::getId).toList();

        List<QuestionVo> vos = baseMapper.selectVoList(Wrappers.<Question>lambdaQuery().in(Question::getId, picked));
        // 保持打乱后的顺序，避免二次查询把顺序洗回 id 升序
        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < picked.size(); i++) {
            order.put(picked.get(i), i);
        }
        vos.sort(Comparator.comparingInt(vo -> order.getOrDefault(vo.getId(), Integer.MAX_VALUE)));
        return vos;
    }
}
