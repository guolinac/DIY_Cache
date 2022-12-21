import computable.Computable;
import computable.ExpensiveFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guolin
 * 描述：     缩小了synchronized的粒度，提高性能，但是依然并发不安全
 * 只并发保护cache的写方法
 * 线程仍然不够安全，虽然多个线程不能同时写了，但是如果在写的同时读，同样是线程不安全的
 */

public class Cache4<A,V> implements Computable<A,V> {

    private final Map<A, V> cache = new HashMap();

    private  final Computable<A,V> c;

    public Cache4(Computable<A, V> c) {
        this.c = c;
    }

    @Override
    public V compute(A arg) throws Exception {
        System.out.println("进入缓存机制");
        V result = cache.get(arg);
        if (result == null) {
            result = c.compute(arg);
            // 只并发保护cache的写方法
            // 线程仍然不够安全，虽然多个线程不能同时写了，但是如果在写的同时读，同样是线程不安全的
            synchronized (this) {
                cache.put(arg, result);
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        Cache4<String, Integer> expensiveComputer = new Cache4<>(
                new ExpensiveFunction());
        Integer result = expensiveComputer.compute("666");
        System.out.println("第一次计算结果："+result);
        result = expensiveComputer.compute("666");
        System.out.println("第二次计算结果："+result);
    }
}
