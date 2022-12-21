import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by guolin
 * 描述：     最简单的缓存形式：HashMap
 */
public class Cache1 {
    private final HashMap<String,Integer> cache = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        Cache1 cache1 = new Cache1();
        System.out.println("开始计算了");
        Integer result = cache1.computer("13");
        System.out.println("第一次计算结果："+result);
        result = cache1.computer("13");
        System.out.println("第二次计算结果："+result);
    }

    /**
     * 查询缓存和插入缓存的操作
     * @param userId
     * @return
     * @throws InterruptedException
     */
    public synchronized Integer computer(String userId) throws InterruptedException {
        Integer result = cache.get(userId);
        //先检查HashMap里面有没有保存过之前的计算结果
        if (result == null) {
            //如果缓存中找不到，那么需要现在计算一下结果，并且保存到HashMap中
            result = doCompute(userId);
            cache.put(userId, result);
        }
        return result;
    }

    /**
     * 模拟的是一个计算逻辑
     * @param userId
     * @return
     * @throws InterruptedException
     */
    private Integer doCompute(String userId) throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        return new Integer(userId);
    }
}
