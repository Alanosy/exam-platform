package org.dromara.easyes.starter.config;

import org.dromara.easyes.core.config.GeneratorConfig;
import org.dromara.easyes.core.toolkit.Generator;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 代码生成注册
 * @author MoJie
 * @since 2.0
 */
@Component
@ConditionalOnProperty(value = "easy-es.enable", havingValue = "true")
public class GeneratorConfiguration extends Generator {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean generate(GeneratorConfig config) {
        super.generateEntity(config, this.client);
        return Boolean.TRUE;
    }
}
