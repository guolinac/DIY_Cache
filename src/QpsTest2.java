import computable.ExpensiveFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guolin
 * 模拟大量请求，观测缓存效果
 * 用CountDownLatch的情况
 */
public class QpsTest2 {

    static Cache12<String, Integer> expensiveComputer = new Cache12<>(new ExpensiveFunction());

    // 同步工具类：等待1个线程执行完毕，其他线程统一开始，最大压测
    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        // 线程池大小
        ExecutorService service = Executors.newFixedThreadPool(17);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            service.submit(() -> {
                Integer result = null;
                try {
                    System.out.println(Thread.currentThread().getName()+"开始等待");
                    countDownLatch.await();
                    SimpleDateFormat dateFormat = ThreadSafeFormatter.dateFormatter.get();
                    String time = dateFormat.format(new Date());
                    System.out.println(Thread.currentThread().getName()+"   "+time+"被放行");
                    result = expensiveComputer.compute("666");

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(result);
            });
        }

        Thread.sleep(5000);
        countDownLatch.countDown();
        service.shutdown();
    }
}

class ThreadSafeFormatter {

    public static ThreadLocal<SimpleDateFormat> dateFormatter = new ThreadLocal<SimpleDateFormat>() {

        //每个线程会调用本方法一次，用于初始化
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("mm:ss");
        }

        //首次调用本方法时，会调用initialValue()；后面的调用会返回第一次创建的值
        @Override
        public SimpleDateFormat get() {
            return super.get();
        }
    };
}