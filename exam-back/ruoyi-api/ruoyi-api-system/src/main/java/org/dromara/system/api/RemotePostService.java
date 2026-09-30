package org.dromara.system.api;

import java.util.List;
import java.util.Map;

/**
 * 岗位服务
 *
 * @author Lion Li
 */
public interface RemotePostService {

    /**
     * 根据岗位 ID 列表查询岗位名称映射关系
     *
     * @param postIds 岗位 ID 列表
     * @return Map，其中 key 为岗位 ID，value 为对应的岗位名称
     */
    Map<Long, String> selectPostNamesByIds(List<Long> postIds);

}
