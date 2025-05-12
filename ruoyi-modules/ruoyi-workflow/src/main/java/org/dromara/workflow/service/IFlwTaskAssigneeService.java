package org.dromara.workflow.service;

import org.dromara.system.api.domain.vo.RemoteUserVo;

import java.util.List;

/**
 * 流程设计器-获取办理人
 *
 * @author AprilWind
 */
public interface IFlwTaskAssigneeService {

    /**
     * 根据存储标识符（storageId）解析分配类型和ID，并获取对应的用户列表
     * 支持单个标识（例如 "user:123" 或 "456"），格式非法将返回空列表
     *
     * @param storageId 包含分配类型和ID的字符串
     * @return 匹配的用户列表，格式非法返回空列表
     */
    List<RemoteUserVo> fetchUsersByStorageId(String storageId);

    /**
     * 批量解析多个存储标识符（storageIds），按类型分类并合并查询用户列表
     * 输入格式支持多个以逗号分隔的标识（如 "user:123,role:456,789"）
     * 会自动去重返回结果，非法格式的标识将被忽略
     *
     * @param storageIds 多个存储标识符字符串（逗号分隔）
     * @return 合并后的用户列表，去重后返回，非法格式的标识将被跳过
     */
    List<RemoteUserVo> fetchUsersByStorageIds(String storageIds);

}
