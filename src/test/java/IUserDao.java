import org.lottery.db.annotation.DBRouter;

/**
 * @ClassName: IUserDao
 * @Description:
 * @Author: seven
 * @CreateTime: 2023-03-24 14:17
 * @Version: 1.0
 **/

public interface IUserDao {
    @DBRouter(key = "userId")
    void insertUser(String req);
}
