package org.dromara.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.workflow.domain.FlowInstanceBizExt;
import org.dromara.workflow.domain.bo.FlowInstanceBizExtBo;
import org.dromara.workflow.mapper.FlwInstanceBizExtMapper;
import org.dromara.workflow.service.IFlwInstanceBizExtService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程实例业务扩展Service业务层处理
 *
 * @author may
 * @date 2025-08-05
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FlwInstanceBizExtServiceImpl implements IFlwInstanceBizExtService {

    private final FlwInstanceBizExtMapper baseMapper;


    /**
     * 新增/修改流程实例业务扩展
     *
     * @param bo 流程实例业务扩展
     * @return 是否新增成功
     */
    @Override
    public Boolean saveOrUpdate(FlowInstanceBizExtBo bo) {
        FlowInstanceBizExt convert = MapstructUtils.convert(bo, FlowInstanceBizExt.class);
        FlowInstanceBizExt flowInstanceBizExt = baseMapper.selectOne(new LambdaQueryWrapper<FlowInstanceBizExt>()
            .eq(FlowInstanceBizExt::getInstanceId, bo.getInstanceId()));
        if (flowInstanceBizExt != null) {
            flowInstanceBizExt.setBusinessTitle(convert.getBusinessTitle());
            return baseMapper.insertOrUpdate(flowInstanceBizExt);
        }
        return baseMapper.insertOrUpdate(convert);
    }

    /**
     * 按照流程实例ID批量删除
     *
     * @param instanceIds 流程实例ID
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteByInstIds(List<Long> instanceIds) {
        return baseMapper.delete(new LambdaQueryWrapper<FlowInstanceBizExt>().in(FlowInstanceBizExt::getInstanceId, instanceIds)) > 0;
    }
}
