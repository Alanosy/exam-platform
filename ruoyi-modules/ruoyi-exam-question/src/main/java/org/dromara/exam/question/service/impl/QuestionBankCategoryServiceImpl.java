package org.dromara.exam.question.service.impl;

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
import org.dromara.exam.question.domain.bo.QuestionBankCategoryBo;
import org.dromara.exam.question.domain.vo.QuestionBankCategoryVo;
import org.dromara.exam.question.domain.QuestionBankCategory;
import org.dromara.exam.question.domain.QuestionBank;
import org.dromara.exam.question.mapper.QuestionBankCategoryMapper;
import org.dromara.exam.question.mapper.QuestionBankMapper;
import org.dromara.exam.question.service.IQuestionBankCategoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 题库分类目录Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionBankCategoryServiceImpl implements IQuestionBankCategoryService {

    private final QuestionBankCategoryMapper baseMapper;

    private final QuestionBankMapper questionBankMapper;

    /**
     * 查询题库分类目录
     *
     * @param id 主键
     * @return 题库分类目录
     */
    @Override
    public QuestionBankCategoryVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询题库分类目录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题库分类目录分页列表
     */
    @Override
    public TableDataInfo<QuestionBankCategoryVo> queryPageList(QuestionBankCategoryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<QuestionBankCategory> lqw = buildQueryWrapper(bo);
        Page<QuestionBankCategoryVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的题库分类目录列表
     *
     * @param bo 查询条件
     * @return 题库分类目录列表
     */
    @Override
    public List<QuestionBankCategoryVo> queryList(QuestionBankCategoryBo bo) {
        LambdaQueryWrapper<QuestionBankCategory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询分类树（全量，供前端树表格与树选择使用）
     *
     * @param bo 查询条件
     * @return 分类列表（平铺，前端自行构树）
     */
    @Override
    public List<QuestionBankCategoryVo> queryTreeList(QuestionBankCategoryBo bo) {
        if (ObjectUtil.isNull(bo)) {
            bo = new QuestionBankCategoryBo();
        }
        // 树只展示未删除的分类
        bo.setIsDeleted(0L);
        return queryList(bo);
    }

    private LambdaQueryWrapper<QuestionBankCategory> buildQueryWrapper(QuestionBankCategoryBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<QuestionBankCategory> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(QuestionBankCategory::getSort);
        lqw.orderByAsc(QuestionBankCategory::getId);
        lqw.eq(bo.getParentId() != null, QuestionBankCategory::getParentId, bo.getParentId());
        lqw.like(StringUtils.isNotBlank(bo.getCategoryName()), QuestionBankCategory::getCategoryName, bo.getCategoryName());
        lqw.eq(bo.getSort() != null, QuestionBankCategory::getSort, bo.getSort());
        lqw.eq(bo.getIsDeleted() != null, QuestionBankCategory::getIsDeleted, bo.getIsDeleted());
        return lqw;
    }

    /**
     * 新增题库分类目录
     *
     * @param bo 题库分类目录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(QuestionBankCategoryBo bo) {
        // 根节点 parentId 为 0
        if (ObjectUtil.isNull(bo.getParentId())) {
            bo.setParentId(0L);
        }
        if (ObjectUtil.isNull(bo.getSort())) {
            bo.setSort(0L);
        }
        bo.setIsDeleted(0L);
        validParent(bo.getParentId(), null);
        QuestionBankCategory add = MapstructUtils.convert(bo, QuestionBankCategory.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改题库分类目录
     *
     * @param bo 题库分类目录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(QuestionBankCategoryBo bo) {
        if (ObjectUtil.isNull(bo.getParentId())) {
            bo.setParentId(0L);
        }
        validParent(bo.getParentId(), bo.getId());
        QuestionBankCategory update = MapstructUtils.convert(bo, QuestionBankCategory.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 校验上级分类：不能挂到自己或其子孙下面，且上级必须存在
     */
    private void validParent(Long parentId, Long selfId) {
        if (ObjectUtil.isNull(parentId) || parentId == 0L) {
            return;
        }
        if (ObjectUtil.isNotNull(selfId) && parentId.equals(selfId)) {
            throw new ServiceException("上级分类不能是自己");
        }
        QuestionBankCategory parent = baseMapper.selectById(parentId);
        if (ObjectUtil.isNull(parent) || ObjectUtil.equal(1L, parent.getIsDeleted())) {
            throw new ServiceException("上级分类不存在");
        }
        if (ObjectUtil.isNull(selfId)) {
            return;
        }
        // 不能移动到自己的子孙节点下，否则会形成环
        List<QuestionBankCategory> all = baseMapper.selectList(
            Wrappers.lambdaQuery(QuestionBankCategory.class).eq(QuestionBankCategory::getIsDeleted, 0L));
        List<Long> children = new ArrayList<>();
        collectChildren(all, selfId, children);
        if (children.contains(parentId)) {
            throw new ServiceException("上级分类不能是自己的子分类");
        }
    }

    private void collectChildren(List<QuestionBankCategory> all, Long parentId, List<Long> result) {
        for (QuestionBankCategory category : all) {
            if (parentId.equals(category.getParentId())) {
                result.add(category.getId());
                collectChildren(all, category.getId(), result);
            }
        }
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(QuestionBankCategory entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除题库分类目录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            for (Long id : ids) {
                // 存在子分类不允许删除
                Long childCount = baseMapper.selectCount(
                    Wrappers.lambdaQuery(QuestionBankCategory.class)
                        .eq(QuestionBankCategory::getParentId, id)
                        .eq(QuestionBankCategory::getIsDeleted, 0L));
                if (childCount > 0) {
                    throw new ServiceException("该分类下存在子分类，不允许删除");
                }
                // 已被题库引用不允许删除
                Long bankCount = questionBankMapper.selectCount(
                    Wrappers.lambdaQuery(QuestionBank.class).eq(QuestionBank::getCategoryId, id));
                if (bankCount > 0) {
                    throw new ServiceException("该分类下存在题库，不允许删除");
                }
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
