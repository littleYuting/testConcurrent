import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class creatThread {
    public static void main(String[] args){
//        testFirstThread();
//        testSecondThread();
//        testThirdThread();
    }

    public static void testFirstThread(){
        // 使用继承子 Thread 类的子类来创建线程类时，多个线程无法共享线程类的实例变量；
        for (int i = 0; i < 20; i++) {
            System.out.println(Thread.currentThread().getName() + ":" + i);
            if (i == 3) {
                FirstThread thread1 = new FirstThread();
                FirstThread thread2 = new FirstThread();
                thread1.start();
                thread2.start();
            }
        }
    }

    public static void testSecondThread(){
        // 使用 Runnable 接口的方式创建多个线程可以共享线程类的实例变量，此方式创建的 Runnable 对象只是线程的 target，多个线程可以共享一个 target；
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + ":" + i);
            if (i == 3) {
                SecondThread thread1 = new SecondThread();
                Thread thread11 = new Thread(thread1, "thread--1");
                Thread thread22 = new Thread(thread1, "thread--2");
                thread11.start();
                thread22.start();
            }
        }
        /**outPut
         * main:0
         * main:1
         * main:2
         * main:3
         * main:4
         * main:5
         * main:6
         * main:7
         * main:8
         * main:9
         * thread--2:0
         * thread--1:0
         * thread--2:1
         * thread--1:2
         * thread--2:3
         * thread--1:4
         * thread--2:5
         * thread--1:6
         * thread--1:8
         * thread--1:9
         * thread--2:7
         *
         */
    }

    public static void testThirdThread(){
        // Callable 接口相比 Runnable 接口， 线程执行体由 run() 变为 call()，附加功能是有返回值，且可抛出异常；
        // FutureTask 类实现 Future 接口和 Runnable 接口，所以可接收 call 方法的返回值，且作为 Thread 的 target 创建并启动新线程；
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + ":" + i);
            if (i == 3) {
                ThirdThread callable = new ThirdThread();
                FutureTask<String> task = new FutureTask(callable);
                Thread thread = new Thread(task, "new thread");
                Thread thread1 = new Thread(task, "new thread-1");
                thread.start();
                try {
                    Thread.sleep(1);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                thread1.start();
                try {
                    System.out.println(task.get());
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

class FirstThread extends Thread{
    private int i;
    @Override
    public void run(){
        for (; i < 10; i++){
            System.out.println(getName() + ":" + i);
        }
    }
}

class SecondThread implements Runnable{
    private int i;
    @Override
    public void run(){
        for (; i < 10; i++){
            System.out.println(Thread.currentThread().getName() + ":" + i);
        }
    }
}

class ThirdThread implements Callable<String>{
    private int i;
    @Override
    public String call() throws Exception{
        for (; i < 10 ; i++){
            System.out.println(Thread.currentThread().getName() + ":" + i);
        }
        return "final result : " + i;
    }
}

