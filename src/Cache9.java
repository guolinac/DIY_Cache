import computable.Computable;
import computable.MayFail;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 用while(true)保证计算错误的情况下还是可以重复计算，但是仍然有缓存污染问题
 * @param <A>
 * @param <V>
 */
public class Cache9<A, V> implements Computable<A, V> {
    // Future包装value
    private final Map<A, Future<V>> cache = new ConcurrentHashMap<>();

    private final Computable<A, V> c;

    public Cache9(Computable<A, V> c) {
        this.c = c;
    }


    @Override
    public V compute(A arg) throws ExecutionException, InterruptedException {
        // 由于ConcurrentHashMap的可见性，当前一个线程写入了cache.put(arg, ft);的时候，后面的缓存就会得到f不是null
        while (true) {
            Future<V> f = cache.get(arg);
            if (f == null) {
                // 新建一个任务
                Callable<V> callable = new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        return c.compute(arg);
                    }
                };

                FutureTask<V> ft = new FutureTask<>(callable);
                f = cache.putIfAbsent(arg, ft);

                if (f == null) {
                    f = ft;
                    System.out.println("从FutureTask调用了计算函数");
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                System.out.println("被取消了");
                throw e;
            } catch (InterruptedException e) {
                throw e;
            } catch (ExecutionException e) {
                System.out.println("计算错误，需要重试");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Cache9<String, Integer> expensiveCompute = new Cache9<>(new MayFail());
        // 第一个线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer result = expensiveCompute.compute("666");
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
                    Integer result = expensiveCompute.compute("666");
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
                    Integer result = expensiveCompute.compute("667");
                    System.out.println("第三个线程：" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}