package org.dromara.exam.question.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.dromara.exam.question.domain.bo.QuestionBankBo;
import org.dromara.exam.question.domain.vo.QuestionBankVo;
import org.dromara.exam.question.domain.QuestionBank;
import org.dromara.exam.question.domain.QuestionBankCategory;
import org.dromara.exam.question.mapper.QuestionBankMapper;
import org.dromara.exam.question.mapper.QuestionBankCategoryMapper;
import org.dromara.exam.question.service.IQuestionBankService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 题库Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionBankServiceImpl implements IQuestionBankService {

    private final QuestionBankMapper baseMapper;

    private final QuestionBankCategoryMapper questionBankCategoryMapper;

    /**
     * 查询题库
     *
     * @param id 主键
     * @return 题库
     */
    @Override
    public QuestionBankVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询题库列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题库分页列表
     */
    @Override
    public TableDataInfo<QuestionBankVo> queryPageList(QuestionBankBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<QuestionBank> lqw = buildQueryWrapper(bo);
        Page<QuestionBankVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的题库列表
     *
     * @param bo 查询条件
     * @return 题库列表
     */
    @Override
    public List<QuestionBankVo> queryList(QuestionBankBo bo) {
        LambdaQueryWrapper<QuestionBank> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<QuestionBank> buildQueryWrapper(QuestionBankBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<QuestionBank> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(QuestionBank::getId);
        lqw.like(StringUtils.isNotBlank(bo.getBankName()), QuestionBank::getBankName, bo.getBankName());
        lqw.eq(StringUtils.isNotBlank(bo.getBankDesc()), QuestionBank::getBankDesc, bo.getBankDesc());
        lqw.eq(bo.getCreatorId() != null, QuestionBank::getCreatorId, bo.getCreatorId());
        lqw.eq(StringUtils.isNotBlank(bo.getVisibility()), QuestionBank::getVisibility, bo.getVisibility());
        lqw.eq(bo.getStatus() != null, QuestionBank::getStatus, bo.getStatus());
        // 分类筛选：选中父分类时带上其下所有子分类
        if (ObjectUtil.isNotNull(bo.getCategoryId())) {
            List<Long> categoryIds = collectCategoryIds(bo.getCategoryId());
            lqw.in(CollUtil.isNotEmpty(categoryIds), QuestionBank::getCategoryId, categoryIds);
        }
        return lqw;
    }

    /**
     * 收集指定分类及其所有子孙分类id
     */
    private List<Long> collectCategoryIds(Long categoryId) {
        List<QuestionBankCategory> all = questionBankCategoryMapper.selectList(
            Wrappers.lambdaQuery(QuestionBankCategory.class).eq(QuestionBankCategory::getIsDeleted, 0L));
        List<Long> result = new ArrayList<>();
        collectChildren(all, categoryId, result);
        return result;
    }

    private void collectChildren(List<QuestionBankCategory> all, Long parentId, List<Long> result) {
        result.add(parentId);
        for (QuestionBankCategory category : all) {
            if (parentId.equals(category.getParentId())) {
                collectChildren(all, category.getId(), result);
            }
        }
    }

    /**
     * 新增题库
     *
     * @param bo 题库
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(QuestionBankBo bo) {
        QuestionBank add = MapstructUtils.convert(bo, QuestionBank.class);
        // 创建人由后端自动填充为当前登录用户，不取前端传值
        add.setCreatorId(LoginHelper.getUserId());
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改题库
     *
     * @param bo 题库
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(QuestionBankBo bo) {
        QuestionBank update = MapstructUtils.convert(bo, QuestionBank.class);
        validEntityBeforeSave(update);
        // 创建人不允许被修改
        update.setCreatorId(null);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(QuestionBank entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除题库信息
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
