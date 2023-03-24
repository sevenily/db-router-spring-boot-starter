import org.junit.Test;
import org.lottery.db.annotation.DBRouter;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

/**
 * @ClassName: Test
 * @Description: 算法测试
 * @Author: seven
 * @CreateTime: 2023-03-23 15:36
 * @Version: 1.0
 **/

@SpringBootTest
public class TestApplication {
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * 斐波那契散列算法:可以让数据更加分散。在发生数据碰撞时，进行开放寻址，从碰撞点向后
     * 寻找位置进行存放元素： f(k) = (k*2654435769)>>28
     * 黄金分割点：（√5-1）/2 = 0.6180339887 1.618:1 == 1:0.618
     */
    @Test
    public void test_idx() {
        int hashcode = 0;
        for (int j = 0; j < 16; j++) {
            hashcode = HASH_INCREMENT * j + HASH_INCREMENT;
            int idx = hashcode & (16 - 1);
            System.out.println("斐波那契散列：" + idx + "普通散列：" + (String.valueOf(j).hashCode() & (16 - 1)));
        }
    }

    /**
     * 扰动函数&拉链寻址：（size-1）&(key.hashCode()^(key.hashCode()>>>16))
     */
    public static int disturbHashIdx(String key, int size) {
        return (size - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));
    }

    @Test
    public void test_str_format() {
        System.out.println(String.format("db%02d", 1));
        System.out.println(String.format("_%03d", 25));
    }

    @Test
    public void test_annotation() throws NoSuchMethodException {
        Class<IUserDao> iUserDaoClass = IUserDao.class;
        Method method = iUserDaoClass.getMethod("insertUser", String.class);
        DBRouter dbRouter = method.getAnnotation(DBRouter.class);
        System.out.println(dbRouter.key());
    }
}
