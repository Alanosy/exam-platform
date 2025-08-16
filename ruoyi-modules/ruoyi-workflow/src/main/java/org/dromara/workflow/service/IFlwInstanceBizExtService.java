package org.dromara.workflow.service;

import org.dromara.workflow.domain.bo.FlowInstanceBizExtBo;

import java.util.List;

/**
 * 流程实例业务扩展Service接口
 *
 * @author may
 * @date 2025-08-05
 */
public interface IFlwInstanceBizExtService {

    /**
     * 新增/修改流程实例业务扩展
     *
     * @param bo 流程实例业务扩展
     * @return 是否新增成功
     */
    Boolean saveOrUpdate(FlowInstanceBizExtBo bo);

    /**
     * 按照流程实例ID批量删除
     *
     * @param instanceIds 流程实例ID
     * @return 是否删除成功
     */
    Boolean deleteByInstIds(List<Long> instanceIds);
}
