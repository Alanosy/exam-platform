package org.dromara.system.service.impl;

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
import org.dromara.system.domain.bo.AiModelConfigBo;
import org.dromara.system.domain.vo.AiModelConfigVo;
import org.dromara.system.domain.AiModelConfig;
import org.dromara.system.mapper.AiModelConfigMapper;
import org.dromara.system.service.IAiModelConfigService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * AI大模型配置Service业务层处理
 *
 * @author Alan
 * @date 2026-10-01
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AiModelConfigServiceImpl implements IAiModelConfigService {

    private final AiModelConfigMapper baseMapper;

    /**
     * 查询AI大模型配置
     *
     * @param id 主键
     * @return AI大模型配置
     */
    @Override
    public AiModelConfigVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询AI大模型配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI大模型配置分页列表
     */
    @Override
    public TableDataInfo<AiModelConfigVo> queryPageList(AiModelConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AiModelConfig> lqw = buildQueryWrapper(bo);
        Page<AiModelConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的AI大模型配置列表
     *
     * @param bo 查询条件
     * @return AI大模型配置列表
     */
    @Override
    public List<AiModelConfigVo> queryList(AiModelConfigBo bo) {
        LambdaQueryWrapper<AiModelConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AiModelConfig> buildQueryWrapper(AiModelConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AiModelConfig> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AiModelConfig::getId);
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), AiModelConfig::getConfigName, bo.getConfigName());
        lqw.eq(StringUtils.isNotBlank(bo.getModelType()), AiModelConfig::getModelType, bo.getModelType());
        lqw.like(StringUtils.isNotBlank(bo.getModelName()), AiModelConfig::getModelName, bo.getModelName());
        lqw.eq(StringUtils.isNotBlank(bo.getApiBase()), AiModelConfig::getApiBase, bo.getApiBase());
        return lqw;
    }

    /**
     * 新增AI大模型配置
     *
     * @param bo AI大模型配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AiModelConfigBo bo) {
        AiModelConfig add = MapstructUtils.convert(bo, AiModelConfig.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改AI大模型配置
     *
     * @param bo AI大模型配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AiModelConfigBo bo) {
        AiModelConfig update = MapstructUtils.convert(bo, AiModelConfig.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(AiModelConfig entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除AI大模型配置信息
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
