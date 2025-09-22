package org.dromara.system.dubbo;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.system.api.RemoteRoleService;
import org.dromara.system.domain.SysRole;
import org.dromara.system.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 角色服务
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteRoleServiceImpl implements RemoteRoleService {

    private final SysRoleMapper roleMapper;

    /**
     * 根据角色 ID 列表查询角色名称映射关系
     *
     * @param roleIds 角色 ID 列表
     * @return Map，其中 key 为角色 ID，value 为对应的角色名称
     */
    @Override
    public Map<Long, String> selectRoleNamesByIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }
        List<SysRole> list = roleMapper.selectList(
            new LambdaQueryWrapper<SysRole>()
                .select(SysRole::getRoleId, SysRole::getRoleName)
                .in(SysRole::getRoleId, roleIds)
        );
        return StreamUtils.toMap(list, SysRole::getRoleId, SysRole::getRoleName);
    }

}
