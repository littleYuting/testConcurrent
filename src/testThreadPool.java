import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/** 参考链接： https://yq.aliyun.com/articles/680584
 * 简单的手写线程池，貌似是针对 coreSize = maxSize 做的处理，而对 maxSize > coreSize 的情况没有处理，
 * 目前运行无终止，后续继续优化；
 */
public class testThreadPool {

    public static void main(String[] args ){
        CustomThreadPool();
    }

    public static void CustomThreadPool(){
        TestPool testPool = new TestPool(2,2,new ArrayBlockingQueue<>(3));
        for (int i = 0; i < 100; i++) {
            final int j = i;
            System.out.println("i = " + i + " " + Thread.currentThread().getName());
            testPool.execute(()->{
                try {
                    Thread.sleep(100);
                    System.out.println("睡 0.1 秒，完成 ："  + j);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

class TestPool{

    private int coreSize;
    private int maxSize;
    private AtomicInteger running = new AtomicInteger(0);
    private BlockingQueue<Runnable> queue;

    public TestPool(int coreSize, int maxSize, ArrayBlockingQueue<Runnable> queue){
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.queue = queue;
    }

    public void execute(Runnable runnable){
        if (running.get() < coreSize) {
            if (!addWorker(runnable)) {
                reject();
            }
        } else {
            System.out.println("当前队列大小： " + queue.size());
            if (!queue.offer(runnable)) {
                System.out.println("offer失败，当前线程数： " + running.get());//offer是有返回boolean类型的，put()不可以；
                if (!addWorker(runnable)) {
                    reject();
                }
            }
        }
    }

    private void reject(){
        throw new RuntimeException("超出大小，线程数： " + running.get() + " , 队列大小 ： " + queue.size());
    }

    private boolean addWorker(Runnable runnable){
        if (running.get() >= maxSize) {
            return false;
        }
        Worker worker = new Worker(runnable);
        worker.start();
        return true;
    }

    private class Worker extends Thread{
        private Runnable runnable;
        public Worker(Runnable runnable){
            this.runnable = runnable;
            System.out.println("创建线程，当前线程数 ： " + running.incrementAndGet());
        }
        @Override
        public void run() {
            try{
                while (true) {
                    runnable.run();
                    System.out.println("运行结束，当前线程数 ： " + running.get());
                    if (running.get() > coreSize) {
                        break;
                    } else {
                        try {
                            System.out.println("000000: 队列大小" + queue.size());
                            runnable = queue.take();
                            System.out.println("11111111: 队列大小" + queue.size());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                running.decrementAndGet();
                System.out.println("结束线程，当前线程数： " + running.get());
            }
        }
    }
}