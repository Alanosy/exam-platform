package org.dromara.system.dubbo;

import cn.hutool.core.lang.Dict;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.system.api.RemoteConfigService;
import org.dromara.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置服务
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteConfigServiceImpl implements RemoteConfigService {

    private final ISysConfigService configService;

    /**
     * 获取注册开关
     */
    @Override
    public boolean selectRegisterEnabled(String tenantId) {
        return configService.selectRegisterEnabled(tenantId);
    }

    @Override
    public String getConfigValue(String configKey) {
        return configService.selectConfigByKey(configKey);
    }

    /**
     * 根据参数 key 获取 Map 类型的配置
     *
     * @param configKey 参数 key
     * @return Dict 对象，如果配置为空或无法解析，返回空 Dict
     */
    @Override
    public Dict getConfigMap(String configKey) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseMap(configValue);
    }

    /**
     * 根据参数 key 获取 Map 类型的配置列表
     *
     * @param configKey 参数 key
     * @return Dict 列表，如果配置为空或无法解析，返回空列表
     */
    @Override
    public List<Dict> getConfigArrayMap(String configKey) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseArrayMap(configValue);
    }

    /**
     * 根据参数 key 获取指定类型的配置对象
     *
     * @param configKey 参数 key
     * @param clazz     目标对象类型
     * @return 对象实例，如果配置为空或无法解析，返回 null
     */
    @Override
    public <T> T getConfigObject(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseObject(configValue, clazz);
    }

    /**
     * 根据参数 key 获取指定类型的配置列表=
     *
     * @param configKey 参数 key
     * @param clazz     目标元素类型
     * @return 指定类型列表，如果配置为空或无法解析，返回空列表
     */
    @Override
    public <T> List<T> getConfigArray(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseArray(configValue, clazz);
    }


}
