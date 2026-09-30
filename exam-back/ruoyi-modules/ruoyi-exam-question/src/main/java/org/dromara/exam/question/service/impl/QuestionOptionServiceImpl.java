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
import org.dromara.exam.question.domain.bo.QuestionOptionBo;
import org.dromara.exam.question.domain.vo.QuestionOptionVo;
import org.dromara.exam.question.domain.QuestionOption;
import org.dromara.exam.question.mapper.QuestionOptionMapper;
import org.dromara.exam.question.service.IQuestionOptionService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 试题选项Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionOptionServiceImpl implements IQuestionOptionService {

    private final QuestionOptionMapper baseMapper;

    /**
     * 查询试题选项
     *
     * @param id 主键
     * @return 试题选项
     */
    @Override
    public QuestionOptionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询试题选项列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题选项分页列表
     */
    @Override
    public TableDataInfo<QuestionOptionVo> queryPageList(QuestionOptionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<QuestionOption> lqw = buildQueryWrapper(bo);
        Page<QuestionOptionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的试题选项列表
     *
     * @param bo 查询条件
     * @return 试题选项列表
     */
    @Override
    public List<QuestionOptionVo> queryList(QuestionOptionBo bo) {
        LambdaQueryWrapper<QuestionOption> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<QuestionOption> buildQueryWrapper(QuestionOptionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<QuestionOption> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(QuestionOption::getId);
        lqw.eq(bo.getQuestionId() != null, QuestionOption::getQuestionId, bo.getQuestionId());
        lqw.eq(StringUtils.isNotBlank(bo.getOptionKey()), QuestionOption::getOptionKey, bo.getOptionKey());
        lqw.eq(StringUtils.isNotBlank(bo.getOptionContent()), QuestionOption::getOptionContent, bo.getOptionContent());
        lqw.eq(bo.getSort() != null, QuestionOption::getSort, bo.getSort());
        return lqw;
    }

    /**
     * 新增试题选项
     *
     * @param bo 试题选项
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(QuestionOptionBo bo) {
        QuestionOption add = MapstructUtils.convert(bo, QuestionOption.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改试题选项
     *
     * @param bo 试题选项
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(QuestionOptionBo bo) {
        QuestionOption update = MapstructUtils.convert(bo, QuestionOption.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(QuestionOption entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除试题选项信息
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
