import computable.Computable;
import computable.MayFail;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 缓存过期功能
 * 出于安全性考虑，缓存需要设置有效期，到期自动失效
 * 否则如果缓存一直不失效，那么会带来缓存不一致等问题
 * @param <A>
 * @param <V>
 */
public class Cache11<A, V> implements Computable<A, V> {
    // Future包装value
    private final Map<A, Future<V>> cache = new ConcurrentHashMap<>();

    private final Computable<A, V> c;

    public Cache11(Computable<A, V> c) {
        this.c = c;
    }

    public final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);

    /**
     * 不带缓存过期时间的执行
     * @param arg
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
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
                // 每个异常抛出以后，缓存都被清理掉了
                System.out.println("被取消了");
                cache.remove(arg);
                throw e;
            } catch (InterruptedException e) {
                cache.remove(arg);
                throw e;
            } catch (ExecutionException e) {
                System.out.println("计算错误，需要重试");
                cache.remove(arg);
            }
        }
    }

    /**
     * 带缓存过期时间的执行
     * @param arg
     * @param expire
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public V compute(A arg, long expire) throws ExecutionException, InterruptedException {
        // 检查超时时间是不是大于0
        if (expire > 0) {
            // 利用这个延迟线程池（Java定时调度机制），超时时间过了以后，清除缓存
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    // 清除
                    expire(arg);
                }
            }, expire, TimeUnit.MILLISECONDS);
        }
        return compute(arg);
    }

    public synchronized void expire(A key) {
        // 看一下缓存里面有没有key
        Future<V> future = cache.get(key);
        if (future != null) {
            // 如果任务时间到了，还没有完成的话，直接取消任务（任务过期）
            if (!future.isDone()) {
                System.out.println("Future任务被取消");
                // 取消任务
                future.cancel(true);
            }
            // 清除缓存
            System.out.println("过期时间到，缓存被清除");
            cache.remove(key);
        }
    }

    public static void main(String[] args) throws Exception {
        Cache11<String, Integer> expensiveComputer = new Cache11<>(new MayFail());
        // 第一个线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Integer result = expensiveComputer.compute("666", 5000L);
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
                    Integer result = expensiveComputer.compute("666");
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
                    Integer result = expensiveComputer.compute("667");
                    System.out.println("第三个线程：" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Thread.sleep(6000L);
        Integer result = expensiveComputer.compute("666");
        System.out.println("主线程的计算结果：" + result);
    }
}