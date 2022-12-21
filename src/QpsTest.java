import computable.ExpensiveFunction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guolin
 * 模拟大量请求，观测缓存效果
 */
public class QpsTest {

    static Cache12<String, Integer> expensiveComputer = new Cache12<>(new ExpensiveFunction());

    public static void main(String[] args) {

        ExecutorService service = Executors.newFixedThreadPool(17);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1050000; i++) {
            service.submit(() -> {
                Integer result = null;
                try {
                    result = expensiveComputer.compute("666");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //System.out.println(result);
            });
        }
        // 关闭线程池
        service.shutdown();

        // 如果线程池关闭则true
        while (!service.isTerminated()) {
        }

        System.out.println("总耗时："+(System.currentTimeMillis() - start));
    }
}
