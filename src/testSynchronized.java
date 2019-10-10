import java.text.SimpleDateFormat;

public class testSynchronized {

    public static void main(String[] args){
//        testObjectSynchronize();
//        testClassSynchronize();
        testObjectAndClassSynchronize();
    }

    public static void testObjectSynchronize(){
        class SyncThread implements Runnable{
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            @Override
            public void run(){
                String threadName = Thread.currentThread().getName();
                if (threadName.startsWith("A")) {
                    async();
                } else if (threadName.startsWith("B")){
                    sync1();
                } else if (threadName.startsWith("C")) {
                    sync2();
                }
            }
            // 异步方法
            private void async(){
                try {
                    System.out.println(Thread.currentThread().getName() + "_async_start : " + sdf.format(System.currentTimeMillis()));
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName() + "_async_end : " + sdf.format(System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 方法中有 synchronized(this|object) {} 同步代码块, 此外的代码依旧是异步的
            private void sync1(){
                System.out.println(Thread.currentThread().getName() + "_sync1 : " + sdf.format(System.currentTimeMillis()));// 异步
                synchronized (this) {// 同步
                    try {
                        System.out.println(Thread.currentThread().getName() + "_sync1_start : " + sdf.format(System.currentTimeMillis()));
                        Thread.sleep(2000);
                        System.out.println(Thread.currentThread().getName() + "_sync1_end : " + sdf.format(System.currentTimeMillis()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // synchronized 修饰非静态方法，同步作用范围是整个方法
            // 补充： 内部类不能有 static 方法
            private synchronized void sync2() {
                System.out.println(Thread.currentThread().getName() + "_sync2 : " + sdf.format(System.currentTimeMillis()));
                try {
                    System.out.println(Thread.currentThread().getName() + "_sync2_start : " + sdf.format(System.currentTimeMillis()));
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName() + "_sync2_end : " + sdf.format(System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        /**
         * Same object
         */
//        SyncThread syncThread = new SyncThread();
//        Thread A_thread1 = new Thread(syncThread, "A_thread1");
//        Thread A_thread2 = new Thread(syncThread, "A_thread2");
//        Thread B_thread1 = new Thread(syncThread, "B_thread1");
//        Thread B_thread2 = new Thread(syncThread, "B_thread2");
//        Thread C_thread1 = new Thread(syncThread, "C_thread1");
//        Thread C_thread2 = new Thread(syncThread, "C_thread2");
//        A_thread1.start();
//        A_thread2.start();
//        B_thread1.start();
//        B_thread2.start();
//        C_thread1.start();
//        C_thread2.start();
        /** output
         * A_thread2_async_start : 11:32
         * A_thread1_async_start : 11:32
         * C_thread1_sync2 : 11:32
         * B_thread1_sync1 : 11:32
         * B_thread2_sync1 : 11:32
         * C_thread1_sync2_start : 11:32
         * A_thread2_async_end : 11:34
         * A_thread1_async_end : 11:34
         * C_thread1_sync2_end : 11:34
         * B_thread2_sync1_start : 11:34
         * B_thread2_sync1_end : 11:36
         * B_thread1_sync1_start : 11:36
         * B_thread1_sync1_end : 11:38
         * C_thread2_sync2 : 11:38
         * C_thread2_sync2_start : 11:38
         * C_thread2_sync2_end : 11:40
         *  explain: B 类和 C 类线程同步，即 类中 synchronized(this|object){} 代码块和 synchronized 修饰非静态方法获取的是同一
         *  个锁，即该类的对象的对象锁！
         */
        /**
         * Different object
         */
        Thread A_thread1 = new Thread(new SyncThread(), "A_thread1");
        Thread A_thread2 = new Thread(new SyncThread(), "A_thread2");
        Thread B_thread1 = new Thread(new SyncThread(), "B_thread1");
        Thread B_thread2 = new Thread(new SyncThread(), "B_thread2");
        Thread C_thread1 = new Thread(new SyncThread(), "C_thread1");
        Thread C_thread2 = new Thread(new SyncThread(), "C_thread2");
        A_thread1.start();
        A_thread2.start();
        B_thread1.start();
        B_thread2.start();
        C_thread1.start();
        C_thread2.start();
        /** output
         * A_thread2_async_start : 19:12
         * A_thread1_async_start : 19:12
         * C_thread2_sync2 : 19:12
         * B_thread2_sync1 : 19:12
         * B_thread1_sync1 : 19:12
         * C_thread1_sync2 : 19:12
         * C_thread2_sync2_start : 19:12
         * B_thread1_sync1_start : 19:12
         * B_thread2_sync1_start : 19:12
         * C_thread1_sync2_start : 19:12
         * A_thread2_async_end : 19:14
         * B_thread1_sync1_end : 19:14
         * A_thread1_async_end : 19:14
         * C_thread2_sync2_end : 19:14
         * B_thread2_sync1_end : 19:14
         * C_thread1_sync2_end : 19:14
         * explain : 两个线程访问不同对象的 synchronized(this|object){} 代码块和 synchronized 修饰非静态方法 是异步的，同一个
         * 类的不同对象的对象锁互不干扰
         */
    }

    public static void testClassSynchronize(){
        /**
         * Same object
         */
//        SyncThread syncThread = new SyncThread();
//        Thread A_thread1 = new Thread(syncThread, "A_thread1");
//        Thread A_thread2 = new Thread(syncThread, "A_thread2");
//        Thread B_thread1 = new Thread(syncThread, "B_thread1");
//        Thread B_thread2 = new Thread(syncThread, "B_thread2");
//        Thread C_thread1 = new Thread(syncThread, "C_thread1");
//        Thread C_thread2 = new Thread(syncThread, "C_thread2");
//        A_thread1.start();
//        A_thread2.start();
//        B_thread1.start();
//        B_thread2.start();
//        C_thread1.start();
//        C_thread2.start();
        /** output
         * B_thread1_sync1 : 34:51
         * B_thread2_sync1 : 34:51
         * A_thread2_async_start : 34:51
         * A_thread1_async_start : 34:51
         * C_thread1_sync2 : 34:51
         * C_thread1_sync2_start : 34:51
         * A_thread2_async_end : 34:53
         * A_thread1_async_end : 34:53
         * C_thread1_sync2_end : 34:53
         * B_thread2_sync1_start : 34:53
         * B_thread2_sync1_end : 34:55
         * B_thread1_sync1_start : 34:55
         * B_thread1_sync1_end : 34:57
         * C_thread2_sync2 : 34:57
         * C_thread2_sync2_start : 34:57
         * C_thread2_sync2_end : 34:59
         * explain : 在同一对象的情况下，synchronized(this|object){} 代码块 & synchronized 修饰非静态方法 与
         * synchronized(类.class){} 代码块 & synchronized 修饰静态方法的实现效果相同
         */
        /**
         * Different object
         */
        Thread A_thread1 = new Thread(new SyncThread(), "A_thread1");
        Thread A_thread2 = new Thread(new SyncThread(), "A_thread2");
        Thread B_thread1 = new Thread(new SyncThread(), "B_thread1");
        Thread B_thread2 = new Thread(new SyncThread(), "B_thread2");
        Thread C_thread1 = new Thread(new SyncThread(), "C_thread1");
        Thread C_thread2 = new Thread(new SyncThread(), "C_thread2");
        A_thread1.start();
        A_thread2.start();
        B_thread1.start();
        B_thread2.start();
        C_thread1.start();
        C_thread2.start();
        /** output
         * A_thread1_async_start : 44:29
         * C_thread2_sync2 : 44:29
         * B_thread2_sync1 : 44:29
         * B_thread1_sync1 : 44:29
         * A_thread2_async_start : 44:29
         * C_thread2_sync2_start : 44:29
         * C_thread2_sync2_end : 44:31
         * A_thread1_async_end : 44:31
         * A_thread2_async_end : 44:31
         * B_thread1_sync1_start : 44:31
         * B_thread1_sync1_end : 44:33
         * B_thread2_sync1_start : 44:33
         * B_thread2_sync1_end : 44:35
         * C_thread1_sync2 : 44:35
         * C_thread1_sync2_start : 44:35
         * C_thread1_sync2_end : 44:37
         * explain : 两个线程访问不同对象的 synchronized(类.class){} 代码块 & synchronized 修饰静态方法 同步，两者获取的是类锁，
         * 即对于同一个类的不同对象的类锁是同一个
         */
    }

    public static void testObjectAndClassSynchronize(){
        Thread E_thread1 = new Thread(new SyncThread(), "E_thread1");// synchronized(this){}
        Thread B_thread1 = new Thread(new SyncThread(), "B_thread1");// synchronized(类.class){}
        Thread C_thread1 = new Thread(new SyncThread(), "C_thread1");//synchronized 修饰静态方法
        Thread D_thread1 = new Thread(new SyncThread(), "D_thread1");//synchronized 修饰非静态方法
        E_thread1.start();
        B_thread1.start();
        C_thread1.start();
        D_thread1.start();
        /** output
         * E_thread1_sync4 : 21:09
         * B_thread1_sync1 : 21:09
         * C_thread1_sync2 : 21:09
         * D_thread1_sync3 : 21:09
         * C_thread1_sync2_start : 21:09
         * E_thread1_sync4_start : 21:09
         * D_thread1_sync3_start : 21:09
         * E_thread1_sync4_end : 21:11
         * C_thread1_sync2_end : 21:11
         * D_thread1_sync3_end : 21:11
         * B_thread1_sync1_start : 21:11
         * B_thread1_sync1_end : 21:13
         * explain : 对象锁和类锁是互相独立的
         */
    }
}

class SyncThread implements Runnable{
    static SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    @Override
    public void run(){
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("A")) {
            async();
        } else if (threadName.startsWith("B")){
            sync1();
        } else if (threadName.startsWith("C")) {
            sync2();
        } else if (threadName.startsWith("D")) {
            sync3();
        } else if (threadName.startsWith("E")) {
            sync4();
        }
    }
    // 异步方法
    private void async(){
        try {
            System.out.println(Thread.currentThread().getName() + "_async_start : " + sdf.format(System.currentTimeMillis()));
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + "_async_end : " + sdf.format(System.currentTimeMillis()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // 方法中有 synchronized(SyncThread.class) {} 同步代码块
    private void sync1(){
        System.out.println(Thread.currentThread().getName() + "_sync1 : " + sdf.format(System.currentTimeMillis()));// 异步
        synchronized (SyncThread.class) {// 同步
            try {
                System.out.println(Thread.currentThread().getName() + "_sync1_start : " + sdf.format(System.currentTimeMillis()));
                Thread.sleep(2000);
                System.out.println(Thread.currentThread().getName() + "_sync1_end : " + sdf.format(System.currentTimeMillis()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    // synchronized 修饰静态方法
    private synchronized static void sync2() {
        System.out.println(Thread.currentThread().getName() + "_sync2 : " + sdf.format(System.currentTimeMillis()));
        try {
            System.out.println(Thread.currentThread().getName() + "_sync2_start : " + sdf.format(System.currentTimeMillis()));
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + "_sync2_end : " + sdf.format(System.currentTimeMillis()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // synchronized 修饰非静态方法
    private synchronized void sync3() {
        System.out.println(Thread.currentThread().getName() + "_sync3 : " + sdf.format(System.currentTimeMillis()));
        try {
            System.out.println(Thread.currentThread().getName() + "_sync3_start : " + sdf.format(System.currentTimeMillis()));
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + "_sync3_end : " + sdf.format(System.currentTimeMillis()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // 方法中有 synchronized(this|object) {} 同步代码块, 此外的代码依旧是异步的
    private void sync4(){
        System.out.println(Thread.currentThread().getName() + "_sync4 : " + sdf.format(System.currentTimeMillis()));
        synchronized (this) {
            try {
                System.out.println(Thread.currentThread().getName() + "_sync4_start : " + sdf.format(System.currentTimeMillis()));
                Thread.sleep(2000);
                System.out.println(Thread.currentThread().getName() + "_sync4_end : " + sdf.format(System.currentTimeMillis()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}