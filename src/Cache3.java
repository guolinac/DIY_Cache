import computable.Computable;
import computable.ExpensiveFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guolin
 * 描述：     用装饰者模式，给计算器自动添加缓存功能
 */
public class Cache3<A,V> implements Computable<A,V> {

    private final Map<A, V> cache = new HashMap();

    private  final Computable<A,V> c;

    public Cache3(Computable<A, V> c) {
        this.c = c;
    }

    @Override
    public synchronized V compute(A arg) throws Exception {
        System.out.println("进入缓存机制");
        V result = cache.get(arg);
        if (result == null) {
            // 由于装饰器模式，实际上这里调用的是Computable<A,V>接口的compute(arg)方法，也就是ExpensiveFunction实现类的compute(arg)方法
            result = c.compute(arg);
            cache.put(arg, result);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        Cache3<String, Integer> expensiveCompute = new Cache3<>(new ExpensiveFunction());

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
