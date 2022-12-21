import computable.Computable;
import computable.ExpensiveFunction;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by guolin
 * 描述：     利用putIfAbsent优化Future小概率的重复计算
 */

public class Cache8<A,V> implements Computable<A,V> {

    private final Map<A, Future<V>> cache = new ConcurrentHashMap<>();

    private final Computable<A, V> c;

    public Cache8(Computable<A, V> c) {
        this.c = c;
    }

    @Override
    public V compute(A arg) throws Exception {
        Future<V> f = cache.get(arg);
        if (f == null) {
            Callable<V> callable = new Callable<V>() {
                @Override
                public V call() throws Exception {
                    return c.compute(arg);
                }
            };
            FutureTask<V> ft = new FutureTask<>(callable);
            // cache.putIfAbsent(arg, ft)如果ConcurrentHashMap里面没arg这个key的时候，则返回null，就不会运行计算函数
            // 如果ConcurrentHashMap里有值，cache.putIfAbsent(arg, ft)就不会往里面重复放值了，而且会返回当前的值
            f = cache.putIfAbsent(arg, ft);
            if (f == null) {
                f = ft;
                System.out.println("从FutureTask调用了计算函数");
                ft.run();
            }
        }
        return f.get();
    }

    public static void main(String[] args) throws Exception {
        Cache8<String, Integer> expensiveCompute = new Cache8<>(new ExpensiveFunction());
        // 第一个线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer result = expensiveCompute.compute("667");
                    System.out.println("第一个线程：" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 第二个线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer result = expensiveCompute.compute("667");
                    System.out.println("第二个线程：" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // 第三个线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer result = expensiveCompute.compute("666");
                    System.out.println("第三个线程：" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
