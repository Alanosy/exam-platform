package cn.org.alan.exam.config;

// import com.zaxxer.hikari.HikariDataSource;
// import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
// import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
// import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
// import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Author Alan
 * @Version
 * @Date 2024/6/11 4:21 PM
 */
// @Configuration
public class ShardingsphereConfig {
    // public DataSource hikariDataSource (String username, String pwd, String className, String url){
    //     HikariDataSource hikariDataSource1=new HikariDataSource();
    //     hikariDataSource1.setUsername(username);
    //     hikariDataSource1.setPassword(pwd);
    //     hikariDataSource1.setDriverClassName(className);
    //     hikariDataSource1.setJdbcUrl(url);
    //     return hikariDataSource1;
    // }
    //
    // @Bean
    // public ShardingRuleConfiguration shardingRuleConfiguration(){
    //     ShardingRuleConfiguration config=new ShardingRuleConfiguration();
    //     LoadBalanceStrategyConfiguration loadConfiguration=new LoadBalanceStrategyConfiguration("round_robin");
    //     MasterSlaveRuleConfiguration configuration=new MasterSlaveRuleConfiguration("dataSource",
    //             "master", List.of("slave"),
    //             loadConfiguration
    //     );
    //     config.setMasterSlaveRuleConfigs(List.of(configuration));
    //     return config;
    // }
    //
    //
    // @Bean
    // public DataSource shardingDataSource(ShardingRuleConfiguration configuration){
    //     DataSource masterDataSource = hikariDataSource("root", "123456", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://47.109.94.143:9506/db_exam");
    //     DataSource slaveDataSource = hikariDataSource("root", "123456", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://47.109.94.143:9507/db_exam");
    //
    //     Map<String, DataSource> dataSourceMap=new HashMap<>();
    //     dataSourceMap.put("master",masterDataSource);
    //     dataSourceMap.put("slave",slaveDataSource);
    //
    //     DataSource shardingDataSource= null;
    //     Properties props=new Properties();
    //     props.put("sql.show",true);
    //     try {
    //         shardingDataSource= ShardingDataSourceFactory.createDataSource(dataSourceMap,configuration,props);
    //     } catch (SQLException e) {
    //         throw new RuntimeException(e);
    //     }
    //     return shardingDataSource;
    // }
}

