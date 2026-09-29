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
import org.dromara.exam.question.domain.bo.QuestionMediaBo;
import org.dromara.exam.question.domain.vo.QuestionMediaVo;
import org.dromara.exam.question.domain.QuestionMedia;
import org.dromara.exam.question.mapper.QuestionMediaMapper;
import org.dromara.exam.question.service.IQuestionMediaService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 试题多媒体附件Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionMediaServiceImpl implements IQuestionMediaService {

    private final QuestionMediaMapper baseMapper;

    /**
     * 查询试题多媒体附件
     *
     * @param id 主键
     * @return 试题多媒体附件
     */
    @Override
    public QuestionMediaVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询试题多媒体附件列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题多媒体附件分页列表
     */
    @Override
    public TableDataInfo<QuestionMediaVo> queryPageList(QuestionMediaBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<QuestionMedia> lqw = buildQueryWrapper(bo);
        Page<QuestionMediaVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的试题多媒体附件列表
     *
     * @param bo 查询条件
     * @return 试题多媒体附件列表
     */
    @Override
    public List<QuestionMediaVo> queryList(QuestionMediaBo bo) {
        LambdaQueryWrapper<QuestionMedia> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<QuestionMedia> buildQueryWrapper(QuestionMediaBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<QuestionMedia> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(QuestionMedia::getId);
        lqw.eq(bo.getQuestionId() != null, QuestionMedia::getQuestionId, bo.getQuestionId());
        lqw.eq(StringUtils.isNotBlank(bo.getMediaType()), QuestionMedia::getMediaType, bo.getMediaType());
        lqw.eq(StringUtils.isNotBlank(bo.getMediaUrl()), QuestionMedia::getMediaUrl, bo.getMediaUrl());
        lqw.like(StringUtils.isNotBlank(bo.getMediaName()), QuestionMedia::getMediaName, bo.getMediaName());
        lqw.eq(bo.getSort() != null, QuestionMedia::getSort, bo.getSort());
        return lqw;
    }

    /**
     * 新增试题多媒体附件
     *
     * @param bo 试题多媒体附件
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(QuestionMediaBo bo) {
        QuestionMedia add = MapstructUtils.convert(bo, QuestionMedia.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改试题多媒体附件
     *
     * @param bo 试题多媒体附件
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(QuestionMediaBo bo) {
        QuestionMedia update = MapstructUtils.convert(bo, QuestionMedia.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(QuestionMedia entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除试题多媒体附件信息
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
