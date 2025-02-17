package org.dromara.system.api;

import org.dromara.system.api.domain.bo.RemoteTaskAssigneeBo;
import org.dromara.system.api.domain.vo.RemoteTaskAssigneeVo;

/**
 * 工作流设计器获取任务执行人
 *
 * @author Lion Li
 */
public interface RemoteTaskAssigneeService {

    /**
     * 查询角色并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    RemoteTaskAssigneeVo selectRolesByTaskAssigneeList(RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询岗位并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    RemoteTaskAssigneeVo selectPostsByTaskAssigneeList(RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询部门并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    RemoteTaskAssigneeVo selectDeptsByTaskAssigneeList(RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询用户并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    RemoteTaskAssigneeVo selectUsersByTaskAssigneeList(RemoteTaskAssigneeBo taskQuery);

}
