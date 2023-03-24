package org.lottery.db.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: DBRouter
 * @Description: 路由注解
 * @Author: seven
 * @CreateTime: 2023-03-24 09:20
 * @Version: 1.0
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouter {
    /**
     * 分库分表字段
     *
     * @return
     */
    String key() default "";

}
