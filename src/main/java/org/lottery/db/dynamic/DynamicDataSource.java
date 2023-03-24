package org.lottery.db.dynamic;

import org.lottery.db.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @ClassName: DynamicDataSource
 * @Description: 动态数据源获取，每当切换数据源，都要从这个里面进行获取
 * @Author: seven
 * @CreateTime: 2023-03-23 19:06
 * @Version: 1.0
 **/

public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDbKey();
    }
}
