package org.dromara.exam.question.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.exam.question.domain.bo.QuestionTagRelBo;
import org.dromara.exam.question.domain.vo.QuestionTagRelVo;
import org.dromara.exam.question.domain.QuestionTagRel;
import org.dromara.exam.question.mapper.QuestionTagRelMapper;
import org.dromara.exam.question.service.IQuestionTagRelService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 试题标签关联Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionTagRelServiceImpl implements IQuestionTagRelService {

    private final QuestionTagRelMapper baseMapper;

    /**
     * 查询试题标签关联
     *
     * @param id 主键
     * @return 试题标签关联
     */
    @Override
    public QuestionTagRelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询试题标签关联列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题标签关联分页列表
     */
    @Override
    public TableDataInfo<QuestionTagRelVo> queryPageList(QuestionTagRelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<QuestionTagRel> lqw = buildQueryWrapper(bo);
        Page<QuestionTagRelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的试题标签关联列表
     *
     * @param bo 查询条件
     * @return 试题标签关联列表
     */
    @Override
    public List<QuestionTagRelVo> queryList(QuestionTagRelBo bo) {
        LambdaQueryWrapper<QuestionTagRel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<QuestionTagRel> buildQueryWrapper(QuestionTagRelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<QuestionTagRel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(QuestionTagRel::getId);
        lqw.eq(bo.getQuestionId() != null, QuestionTagRel::getQuestionId, bo.getQuestionId());
        lqw.eq(bo.getTagId() != null, QuestionTagRel::getTagId, bo.getTagId());
        return lqw;
    }

    /**
     * 新增试题标签关联
     *
     * @param bo 试题标签关联
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(QuestionTagRelBo bo) {
        QuestionTagRel add = MapstructUtils.convert(bo, QuestionTagRel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改试题标签关联
     *
     * @param bo 试题标签关联
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(QuestionTagRelBo bo) {
        QuestionTagRel update = MapstructUtils.convert(bo, QuestionTagRel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(QuestionTagRel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除试题标签关联信息
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
