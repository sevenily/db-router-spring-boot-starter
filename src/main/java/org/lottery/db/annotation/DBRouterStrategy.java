package org.lottery.db.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: DBRouterStrategy
 * @Description: 路由策略，分表标记
 * @Author: seven
 * @CreateTime: 2023-03-24 09:26
 * @Version: 1.0
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    boolean splitTable() default false;
}
