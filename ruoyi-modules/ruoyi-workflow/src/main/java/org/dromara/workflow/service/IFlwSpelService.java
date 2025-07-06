package org.dromara.workflow.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.api.domain.bo.RemoteTaskAssigneeBo;
import org.dromara.system.api.domain.vo.RemoteTaskAssigneeVo;
import org.dromara.workflow.domain.bo.FlowSpelBo;
import org.dromara.workflow.domain.vo.FlowSpelVo;

import java.util.Collection;
import java.util.List;

/**
 * 流程spel达式定义Service接口
 *
 * @author Michelle.Chung
 * @date 2025-07-04
 */
public interface IFlwSpelService {

    RemoteTaskAssigneeVo selectSpelByTaskAssigneeList(RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询流程spel达式定义
     *
     * @param id 主键
     * @return 流程spel达式定义
     */
    FlowSpelVo queryById(Long id);

    /**
     * 分页查询流程spel达式定义列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 流程spel达式定义分页列表
     */
    TableDataInfo<FlowSpelVo> queryPageList(FlowSpelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的流程spel达式定义列表
     *
     * @param bo 查询条件
     * @return 流程spel达式定义列表
     */
    List<FlowSpelVo> queryList(FlowSpelBo bo);

    /**
     * 新增流程spel达式定义
     *
     * @param bo 流程spel达式定义
     * @return 是否新增成功
     */
    Boolean insertByBo(FlowSpelBo bo);

    /**
     * 修改流程spel达式定义
     *
     * @param bo 流程spel达式定义
     * @return 是否修改成功
     */
    Boolean updateByBo(FlowSpelBo bo);

    /**
     * 校验并批量删除流程spel达式定义信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
