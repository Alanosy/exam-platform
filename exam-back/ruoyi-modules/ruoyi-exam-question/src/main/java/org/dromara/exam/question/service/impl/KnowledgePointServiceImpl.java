package org.dromara.exam.question.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.exam.question.domain.bo.KnowledgePointBo;
import org.dromara.exam.question.domain.vo.KnowledgePointVo;
import org.dromara.exam.question.domain.vo.QuestionKnowledgeVo;
import org.dromara.exam.question.domain.KnowledgePoint;
import org.dromara.exam.question.domain.QuestionKnowledge;
import org.dromara.exam.question.mapper.KnowledgePointMapper;
import org.dromara.exam.question.mapper.QuestionKnowledgeMapper;
import org.dromara.exam.question.service.IKnowledgePointService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识点Service业务层处理
 *
 * <p>两级树：parentId = 0 是章节，其下是知识点。删除前会校验有没有子节点、
 * 有没有被试题引用，避免留下悬空关联。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class KnowledgePointServiceImpl implements IKnowledgePointService {

    private final KnowledgePointMapper baseMapper;

    private final QuestionKnowledgeMapper questionKnowledgeMapper;

    /**
     * 查询知识点
     *
     * @param id 主键
     * @return 知识点
     */
    @Override
    public KnowledgePointVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询知识点列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 知识点分页列表
     */
    @Override
    public TableDataInfo<KnowledgePointVo> queryPageList(KnowledgePointBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<KnowledgePoint> lqw = buildQueryWrapper(bo);
        Page<KnowledgePointVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的知识点列表
     *
     * @param bo 查询条件
     * @return 知识点列表
     */
    @Override
    public List<KnowledgePointVo> queryList(KnowledgePointBo bo) {
        LambdaQueryWrapper<KnowledgePoint> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询知识点树
     *
     * @param bo 查询条件
     * @return 根节点列表（children 递归）
     */
    @Override
    public List<KnowledgePointVo> queryTreeList(KnowledgePointBo bo) {
        if (ObjectUtil.isNull(bo)) {
            bo = new KnowledgePointBo();
        }
        // 树只展示未删除的
        bo.setDelFlag(0L);
        List<KnowledgePointVo> all = queryList(bo);
        return buildTree(all, 0L);
    }

    private List<KnowledgePointVo> buildTree(List<KnowledgePointVo> all, Long parentId) {
        List<KnowledgePointVo> children = new ArrayList<>();
        for (KnowledgePointVo node : all) {
            if (ObjectUtil.equal(parentId, node.getParentId())) {
                List<KnowledgePointVo> sub = buildTree(all, node.getId());
                if (CollUtil.isNotEmpty(sub)) {
                    node.setChildren(sub);
                }
                children.add(node);
            }
        }
        return children;
    }

    private LambdaQueryWrapper<KnowledgePoint> buildQueryWrapper(KnowledgePointBo bo) {
        LambdaQueryWrapper<KnowledgePoint> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(KnowledgePoint::getSort);
        lqw.orderByAsc(KnowledgePoint::getId);
        lqw.eq(bo.getParentId() != null, KnowledgePoint::getParentId, bo.getParentId());
        lqw.like(StringUtils.isNotBlank(bo.getName()), KnowledgePoint::getName, bo.getName());
        lqw.eq(bo.getDelFlag() != null, KnowledgePoint::getDelFlag, bo.getDelFlag());
        return lqw;
    }

    /**
     * 新增知识点
     *
     * @param bo 知识点
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(KnowledgePointBo bo) {
        if (ObjectUtil.isNull(bo.getParentId())) {
            bo.setParentId(0L);
        }
        if (ObjectUtil.isNull(bo.getSort())) {
            bo.setSort(0L);
        }
        // 两级：章节下才能挂知识点，知识点下不能再挂子节点
        validParent(bo.getParentId(), null);
        checkNameDuplicate(bo.getParentId(), bo.getName(), null);
        KnowledgePoint add = MapstructUtils.convert(bo, KnowledgePoint.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改知识点
     *
     * @param bo 知识点
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(KnowledgePointBo bo) {
        if (ObjectUtil.isNull(bo.getParentId())) {
            bo.setParentId(0L);
        }
        validParent(bo.getParentId(), bo.getId());
        checkNameDuplicate(bo.getParentId(), bo.getName(), bo.getId());
        KnowledgePoint update = MapstructUtils.convert(bo, KnowledgePoint.class);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 校验上级节点：必须存在，且不能是自己或自己的子孙；同时限制只能两级
     */
    private void validParent(Long parentId, Long selfId) {
        if (ObjectUtil.isNull(parentId) || parentId == 0L) {
            return;
        }
        if (ObjectUtil.isNotNull(selfId) && parentId.equals(selfId)) {
            throw new ServiceException("上级知识点不能是自己");
        }
        KnowledgePoint parent = baseMapper.selectById(parentId);
        if (ObjectUtil.isNull(parent)) {
            throw new ServiceException("上级知识点不存在");
        }
        // 只允许两级：章节（parentId=0）→ 知识点，知识点下不能再挂
        if (parent.getParentId() != null && parent.getParentId() != 0L) {
            throw new ServiceException("知识点只支持两级（章节 → 知识点），不能在知识点下再建子节点");
        }
        if (ObjectUtil.isNull(selfId)) {
            return;
        }
        // 不能移动到自己的子孙节点下，否则会形成环
        List<KnowledgePoint> all = baseMapper.selectList(Wrappers.lambdaQuery());
        List<Long> children = new ArrayList<>();
        collectChildren(all, selfId, children);
        if (children.contains(parentId)) {
            throw new ServiceException("上级知识点不能是自己的子级");
        }
    }

    private void collectChildren(List<KnowledgePoint> all, Long parentId, List<Long> result) {
        for (KnowledgePoint node : all) {
            if (parentId.equals(node.getParentId())) {
                result.add(node.getId());
                collectChildren(all, node.getId(), result);
            }
        }
    }

    /**
     * 同一层级下名称不能重复，否则出题选知识点时会分不清
     */
    private void checkNameDuplicate(Long parentId, String name, Long selfId) {
        if (StringUtils.isBlank(name)) {
            return;
        }
        LambdaQueryWrapper<KnowledgePoint> lqw = Wrappers.lambdaQuery();
        lqw.eq(KnowledgePoint::getParentId, parentId == null ? 0L : parentId);
        lqw.eq(KnowledgePoint::getName, name.trim());
        List<KnowledgePoint> same = baseMapper.selectList(lqw);
        for (KnowledgePoint node : same) {
            if (ObjectUtil.isNull(selfId) || !selfId.equals(node.getId())) {
                throw new ServiceException("同级下已存在同名知识点");
            }
        }
    }

    /**
     * 校验并批量删除知识点信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            for (Long id : ids) {
                Long childCount = baseMapper.selectCount(
                    Wrappers.lambdaQuery(KnowledgePoint.class).eq(KnowledgePoint::getParentId, id));
                if (childCount > 0) {
                    throw new ServiceException("该章节下还有知识点，不允许删除");
                }
                Long refCount = questionKnowledgeMapper.selectCount(
                    Wrappers.lambdaQuery(QuestionKnowledge.class).eq(QuestionKnowledge::getKnowledgeId, id));
                if (refCount > 0) {
                    throw new ServiceException("该知识点已被试题引用，不允许删除");
                }
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 按试题ID批量取知识点
     *
     * @param questionIds 试题ID集合
     * @return 试题ID → 知识点列表
     */
    @Override
    public Map<Long, List<KnowledgePointVo>> mapByQuestionIds(Collection<Long> questionIds) {
        Map<Long, List<KnowledgePointVo>> result = new LinkedHashMap<>();
        if (CollUtil.isEmpty(questionIds)) {
            return result;
        }
        for (QuestionKnowledgeVo ref : listByQuestionIds(questionIds)) {
            KnowledgePointVo vo = new KnowledgePointVo();
            vo.setId(ref.getKnowledgeId());
            vo.setName(ref.getKnowledgeName());
            result.computeIfAbsent(ref.getQuestionId(), k -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    /**
     * 按试题ID批量取关联明细（含知识点名称）
     *
     * @param questionIds 试题ID集合
     * @return 关联明细列表
     */
    @Override
    public List<QuestionKnowledgeVo> listByQuestionIds(Collection<Long> questionIds) {
        if (CollUtil.isEmpty(questionIds)) {
            return List.of();
        }
        List<QuestionKnowledge> refs = questionKnowledgeMapper.selectList(
            Wrappers.lambdaQuery(QuestionKnowledge.class).in(QuestionKnowledge::getQuestionId, questionIds));
        if (CollUtil.isEmpty(refs)) {
            return List.of();
        }
        List<Long> knowledgeIds = refs.stream().map(QuestionKnowledge::getKnowledgeId).distinct().toList();
        Map<Long, String> nameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(knowledgeIds)) {
            for (KnowledgePoint point : baseMapper.selectByIds(knowledgeIds)) {
                nameMap.put(point.getId(), point.getName());
            }
        }
        // 关联表不存排序，这里按知识点ID升序，保证每次回显顺序稳定
        return refs.stream()
            .sorted((a, b) -> a.getKnowledgeId().compareTo(b.getKnowledgeId()))
            .map(ref -> {
                QuestionKnowledgeVo vo = new QuestionKnowledgeVo();
                vo.setQuestionId(ref.getQuestionId());
                vo.setKnowledgeId(ref.getKnowledgeId());
                vo.setKnowledgeName(nameMap.get(ref.getKnowledgeId()));
                return vo;
            })
            .collect(Collectors.toList());
    }

    /**
     * 保存试题的知识点关联（全量覆盖）
     *
     * @param questionId   试题ID
     * @param knowledgeIds 知识点ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionKnowledge(Long questionId, List<Long> knowledgeIds) {
        if (ObjectUtil.isNull(questionId)) {
            return;
        }
        // 先清后插：幂等，编辑时不用比对差异
        questionKnowledgeMapper.delete(
            Wrappers.lambdaQuery(QuestionKnowledge.class).eq(QuestionKnowledge::getQuestionId, questionId));
        if (CollUtil.isEmpty(knowledgeIds)) {
            return;
        }
        for (Long knowledgeId : knowledgeIds.stream().filter(ObjectUtil::isNotNull).distinct().toList()) {
            // 知识点可能已被删除，这里直接跳过，不让一道题因为脏ID存不进去
            if (ObjectUtil.isNull(baseMapper.selectById(knowledgeId))) {
                log.warn("试题 {} 关联了不存在的知识点 {}，已跳过", questionId, knowledgeId);
                continue;
            }
            QuestionKnowledge ref = new QuestionKnowledge();
            ref.setQuestionId(questionId);
            ref.setKnowledgeId(knowledgeId);
            questionKnowledgeMapper.insert(ref);
        }
    }

    /**
     * 清理试题的知识点关联
     *
     * @param questionIds 试题ID集合
     */
    @Override
    public void deleteByQuestionIds(Collection<Long> questionIds) {
        if (CollUtil.isEmpty(questionIds)) {
            return;
        }
        questionKnowledgeMapper.delete(
            Wrappers.lambdaQuery(QuestionKnowledge.class).in(QuestionKnowledge::getQuestionId, questionIds));
    }
}
