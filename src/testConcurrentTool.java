import java.util.concurrent.*;

public class testConcurrentTool {

    public static void main(String[] args){
        // 问题： CountDownLatch 的不可重用性案例没表现出来
        testCountDownLatch();
//        testCyclicBarrier();
        /** 对比 闭锁 CountDownLatch 和 回环栅栏  CyclicBarrier
         *  相同点 ： 等待所有线程任务执行完之后再执行其他任务
         *  不同点：
         *  1）线程间的互相影响：CountDownLatch 等待的是一组线程执行完之后，才开始执行其他任务，而 CyclicBarrier 的等待是一组线程间的互相等待；
         *  2）可复用：CyclicBarrier 可复用， CountDownLatch 不可复用；
         *  3）计数：CyclicBarrier 递增， CountDownLatch 递减；
         */
//        testSemaphore(); // Semaphore 和 锁类似，一般用于控制对某组资源的访问权限
    }

    public static void testCountDownLatch(){
        class MyThread extends Thread{
            private CountDownLatch readyLatch;
            private CountDownLatch startLatch;
            public MyThread (CountDownLatch readyLatch, CountDownLatch startLatch) {
                this.readyLatch = readyLatch;
                this.startLatch = startLatch;
            }
            @Override
            public void run(){
                try {
                    System.out.println("子线程"+Thread.currentThread().getName()+"正在执行");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("子线程"+Thread.currentThread().getName()+"执行完毕");
                readyLatch.countDown();// 若把此处注释，则 count 无法变成0，程序会在 await 处暂停执行；
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("子线程"+Thread.currentThread().getName()+"开始执行主线程任务");
            }
        }
        int thread_num = 3;
        CountDownLatch readyLatch = new CountDownLatch(thread_num);
        CountDownLatch startLatch = new CountDownLatch(1);
        for (int i = 0; i < thread_num; i++) {
            MyThread thread = new MyThread(readyLatch, startLatch);
            thread.start();
//            if (i != 0) {
//                thread.start();
//            }
        }
//      latch.await();// 返回 void ， 调用 await() 方法的线程会被挂起，ta 会等待直到 count = 0 才继续执行
//      latch.await(5000, TimeUnit.MILLISECONDS);//返回 boolean , 有时间参数 ，相比无返回值的 await ，区别在于等待一定时间后，如果 count 还没变为 0 的话会继续执行
        System.out.println("等待 " + thread_num +" 个子线程执行完毕");
        try {
            readyLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startLatch.countDown();
        System.out.println(readyLatch.getCount() + " 个子线程未执行完毕");
        // CountDownLatch 不支持重用
//        for (int i = 0; i < thread_num; i++) {
//            new MyThread(readyLatch, startLatch).start();
//        }
    }

    public static void testCyclicBarrier(){
        class Writer extends Thread{
            private CyclicBarrier cyclicBarrier;
            public Writer (CyclicBarrier cyclicBarrier) {
                this.cyclicBarrier = cyclicBarrier;
            }
            @Override
            public void run(){
                System.out.println("子线程"+Thread.currentThread().getName()+"正在写入数据......");
                try {
                    Thread.sleep(2000); // 以睡眠来模拟写入数据操作
                    System.out.println("子线程"+Thread.currentThread().getName()+"写入数据完毕，等待其他子线程写入");
                    // one test:
//                    cyclicBarrier.await(); // 所有线程都到达 barrier 状态再同时执行后续任务
                    // two test:
                    try {
                        cyclicBarrier.await(3000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("所有子线程写入完毕，继续处理其他任务......");
            }
        }
        int barrier_num = 3;
        CyclicBarrier barrier = new CyclicBarrier(barrier_num, new Runnable() {
            @Override
            public void run() {
                System.out.println("在子线程处理完毕后，处理的其他任务线程： " + Thread.currentThread().getName());
            }
        });
        // three test:
         System.out.println("CyclicBarrier 可重用");
        // one test:
        for (int i = 0; i < barrier_num; i++) {
            new Writer(barrier).start();
        }
        // two test:
        for (int i = 0; i < barrier_num; i++) {
            if (i != barrier_num -1) {
                new Writer(barrier).start();
            } else {
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new Writer(barrier).start();
            }
        }
        /** one test's output
         * 子线程Thread-0正在写入数据......
         * 子线程Thread-1正在写入数据......
         * 子线程Thread-2正在写入数据......
         * 子线程Thread-2写入数据完毕，等待其他子线程写入
         * 子线程Thread-1写入数据完毕，等待其他子线程写入
         * 子线程Thread-0写入数据完毕，等待其他子线程写入
         * 在子线程处理完毕后，处理的其他任务线程： Thread-1
         * 所有子线程写入完毕，继续处理其他任务......
         * 所有子线程写入完毕，继续处理其他任务......
         * 所有子线程写入完毕，继续处理其他任务......
         * explain : 当所有子线程都到达 barrier 状态后，会选择其中一个子线程去执行 runnable
         */
        /** two test's output
         *子线程Thread-1正在写入数据......
         * 子线程Thread-0正在写入数据......
         * 子线程Thread-1写入数据完毕，等待其他子线程写入
         * 子线程Thread-0写入数据完毕，等待其他子线程写入
         * java.util.concurrent.TimeoutException
         * 	at java.util.concurrent.CyclicBarrier.dowait(CyclicBarrier.java:257)
         * 	at java.util.concurrent.CyclicBarrier.await(CyclicBarrier.java:435)
         * 	at testConcurrentTool$1Writer.run(testConcurrentTool.java:65)
         * 所有子线程写入完毕，继续处理其他任务......
         * java.util.concurrent.BrokenBarrierException
         * 	at java.util.concurrent.CyclicBarrier.dowait(CyclicBarrier.java:250)
         * 	at java.util.concurrent.CyclicBarrier.await(CyclicBarrier.java:435)
         * 	at testConcurrentTool$1Writer.run(testConcurrentTool.java:65)
         * 所有子线程写入完毕，继续处理其他任务......
         * 子线程Thread-2正在写入数据......
         * 子线程Thread-2写入数据完毕，等待其他子线程写入
         * java.util.concurrent.BrokenBarrierException
         * 	at java.util.concurrent.CyclicBarrier.dowait(CyclicBarrier.java:207)
         * 所有子线程写入完毕，继续处理其他任务......
         * 	at java.util.concurrent.CyclicBarrier.await(CyclicBarrier.java:435)
         * 	at testConcurrentTool$1Writer.run(testConcurrentTool.java:65)
         * 	explain : 让一组线程等待至一定时间，若还有线程没有到达 barrier 状态就直接抛出异常，并让到达 barrier 的线程执行后续任务
         */
    }

    public static void testSemaphore(){
        class Worker extends Thread {
            int num;
            Semaphore semaphore;
            Worker (int num, Semaphore semaphore) {
                this.num = num;
                this.semaphore = semaphore;
            }
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                    System.out.println("worker " + num + " 占用一个机器在生产");
                    Thread.sleep(2000);
                    System.out.println("worker " + num + " 释放出机器");
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        int num = 5;
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < num; i++) {
            new Worker(i, semaphore).start();
        }
        /** output
         * worker 0 占用一个机器在生产
         * worker 1 占用一个机器在生产
         * worker 4 占用一个机器在生产
         * worker 1 释放出机器
         * worker 0 释放出机器
         * worker 4 释放出机器
         * worker 2 占用一个机器在生产
         * worker 3 占用一个机器在生产
         * worker 2 释放出机器
         * worker 3 释放出机器
         */
    }
}
