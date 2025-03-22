package cn.org.alan.exam.config;

import cn.org.alan.exam.common.handler.FiledFullHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import javax.annotation.Resource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus配置
 *
 * @Author Alan
 * @Version
 * @Date 2024/3/28 3:57 PM
 */
@Configuration
// mapper 包扫描
@MapperScan("cn.org.alan.exam.mapper")
public class MybatisPlusConfig {
    @Resource
    private FiledFullHandler filedFullHandler;

    /**
     * 配置数据库类型
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new
                PaginationInnerInterceptor(DbType.MYSQL));
        // 添加元数据对象处理器
        return interceptor;
    }

    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig config = new GlobalConfig();
        config.setMetaObjectHandler(filedFullHandler);
        return config;
    }
}
