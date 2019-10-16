import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class testCondition {

    public static void main (String[] args) {
//        testUsingSingleConditionWaitNotify();
        testUsingMultiConditionWaitNotify();
    }

    public static void testUsingSingleConditionWaitNotify(){
        MyServie myServie = new MyServie();
        Thread_1 thread_1 = new Thread_1(myServie);
        thread_1.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        myServie.signal_1();
        /** output :
         * for await_1 , the starting time： 1571192834602
         * for signal_1 , the starting time： 1571192836603
         * for signal_1 , the ending time： 1571192839604
         * for await_1 , the ending time： 1571192839604
         * explainer : signal 在 await 间隔两秒后执行， 且需等 signal 所在 try 语句块之后才释放锁，condition.await() 后的语句才能执行；
         */
    }

    public static void testUsingMultiConditionWaitNotify(){
        MyServie myServie = new MyServie();
        Thread_1 thread_1 = new Thread_1(myServie);
        thread_1.start();
        Thread_2 thread_2 = new Thread_2(myServie);
        thread_2.start();
        Thread_3 thread_3 = new Thread_3(myServie);
        thread_3.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 若 condition 涉及两个线程，执行 signal 只能唤醒其中一个， 这样会使程序无法结束
//        myServie.signal_1();
        /** output :
         * Thread-0 for await_1 , the starting time： 1571194543403
         * Thread-1 for await_1 , the starting time： 1571194543404
         * Thread-2 for await_2 , the starting time： 1571194543404
         * main for signal_1 , the starting time： 1571194545404
         * main for signal_1 , the ending time： 1571194548404
         * main for signal_2 , the starting time： 1571194548404
         * main for signal_2 , the ending time： 1571194551405
         * Thread-0 for await_1 , the ending time： 1571194551405
         * for await_2 , the ending time： 1571194551405
         * Thread-0 start to await_1
         * Thread-2 start to await_2
         */
        myServie.signalAll_1();
        /** SignalAll 可唤醒所有用到该 condition 的线程；
         * output :
         * Thread-0 for await_1 , the starting time： 1571194860280
         * Thread-1 for await_1 , the starting time： 1571194860281
         * Thread-2 for await_2 , the starting time： 1571194860281
         * main for signalAll_1 , the starting time： 1571194862281
         * main for signalAll_1 , the ending time： 1571194865281
         * main for signal_2 , the starting time： 1571194865281
         * main for signal_2 , the ending time： 1571194868282
         * Thread-0 for await_1 , the ending time： 1571194868282
         * Thread-0 start to await_1
         * Thread-1 for await_1 , the ending time： 1571194868282
         * Thread-1 start to await_1
         * for await_2 , the ending time： 1571194868282
         * Thread-2 start to await_2
         */
        myServie.signal_2();
    }

}
class MyServie{
    Lock lock = new ReentrantLock();
    Condition cdn_1 = lock.newCondition();
    Condition cdn_2 = lock.newCondition();

    public void await_1(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " for await_1 , the starting time： " + System.currentTimeMillis());
            cdn_1.await();
            System.out.println(Thread.currentThread().getName() + " for await_1 , the ending time： " + System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void signal_1(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " for signal_1 , the starting time： " + System.currentTimeMillis());
            cdn_1.signal();
            Thread.sleep(3000);
            System.out.println(Thread.currentThread().getName() + " for signal_1 , the ending time： " + System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void signalAll_1(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " for signalAll_1 , the starting time： " + System.currentTimeMillis());
            cdn_1.signalAll();
            Thread.sleep(3000);
            System.out.println(Thread.currentThread().getName() + " for signalAll_1 , the ending time： " + System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void await_2(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " for await_2 , the starting time： " + System.currentTimeMillis());
            cdn_2.await();
            System.out.println("for await_2 , the ending time： " + System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void signal_2(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " for signal_2 , the starting time： " + System.currentTimeMillis());
            cdn_2.signal();
            Thread.sleep(3000);
            System.out.println(Thread.currentThread().getName() + " for signal_2 , the ending time： " + System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}

class Thread_1 extends Thread{
    private MyServie myServie;
    public Thread_1(MyServie myServie){
        super();
        this.myServie = myServie;
    }
    @Override
    public void run(){
        myServie.await_1();
        System.out.println(Thread.currentThread().getName() + " start to await_1");
    }
}

class Thread_2 extends Thread{
    private MyServie myServie;
    public Thread_2(MyServie myServie){
        super();
        this.myServie = myServie;
    }
    @Override
    public void run(){
        myServie.await_1();
        System.out.println(Thread.currentThread().getName() + " start to await_1");
    }
}

class Thread_3 extends Thread{
    private MyServie myServie;
    public Thread_3(MyServie myServie){
        super();
        this.myServie = myServie;
    }
    @Override
    public void run(){
        myServie.await_2();
        System.out.println(Thread.currentThread().getName() + " start to await_2");
    }
}