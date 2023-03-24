package org.lottery.db.strategy.impl;

import org.lottery.db.DBContextHolder;
import org.lottery.db.DBRouterConfig;
import org.lottery.db.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: DBRouterStrategyHashCode
 * @Description: 哈希路由
 * @Author: seven
 * @CreateTime: 2023-03-23 18:47
 * @Version: 1.0
 **/

public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyHashCode.class);
    private DBRouterConfig dbRouterConfig;

    public DBRouterStrategyHashCode(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        //扰动函数：在JDK的HashMap中，对于一个元素的存放，需要进行哈希散列。而为了让散列更加均匀，所以添加了扰动函数
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));

        //库索引：相当于是把一个长条的桶，切割成段，对应分库分表中的库编号和表编号
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        //设置到ThreadLocal
        DBContextHolder.setDbKey(String.format("%02d", dbIdx));
        DBContextHolder.setTbKey(String.format("%03d", tbIdx));

        logger.debug("数据库路由 dbIdx:{} tbIdx:{}", dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDbKey(String.format("%02d", dbIdx));

    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTbKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}
