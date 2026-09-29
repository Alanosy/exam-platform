package org.dromara.exam.question.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.dromara.exam.question.domain.bo.QuestionOptionSaveBo;
import org.dromara.exam.question.domain.vo.QuestionVo;
import org.dromara.exam.question.domain.Question;
import org.dromara.exam.question.domain.QuestionOption;
import org.dromara.exam.question.mapper.QuestionMapper;
import org.dromara.exam.question.mapper.QuestionOptionMapper;
import org.dromara.exam.question.service.IQuestionService;

import java.util.ArrayList;
import java.util.HashSet;
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
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Question> buildQueryWrapper(QuestionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Question> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Question::getId);
        lqw.eq(bo.getBankId() != null, Question::getBankId, bo.getBankId());
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), Question::getTitle, bo.getTitle());
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
            bo.setStatus(1L);
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
     * 修改试题主
     *
     * @param bo 试题主
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(QuestionBo bo) {
        Question update = MapstructUtils.convert(bo, Question.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
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
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
