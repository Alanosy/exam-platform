package org.dromara.exam.paper.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.exam.paper.domain.PaperQuestion;
import org.dromara.exam.paper.domain.bo.PaperBo;
import org.dromara.exam.paper.domain.bo.PaperQuestionSaveBo;
import org.dromara.exam.paper.domain.vo.PaperQuestionVo;
import org.dromara.exam.paper.domain.vo.PaperVo;
import org.dromara.exam.paper.domain.Paper;
import org.dromara.exam.paper.mapper.PaperMapper;
import org.dromara.exam.paper.mapper.PaperQuestionMapper;
import org.dromara.exam.paper.service.IPaperService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * 试卷主Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PaperServiceImpl implements IPaperService {

    private final PaperMapper baseMapper;

    private final PaperQuestionMapper paperQuestionMapper;

    /**
     * 查询试卷主
     *
     * @param id 主键
     * @return 试卷主（含已选试题明细）
     */
    @Override
    public PaperVo queryById(Long id){
        PaperVo vo = baseMapper.selectVoById(id);
        if (ObjectUtil.isNotNull(vo)) {
            vo.setQuestions(queryQuestions(id));
        }
        return vo;
    }

    /**
     * 查询试卷已选试题明细，按 sort 升序
     */
    @Override
    public List<PaperQuestionVo> queryQuestions(Long paperId) {
        LambdaQueryWrapper<PaperQuestion> lqw = Wrappers.lambdaQuery();
        lqw.eq(PaperQuestion::getPaperId, paperId);
        lqw.orderByAsc(PaperQuestion::getSort);
        return paperQuestionMapper.selectVoList(lqw);
    }

    /**
     * 分页查询试卷主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试卷主分页列表
     */
    @Override
    public TableDataInfo<PaperVo> queryPageList(PaperBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Paper> lqw = buildQueryWrapper(bo);
        Page<PaperVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的试卷主列表
     *
     * @param bo 查询条件
     * @return 试卷主列表
     */
    @Override
    public List<PaperVo> queryList(PaperBo bo) {
        LambdaQueryWrapper<Paper> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Paper> buildQueryWrapper(PaperBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Paper> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Paper::getId);
        lqw.like(StringUtils.isNotBlank(bo.getPaperName()), Paper::getPaperName, bo.getPaperName());
        // 描述是长文本，按关键词模糊匹配才有意义
        lqw.like(StringUtils.isNotBlank(bo.getPaperDesc()), Paper::getPaperDesc, bo.getPaperDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getPaperType()), Paper::getPaperType, bo.getPaperType());
        lqw.eq(bo.getTotalScore() != null, Paper::getTotalScore, bo.getTotalScore());
        lqw.eq(bo.getPassScore() != null, Paper::getPassScore, bo.getPassScore());
        lqw.eq(StringUtils.isNotBlank(bo.getVisibility()), Paper::getVisibility, bo.getVisibility());
        lqw.eq(StringUtils.isNotBlank(bo.getSharePassword()), Paper::getSharePassword, bo.getSharePassword());
        lqw.eq(StringUtils.isNotBlank(bo.getRandomRule()), Paper::getRandomRule, bo.getRandomRule());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Paper::getStatus, bo.getStatus());
        lqw.eq(bo.getCreatorId() != null, Paper::getCreatorId, bo.getCreatorId());
        return lqw;
    }

    /**
     * 新增试卷主
     *
     * @param bo 试卷主
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(PaperBo bo) {
        Paper add = MapstructUtils.convert(bo, Paper.class);
        // 创建人未传时取当前登录用户
        if (ObjectUtil.isNull(add.getCreatorId())) {
            add.setCreatorId(LoginHelper.getUserId());
        }
        fillDefaultConfig(add);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改试卷主
     *
     * @param bo 试卷主
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(PaperBo bo) {
        Paper update = MapstructUtils.convert(bo, Paper.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 组卷保存：试卷主表 + 试题明细一次提交，同一事务内完成
     *
     * <p>试题明细采用「全量覆盖」策略：先删除该试卷原有的 paper_question，
     * 再按前端传入的顺序重新写入，避免逐条 diff 产生的脏数据。
     * 分值未单独指定时回落到试卷的默认单题分值。
     *
     * @param bo 试卷信息（含试题明细）
     * @return 试卷主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveWithQuestions(PaperBo bo) {
        if (StringUtils.isBlank(bo.getPaperName())) {
            throw new ServiceException("试卷名称不能为空");
        }
        List<PaperQuestionSaveBo> questions = bo.getQuestions() == null ? new ArrayList<>() : bo.getQuestions();
        Set<Long> questionIds = new LinkedHashSet<>();
        Map<Long, Long> scoreMap = new LinkedHashMap<>();
        for (PaperQuestionSaveBo item : questions) {
            if (ObjectUtil.isNull(item) || ObjectUtil.isNull(item.getQuestionId())) {
                throw new ServiceException("试题ID不能为空");
            }
            // 同一道题重复提交时只保留一份（LinkedHashSet 保持顺序），分值以最后一次为准
            questionIds.add(item.getQuestionId());
            scoreMap.put(item.getQuestionId(), item.getPaperScore());
        }

        Paper paper = MapstructUtils.convert(bo, Paper.class);
        fillDefaultConfig(paper);

        if (ObjectUtil.isNull(paper.getId())) {
            // 创建人固定为当前登录用户，后续他人编辑也不覆盖
            if (ObjectUtil.isNull(paper.getCreatorId())) {
                paper.setCreatorId(LoginHelper.getUserId());
            }
            if (StringUtils.isBlank(paper.getStatus())) {
                paper.setStatus("draft");
            }
            validEntityBeforeSave(paper);
            if (baseMapper.insert(paper) <= 0) {
                throw new ServiceException("新增试卷失败");
            }
        } else {
            Paper exist = baseMapper.selectById(paper.getId());
            if (ObjectUtil.isNull(exist)) {
                throw new ServiceException("试卷不存在或已删除");
            }
            validEntityBeforeSave(paper);
            if (baseMapper.updateById(paper) <= 0) {
                throw new ServiceException("修改试卷失败");
            }
        }
        bo.setId(paper.getId());
        saveQuestions(paper.getId(), questionIds, scoreMap, paper.getDefaultScore());
        return paper.getId();
    }

    /**
     * 全量重写试卷的试题明细
     */
    private void saveQuestions(Long paperId, Set<Long> questionIds, Map<Long, Long> scoreMap, Long defaultScore) {
        LambdaQueryWrapper<PaperQuestion> del = Wrappers.lambdaQuery();
        del.eq(PaperQuestion::getPaperId, paperId);
        paperQuestionMapper.delete(del);

        long sort = 1;
        for (Long questionId : questionIds) {
            Long score = scoreMap.get(questionId);
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(questionId);
            pq.setPaperScore(score != null ? score : defaultScore);
            pq.setSort(sort++);
            paperQuestionMapper.insert(pq);
        }
    }

    /**
     * 补齐组卷配置项的默认值，避免前端漏传时数据库里出现 null
     */
    private void fillDefaultConfig(Paper paper) {
        if (StringUtils.isBlank(paper.getPaperType())) {
            paper.setPaperType("MANUAL");
        }
        if (StringUtils.isBlank(paper.getVisibility())) {
            paper.setVisibility("private");
        }
        if (StringUtils.isBlank(paper.getShareScope())) {
            paper.setShareScope("SELF");
        }
        if (StringUtils.isBlank(paper.getQuestionShuffle())) {
            paper.setQuestionShuffle("0");
        }
        if (StringUtils.isBlank(paper.getOptionShuffle())) {
            paper.setOptionShuffle("0");
        }
        if (StringUtils.isBlank(paper.getAutoJudge())) {
            paper.setAutoJudge("1");
        }
        if (StringUtils.isBlank(paper.getManualReview())) {
            paper.setManualReview("1");
        }
        if (StringUtils.isBlank(paper.getPartialScore())) {
            paper.setPartialScore("0");
        }
        if (StringUtils.isBlank(paper.getWrongDeduct())) {
            paper.setWrongDeduct("0");
        }
        if (ObjectUtil.isNull(paper.getDefaultScore())) {
            paper.setDefaultScore(0L);
        }
        if (ObjectUtil.isNull(paper.getTotalScore())) {
            paper.setTotalScore(0L);
        }
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Paper entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除试卷主信息
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
