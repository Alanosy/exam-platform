package org.dromara.exam.question.service.impl;

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
import org.dromara.exam.question.domain.bo.QuestionTagBo;
import org.dromara.exam.question.domain.vo.QuestionTagVo;
import org.dromara.exam.question.domain.QuestionTag;
import org.dromara.exam.question.mapper.QuestionTagMapper;
import org.dromara.exam.question.service.IQuestionTagService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 试题标签Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionTagServiceImpl implements IQuestionTagService {

    private final QuestionTagMapper baseMapper;

    /**
     * 查询试题标签
     *
     * @param id 主键
     * @return 试题标签
     */
    @Override
    public QuestionTagVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询试题标签列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题标签分页列表
     */
    @Override
    public TableDataInfo<QuestionTagVo> queryPageList(QuestionTagBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<QuestionTag> lqw = buildQueryWrapper(bo);
        Page<QuestionTagVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的试题标签列表
     *
     * @param bo 查询条件
     * @return 试题标签列表
     */
    @Override
    public List<QuestionTagVo> queryList(QuestionTagBo bo) {
        LambdaQueryWrapper<QuestionTag> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<QuestionTag> buildQueryWrapper(QuestionTagBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<QuestionTag> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(QuestionTag::getId);
        lqw.like(StringUtils.isNotBlank(bo.getTagName()), QuestionTag::getTagName, bo.getTagName());
        lqw.eq(bo.getCreatorId() != null, QuestionTag::getCreatorId, bo.getCreatorId());
        return lqw;
    }

    /**
     * 新增试题标签
     *
     * @param bo 试题标签
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(QuestionTagBo bo) {
        QuestionTag add = MapstructUtils.convert(bo, QuestionTag.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改试题标签
     *
     * @param bo 试题标签
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(QuestionTagBo bo) {
        QuestionTag update = MapstructUtils.convert(bo, QuestionTag.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(QuestionTag entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除试题标签信息
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
