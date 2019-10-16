import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class testVolatile {
    public static void main(String[] args){
//        testAutoAddOperate();
        testBooleanUpdate();
    }
    public static void testBooleanUpdate(){
        class MyThread extends Thread{
//            private volatile boolean isRunning = true; // 不加 volatile 会使程序陷入死循环
            private boolean isRunning = true; // 不加 volatile 会使程序陷入死循环
            int m;
            public void setRunning(boolean isRunning){
                this.isRunning = isRunning;
            }
            public boolean isRunning(){
                return isRunning;
            }
            @Override
            public void run(){
                System.out.println("***********start***********");
                while (isRunning) {
                    int a = 2;
                    int b = 3;
                    int c = a + b;
                    m = c;
                    System.out.println("线程还未从主内存更新 isRunning 状态");// 在 run 中加入别的方法，会使 cpu 有时间去更新变量值，即 jvm 会尽力保证内存的可见性；
                    // 在 isRunning 状态更新前会一直输出此语句；
                }
                System.out.println(m);
                System.out.println("***********end***********");
            }
        }
        MyThread myThread = new MyThread();
        myThread.start();
        try {
            myThread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        myThread.setRunning(false);
        System.out.println("myThread's isRunning change to false!");
        /** output
         * ***********start***********
         * myThread's isRunning change to false!
         * 5
         * ***********end***********
         */
    }
    public static void testAutoAddOperate(){
        class AutoAdd implements Runnable{
            public volatile int inc = 0;
            Lock lock = new ReentrantLock();
            public AtomicInteger auto_inc = new AtomicInteger();
            @Override
//            public void run(){
////                for (int j = 0; j < 1000; j++) {
////                    lock.lock();
////                    try {
////                        inc++;
////                    } finally {
////                        lock.unlock();
////                    }
////                }
////                System.out.println("the final result : " + inc);
//                lock.lock();
//                try {
//                    for (int j = 0; j < 1000; j++) {
//                        inc++;
//                    }
//                } finally {
//                    lock.unlock();
//                }
//                System.out.println("the final result : " + inc);
//                }
//            public void run(){
////                synchronized (this) {
////                    for (int j = 0; j < 1000; j++) {
////                        lock.lock();
////                        try {
////                            inc++;
////                        } finally {
////                            lock.unlock();
////                        }
////                    }
////                    System.out.println("the final result : " + inc);
////                }
////            }
            public void run(){
                for (int j = 0; j < 1000; j++) {
                    System.out.println("the final result : " + auto_inc.getAndIncrement());

                }
            }
        }
        AutoAdd test = new AutoAdd();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(test);
            thread.start();
        }

        /** output
         * the final result : 2000
         * the final result : 4000
         * the final result : 3000
         * the final result : 2000
         * the final result : 6000
         * the final result : 5000
         * the final result : 7000
         * the final result : 8000
         * the final result : 9545
         * the final result : 9730
         * summary ： 结果不大于 10000，volatile 只能保证可见性，不能保证对共享变量的操作是原子性的
         */
        // 修改一 ： 在 run 方法里加 synchronized 锁
        /**output
         * the final result : 1000
         * the final result : 2000
         * the final result : 3000
         * the final result : 4000
         * the final result : 5000
         * the final result : 6000
         * the final result : 7000
         * the final result : 8000
         * the final result : 9000
         * the final result : 10000
         */
        // 修改二 ： 加 lock 锁, lock increase 操作
        /**output
         *the final result : 4948
         * the final result : 6491
         * the final result : 8207
         * the final result : 8496
         * the final result : 8497
         * the final result : 8991
         * the final result : 9045
         * the final result : 9205
         * the final result : 9849
         * the final result : 10000
         */
        // 修改三 ： 加 lock 锁, lock for 操作
        /** output
         * the final result : 1000
         * the final result : 2000
         * the final result : 3000
         * the final result : 4096
         * the final result : 5000
         * the final result : 6000
         * the final result : 7000
         * the final result : 8000
         * the final result : 9000
         * the final result : 10000
         */
        // 修改4 ： 引用 java.util.concurrent.atomic 原子类操作
        /** output
         * 此处省略，输出 10000 次，从 0 ~ 9999
         */

    }

}
