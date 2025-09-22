package org.dromara.system.api;

import java.util.List;
import java.util.Map;

/**
 * 角色服务
 *
 * @author Lion Li
 */
public interface RemoteRoleService {

    /**
     * 根据角色 ID 列表查询角色名称映射关系
     *
     * @param roleIds 角色 ID 列表
     * @return Map，其中 key 为角色 ID，value 为对应的角色名称
     */
    Map<Long, String> selectRoleNamesByIds(List<Long> roleIds);

}
