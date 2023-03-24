package org.lottery.db;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.lottery.db.annotation.DBRouter;
import org.lottery.db.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @ClassName: DBRouterJoinPoint
 * @Description: 数据路由切面，通过自定义注解的方式，
 * 拦截被切面的方法，进行数据库路由
 * @Author: seven
 * @CreateTime: 2023-03-23 16:10
 * @Version: 1.0
 **/

@Aspect
public class DBRouterJoinPoint {
    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    private DBRouterConfig dbRouterConfig;

    private IDBRouterStrategy dbRouterStrategy;

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    @Pointcut("@annotation(org.lottery.db.annotation.DBRouter)")
    public void aopPoint() {

    }

    /**
     * 所有分库分表的操作，都需要使用自定义注解进行拦截，
     * 拦截后读取方法中的入参字段，根据字段进行路由操作。
     * 1. dbRouter.key() 确定根据那个字段进行路由
     * 2. getAttrValue 根据数据库路由字段，从入参中读取对应的值，比如路由key是uId， 那么就从入参对象Obj中获取 uId
     * 3. dbRouterStrategy.doRouter(dbKeyAttr) 路由策略根据具体的路由值进行处理
     * 4. 路由处理完成比，就是放行。 jp.proceed()
     * 5. 最后dbRouterStrategy 需要执行clear 因此这里用到了ThreadLocal 需要手动清空，
     *
     * @param jp
     * @param dbRouter
     * @return
     * @throws Throwable
     */
    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable {
        String dbkey = dbRouter.key();
        if (StringUtils.isBlank(dbkey) && StringUtils.isBlank(dbRouterConfig.getRouterKey())) {
            throw new RuntimeException("annotation DBRouter key is null");
        }

        dbkey = StringUtils.isBlank(dbkey) ? dbkey : dbRouterConfig.getRouterKey();
        //路由属性
        String dbKeyAttr = getAttrValue(dbkey, jp.getArgs());
        // 路由策略
        dbRouterStrategy.doRouter(dbKeyAttr);
        //返回结果
        try {
            return jp.proceed();
        } finally {
            dbRouterStrategy.clear();
        }

    }


    public Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());

    }

    /**
     * 获取属性值
     *
     * @param attr 属性
     * @param args 参数
     * @return
     */
    public String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                filedValue = BeanUtils.getProperty(arg, attr);
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr:{}", attr, e);
            }

        }
        return filedValue;
    }
}
