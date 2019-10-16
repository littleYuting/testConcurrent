import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class testLock {
    public static void main(String[] args){
        testLocalVariable();
        testLockInterrupt();
    }

    public static void testLocalVariable(){
        // lock 是局部变量
        class MyRunnable implements Runnable {
//        class MyThread extends Thread{
            private int m = 0;
            @Override
            public void run() {
                Lock lock = new ReentrantLock();
//                lock.lock();
                if (lock.tryLock()) {
                    try {
                        System.out.println("************get the lock************");
                        for (int i = 0; i < 10; i++) {
                            m += i;
                        }
                        System.out.println("the final result : " + m);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                        System.out.println("************release the lock************");
                    }
                } else {
                    System.out.println("fail to get the lock!");
                }
            }
        }
//        Thread thread_1 = new MyThread();
//        Thread thread_2 = new MyThread();
//        thread_1.start();
//        thread_2.start();
        /** output
         * ************get the lock************
         * ************get the lock************
         * the final result : 45
         * the final result : 45
         * ************release the lock************
         * ************release the lock************
         */
        MyRunnable runnable = new MyRunnable();
        for (int i = 0; i < 2; i++) {
            new Thread(runnable).start();
        }
        /** output
         * ************get the lock************
         * ************get the lock************
         * the final result : 45
         * the final result : 90
         * ************release the lock************
         * ************release the lock************
         */
    }

    public static void testLockInterrupt(){
        Lock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lockInterruptibly();
                } catch (InterruptedException e) {
                    System.out.println(Thread.currentThread().getName() + " interrupted.");
                }
            }
        });
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.interrupt();// 子线程在阻塞时会调用 lockInterruptibly 方法，并处理其异常
    }
}
