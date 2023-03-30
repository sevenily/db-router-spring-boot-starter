package org.lottery.db.config;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.transaction.Transaction;
import org.lottery.db.DBRouterConfig;
import org.lottery.db.DBRouterJoinPoint;
import org.lottery.db.dynamic.DynamicDataSource;
import org.lottery.db.dynamic.DynamicMybatisPlugin;
import org.lottery.db.strategy.IDBRouterStrategy;
import org.lottery.db.strategy.impl.DBRouterStrategyHashCode;
import org.lottery.db.util.PropertyUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: DataSourceAutoConfig
 *
 * EnvironmentAware: 可在服务启动时
 * 获取系统环境变量和application配置文件中的变量
 *
 * @Description: 数据源配置解析
 * @Author: seven
 * @CreateTime: 2023-03-30 16:34
 * @Version: 1.0
 **/

@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {
    /**
     * 数据源配置组
     */
    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceConfig;

    /**
     * 分库数量
     */
    private int dbCount;

    /**
     * 分表数量
     */
    private int tbCount;

    /**
     * 路由字段
     */
    private String routerKey;

    /**
     * 注入路由策略对象，便于切面和硬编码注入使用
     *
     *
     *  @ConditionalOnMissingBean: 修饰bean的一个注解，当bean被注册成功后，
     *  相同的bean只能被注册一次 保证实例只有一个
     *  @Bean
     * @param dbRouterConfig
     * @param dbRouterStrategy
     * @return
     */
    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DBRouterJoinPoint point(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy){
        return new DBRouterJoinPoint(dbRouterConfig, dbRouterStrategy);
    }

    @Bean
    public DBRouterConfig dbRouterConfig(){
        return new DBRouterConfig(dbCount,tbCount, routerKey);
    }

    @Bean
    public IDBRouterStrategy dbRouterStrategy(DBRouterConfig dbRouterConfig){
        return new DBRouterStrategyHashCode(dbRouterConfig);
    }

    @Bean
    public Interceptor plugin(){
        return new DynamicMybatisPlugin();
    }

    @Bean
    public DataSource dataSource(){
        //创建数据源
        HashMap<Object, Object> targetDataSources = new HashMap<>();
        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> objectMap = dataSourceMap.get(dbInfo);
            targetDataSources.put(dbInfo, new DriverManagerDataSource(objectMap.get("url").toString(),
                    dataSourceMap.get("username").toString(), dataSourceMap.get("password").toString()));
        }

        //设置数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(
                new DriverManagerDataSource(defaultDataSourceConfig.get("url").toString(),
                        defaultDataSourceConfig.get("username").toString(),
                        defaultDataSourceConfig.get("password").toString()));

        return dynamicDataSource;
    }

    /**
     *
     *  创建事务对象，用于编程式事务引用
     * @param dataSource
     * @return
     */
    //编程式事务
    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource){
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    //设置环境变量
    @Override
    public void setEnvironment(Environment environment) {
        String prefix= "mini-db-router.jdbc.datasource.";

        dbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "dbCount")));
        tbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "tbCount")));
        routerKey = environment.getProperty(prefix + "routerKey");

        String dataSource = environment.getProperty(prefix + "list");

        assert dataSource!= null;
        // 解析多数据源配置组的 driver-class-name、url、username、password
        for (String dbInfo : dataSource.split(",")) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }

        //默认数据源配置
        String defaultDataSource = environment.getProperty(prefix + "default");
        defaultDataSourceConfig = PropertyUtil.handle(environment, prefix+defaultDataSource, Map.class);
    }
}
