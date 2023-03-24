package org.lottery.db;

/**
 * @ClassName: DBContextHolder
 * @Description: 数据源上下文
 * @Author: seven
 * @CreateTime: 2023-03-23 16:03
 * @Version: 1.0
 **/

public class DBContextHolder {
    private static final ThreadLocal<String> dbKey = new ThreadLocal<String>();
    private static final ThreadLocal<String> tbKey = new ThreadLocal<String>();

    public static void setDbKey(String dbKeyIdx) {
        dbKey.set(dbKeyIdx);
    }

    public static String getDbKey() {
        return dbKey.get();
    }

    public static void setTbKey(String tbKeyIdx) {
        tbKey.set(tbKeyIdx);
    }

    public static String getTbKey() {
        return tbKey.get();
    }

    public static void clearDBKey() {
        dbKey.remove();
    }

    public static void clearTBKey() {
        tbKey.remove();
    }
}
