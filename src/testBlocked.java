import java.text.SimpleDateFormat;

public class testBlocked {
    static SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    public static void main(String[] args){
        System.out.println(sdf.format(System.currentTimeMillis()) + " : main start");
//        testSleep();
//        testWait();
//        testJoin1();
//        testJoin2();
        testYield();
        System.out.println(sdf.format(System.currentTimeMillis()) + " : main end");
    }

    public static void testSleep(){
        class Thread1 implements Runnable{
            @Override
            public void run(){
                synchronized (sdf) {
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread1 start");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread1 end");
                }
            }
        }
        class Thread2 implements Runnable{
            @Override
            public void run(){
                synchronized (sdf) {
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread2 start");
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread2 end");
                }
            }
        }
        new Thread(new Thread1()).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new Thread2()).start();
        // 主线程 sleep , 子线程 无 sleep
        /** outPut
         * 07:14 : main start
         * 07:14 : thread1 start
         * 07:14 : thread1 end
         * 07:16 : main end
         * 07:16 : thread2 start
         * 07:16 : thread2 end
         */
        // 主线程 sleep + 子线程 sleep， 主线程的 sleep 不会影响 子线程
        /** outPut
         * 09:13 : main start
         * 09:13 : thread1 start
         * 09:15 : main end
         * 09:15 : thread2 start
         * 09:15 : thread2 end
         * 09:16 : thread1 end
         */
        // 给子线程加锁 synchronized，线程在休眠时不会释放锁
        /** output
         * 22:06 : main start
         * 22:06 : thread1 start
         * 22:08 : main end
         * 22:09 : thread1 end
         * 22:09 : thread2 start
         * 22:09 : thread2 end
         */
    }

    public static void testWait(){
        class Thread1 implements Runnable{
            @Override
            public void run(){
                synchronized (sdf) {
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread1 start");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        System.out.println(sdf.format(System.currentTimeMillis()) + " : begin to wait");
                        sdf.wait();
                        System.out.println(sdf.format(System.currentTimeMillis()) + " : end waiting, then restart");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sdf.notify();
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread1 end");
                }
            }
        }
        class Thread2 implements Runnable{
            @Override
            public void run(){
                synchronized (sdf) {
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread2 start");
                    sdf.notify(); // 两个 notify 同时作用对代码无影响；
                    try {
//                        Thread.sleep(5000);
                        sdf.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    sdf.notify();
                    System.out.println(sdf.format(System.currentTimeMillis()) + " : thread2 end");
                }
            }
        }
        new Thread(new Thread1()).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new Thread2()).start();
        //  wait 后，锁会被释放，若不唤醒，线程将一直阻塞
        /** output
         * 30:09 : main start
         * 30:09 : thread1 start
         * 30:11 : main end
         * 30:12 : begin to wait
         * 30:12 : thread2 start
         * 30:17 : thread2 end
         * ............ 运行无终止
         */
        // notify Thread.sleep(5000);
        /** output
         * 35:52 : main start
         * 35:52 : thread1 start
         * 35:54 : main end
         * 35:55 : begin to wait
         * 35:55 : thread2 start
         * 36:00 : thread2 end
         * 36:00 : end waiting, then restart
         * 36:04 : thread1 end
         */
        // notify sdf.wait()
        /** output
         * 13:05 : main start
         * 13:05 : thread1 start
         * 13:07 : main end
         * 13:08 : begin to wait
         * 13:08 : thread2 start
         * 13:08 : end waiting, then restart
         * 13:12 : thread1 end
         * 13:12 : thread2 end
         */
    }

    public static void testYield(){
        // 暂时让渡 cpu 调度权，再参与抢夺
        class Thread1 implements Runnable{
            @Override
            public void run(){
                for (int i = 0; i < 5; i++) {
                    System.out.println(Thread.currentThread().getName() + " : " + i);
                    if (i == 2) {
                        Thread.yield();
                    }
                }
            }
        }
        class Thread2 implements Runnable{
            @Override
            public void run(){
                for (int i = 0; i < 5; i++) {
                    System.out.println(Thread.currentThread().getName() + " : " + i);
                    if (i == 3) {
                        Thread.yield();
                    }
                }
            }
        }
        Thread thread1 = new Thread(new Thread1());
        Thread thread2 = new Thread(new Thread2());
        thread1.start();
        thread2.start();
        /** output
         * 45:03 : main start
         * 45:03 : main end
         * Thread-0 : 0
         * Thread-1 : 0
         * Thread-0 : 1
         * Thread-1 : 1
         * Thread-1 : 2
         * Thread-0 : 2
         * Thread-1 : 3
         * Thread-0 : 3
         * Thread-1 : 4
         * Thread-0 : 4
         */
    }
    public static void testJoin1(){
        class Thread1 implements Runnable{
            @Override
            public void run(){
                System.out.println(sdf.format(System.currentTimeMillis()) + " : thread1 start");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(sdf.format(System.currentTimeMillis()) + " : thread1 end");
            }
        }
        Thread thread = new Thread(new Thread1());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // no join
        /** output
         * 28:00 : main start
         * 28:00 : main end
         * 28:00 : thread1 start
         * 28:03 : thread1 end
         */
        // add join
        /** output
         * 28:51 : main start
         * 28:51 : thread1 start
         * 28:54 : thread1 end
         * 28:54 : main end
         */
    }
    public static void testJoin2(){
        // 在被 join 的线程执行前，两个线程交替进行，而主线程处于等待状态，直到被 join 的线程执行完毕，主线程继续执行；
        class JoinThread extends Thread{
            public JoinThread(String name){
                super(name);
            }
            @Override
            public void run(){
                for (int i = 0; i < 10; i++) {
                    System.out.println(getName() + " : " + i);
                }
            }
        }
        new JoinThread("新线程").start();
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + ":" + i);
            if (i == 3) {
                JoinThread thread = new JoinThread("被 join 的线程");
                thread.start();
                try{
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        /** output
         * 30:36 : main start
         * main:0
         * main:1
         * main:2
         * main:3
         * 新线程 : 0
         * 新线程 : 1
         * 新线程 : 2
         * 新线程 : 3
         * 新线程 : 4
         * 新线程 : 5
         * 新线程 : 6
         * 被 join 的线程 : 0
         * 新线程 : 7
         * 新线程 : 8
         * 新线程 : 9
         * 被 join 的线程 : 1
         * 被 join 的线程 : 2
         * 被 join 的线程 : 3
         * 被 join 的线程 : 4
         * 被 join 的线程 : 5
         * 被 join 的线程 : 6
         * 被 join 的线程 : 7
         * 被 join 的线程 : 8
         * 被 join 的线程 : 9
         * main:4
         * main:5
         * main:6
         * main:7
         * main:8
         * main:9
         * 30:36 : main end
         */

    }
}
