package org.dromara.exam.paper.service.impl;

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
import org.dromara.exam.paper.domain.bo.PaperBo;
import org.dromara.exam.paper.domain.vo.PaperVo;
import org.dromara.exam.paper.domain.Paper;
import org.dromara.exam.paper.mapper.PaperMapper;
import org.dromara.exam.paper.service.IPaperService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

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

    /**
     * 查询试卷主
     *
     * @param id 主键
     * @return 试卷主
     */
    @Override
    public PaperVo queryById(Long id){
        return baseMapper.selectVoById(id);
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
        lqw.eq(StringUtils.isNotBlank(bo.getPaperDesc()), Paper::getPaperDesc, bo.getPaperDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getPaperType()), Paper::getPaperType, bo.getPaperType());
        lqw.eq(bo.getTotalScore() != null, Paper::getTotalScore, bo.getTotalScore());
        lqw.eq(bo.getPassScore() != null, Paper::getPassScore, bo.getPassScore());
        lqw.eq(bo.getTimeLimit() != null, Paper::getTimeLimit, bo.getTimeLimit());
        lqw.eq(StringUtils.isNotBlank(bo.getVisibility()), Paper::getVisibility, bo.getVisibility());
        lqw.eq(StringUtils.isNotBlank(bo.getSharePassword()), Paper::getSharePassword, bo.getSharePassword());
        lqw.eq(bo.getShareExpireTime() != null, Paper::getShareExpireTime, bo.getShareExpireTime());
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
