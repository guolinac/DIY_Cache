import computable.Computable;
import computable.ExpensiveFunction;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by guolin
 * 描述：     利用Future，避免重复计算
 */

public class Cache7<A,V> implements Computable<A,V> {

    private final Map<A, Future<V>> cache = new ConcurrentHashMap<>();

    private final Computable<A, V> c;

    public Cache7(Computable<A, V> c) {
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
            f = ft;
            // 先把FutureTask这个任务放进去，如果后续有别的相同计算，就不会重复计算逻辑
            cache.put(arg, ft);
            System.out.println("从FutureTask调用了计算函数");

            // run()就是在内部执行callable，这个方法执行完以后，相当于内部就有值了
            ft.run();
        }
        return f.get();
    }

    public static void main(String[] args) throws Exception {
        Cache7<String, Integer> expensiveCompute = new Cache7<>(new ExpensiveFunction());
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
