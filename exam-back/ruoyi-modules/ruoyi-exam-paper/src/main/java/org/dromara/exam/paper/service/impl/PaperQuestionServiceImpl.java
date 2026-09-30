package org.dromara.exam.paper.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.exam.paper.domain.bo.PaperQuestionBo;
import org.dromara.exam.paper.domain.vo.PaperQuestionVo;
import org.dromara.exam.paper.domain.PaperQuestion;
import org.dromara.exam.paper.mapper.PaperQuestionMapper;
import org.dromara.exam.paper.service.IPaperQuestionService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 试卷-试题中间Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PaperQuestionServiceImpl implements IPaperQuestionService {

    private final PaperQuestionMapper baseMapper;

    /**
     * 查询试卷-试题中间
     *
     * @param id 主键
     * @return 试卷-试题中间
     */
    @Override
    public PaperQuestionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询试卷-试题中间列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试卷-试题中间分页列表
     */
    @Override
    public TableDataInfo<PaperQuestionVo> queryPageList(PaperQuestionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PaperQuestion> lqw = buildQueryWrapper(bo);
        Page<PaperQuestionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的试卷-试题中间列表
     *
     * @param bo 查询条件
     * @return 试卷-试题中间列表
     */
    @Override
    public List<PaperQuestionVo> queryList(PaperQuestionBo bo) {
        LambdaQueryWrapper<PaperQuestion> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<PaperQuestion> buildQueryWrapper(PaperQuestionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<PaperQuestion> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(PaperQuestion::getId);
        lqw.eq(bo.getPaperId() != null, PaperQuestion::getPaperId, bo.getPaperId());
        lqw.eq(bo.getQuestionId() != null, PaperQuestion::getQuestionId, bo.getQuestionId());
        lqw.eq(bo.getPaperScore() != null, PaperQuestion::getPaperScore, bo.getPaperScore());
        lqw.eq(bo.getSort() != null, PaperQuestion::getSort, bo.getSort());
        return lqw;
    }

    /**
     * 新增试卷-试题中间
     *
     * @param bo 试卷-试题中间
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(PaperQuestionBo bo) {
        PaperQuestion add = MapstructUtils.convert(bo, PaperQuestion.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改试卷-试题中间
     *
     * @param bo 试卷-试题中间
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(PaperQuestionBo bo) {
        PaperQuestion update = MapstructUtils.convert(bo, PaperQuestion.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(PaperQuestion entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除试卷-试题中间信息
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
