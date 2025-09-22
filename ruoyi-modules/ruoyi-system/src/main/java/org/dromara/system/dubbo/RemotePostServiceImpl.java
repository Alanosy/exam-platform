package org.dromara.system.dubbo;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.system.api.RemotePostService;
import org.dromara.system.domain.SysPost;
import org.dromara.system.mapper.SysPostMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 岗位服务
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@DubboService
public class RemotePostServiceImpl implements RemotePostService {

    private final SysPostMapper postMapper;

    /**
     * 根据岗位 ID 列表查询岗位名称映射关系
     *
     * @param postIds 岗位 ID 列表
     * @return Map，其中 key 为岗位 ID，value 为对应的岗位名称
     */
    @Override
    public Map<Long, String> selectPostNamesByIds(List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyMap();
        }
        List<SysPost> list = postMapper.selectList(
            new LambdaQueryWrapper<SysPost>()
                .select(SysPost::getPostId, SysPost::getPostName)
                .in(SysPost::getPostId, postIds)
        );
        return StreamUtils.toMap(list, SysPost::getPostId, SysPost::getPostName);
    }

}
