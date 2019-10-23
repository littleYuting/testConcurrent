import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class testConsumerAndProvider {
    /** 前言_生产者&消费者模型
     * 1） 定义：基于等待/通知机制，描述的是有一块缓冲区作为仓库，生产者可将产品放入仓库，消费者可以从仓库中取出产品；
     * 2）规则：两个排斥(生产时不能消费，消费时不能生产)；两个等待(缓冲区为空时不能消费，缓冲区满时不能生产)；
     * 3）优点： 解耦（公共缓冲区）； 通过速度平衡实现最优解；
     */
    private int queue_size = 4;
    private PriorityQueue<Integer> queue = new PriorityQueue<>(queue_size);

    private BlockingQueue blockingQueue = new ArrayBlockingQueue(queue_size);

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();
    private Condition empty = lock.newCondition();
    private Condition full = lock.newCondition();

    final Semaphore noEmpty = new Semaphore(0);
    final Semaphore noFull = new Semaphore(3);
    final Semaphore mutex = new Semaphore(1);

    final PipedInputStream pipedInputStream = new PipedInputStream();
    final PipedOutputStream pipedOutputStream = new PipedOutputStream();
    {
        try {
            pipedInputStream.connect(pipedOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) {
        testConsumerAndProvider test = new testConsumerAndProvider();
//        Provider provider = test.new Provider();
//        Consumer consumer = test.new Consumer();
        /** 不加任何锁 queue_size = 5;
         *  output :
         * the queue has been empty
         * add into one element， then can push : 4
         * take off one element， then left : 0
         * add into one element， then can push : 4
         * add into one element， then can push : 4
         * take off one element， then left : 0
         * add into one element， then can push : 3
         * take off one element， then left : 1
         * add into one element， then can push : 3
         * take off one element， then left : 1
         * add into one element， then can push : 3
         * add into one element， then can push : 3
         * add into one element， then can push : 2
         * take off one element， then left : 1
         * add into one element， then can push : 1
         * add into one element， then can push : 0
         */
//        Provider1 provider = test.new Provider1();
//        Consumer1 consumer = test.new Consumer1();
        /** try 1 : synchronized : wait(),notify
         * output :
         * the queue has been empty
         * add into one element， then can push : 3
         * add into one element， then can push : 2
         * add into one element， then can push : 1
         * add into one element， then can push : 0
         * the queue has become full
         * take off one element， then left : 3
         * take off one element， then left : 2
         * take off one element， then left : 1
         * take off one element， then left : 0
         * the queue has been empty
         * add into one element， then can push : 3
         * take off one element， then left : 0
         * the queue has been empty
         */
//        Provider2 provider = test.new Provider2();
//        Consumer2 consumer = test.new Consumer2();
        /** try 2 : ReentrankLock.condition : await(),signal()
         * 运行结果同 try 1；
         */
//        Provider3 provider = test.new Provider3();
//        Consumer3 consumer = test.new Consumer3();
        /** try 3 : BlookingQueue
         * 运行结果同 try 1；
         * 注 ： 生产方法改为 put ; 消费方法改为 take；否则 阻塞队列失效，程序会陷入死循环；
         * 补充知识点 ： blookingQueue 接口方法在四种情况下的使用：1）抛异常(add,remove); 2)特定值（offer,poll）; 3）阻塞（put,take）; 4)超时（offer(timeout,timeutil),poll()）
         */
//        Provider4 provider = test.new Provider4();
//        Consumer4 consumer = test.new Consumer4();
        /** try 4 : Semaphor(信号量)
         * 运行结果同 try 1；
         * 补充知识点 ： Semaphore 管理一组 permit ， acquire & release ， 若初始化一个 permit 为 1 的 Semphore ，其相当于一个不可重入的互斥锁 mutex
         */
        Provider5 provider = test.new Provider5();
        Consumer5 consumer = test.new Consumer5();
        /** try 5 : 管道输入输出流 PipedInputStream 和 pipedOutputSrteam， 局限性： 只能用于一对 生产者和消费者；
         * 补充知识点 ： 1）使多线程通过管道进行线程间的通讯； 2）使用管道通讯时，两者必须配套使用；
         * output :
         * Provider write : 0
         * Provider write : 1
         * Consumer read : 0
         * Consumer read : 1
         * Provider write : 2
         * Provider write : 3
         * Consumer read : 2
         * Provider write : 4
         * Consumer read : 3
         * java.io.IOException: Pipe closed
         * 	at java.io.PipedInputStream.read(PipedInputStream.java:307)
         * 	at testConsumerAndProvider$Consumer5.consume(testConsumerAndProvider.java:341)
         * 	at testConsumerAndProvider$Consumer5.run(testConsumerAndProvider.java:336)
         */
        provider.start();
        consumer.start();

    }
    // 无锁
    class Consumer extends Thread{
        @Override
        public void run(){
            consume();
        }
        public void consume(){
            for (int i = 0; i < 5; i++) {
                while (queue.size() == 0) {
                    System.out.println("the queue has been empty");
                }
                queue.poll();
                System.out.println("take off one element， then left : " + queue.size());
            }
        }
    }
    class Provider extends Thread{
        @Override
        public void run(){
            provide();
        }
        public void provide(){
            for (int i = 0; i < 10; i++) {
                while (queue.size() == queue_size) {
                    System.out.println("the queue has become full");
                }
                queue.offer(1);
                System.out.println("add into one element， then can push : " + (queue_size - queue.size()));
            }
        }
    }
    // try 1 : synchronized : wait(),notify
    class Consumer1 extends Thread{
        @Override
        public void run(){
            consume();
        }
        public void consume(){
            for (int i = 0; i < 10; i++) {
                synchronized (queue) {
                    while (queue.size() == 0) {
                        try {
                            System.out.println("the queue has been empty");
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            queue.notify();
                        }
                    }
                    queue.poll();
                    queue.notify();
                    System.out.println("take off one element， then left : " + queue.size());
                }
            }
        }
    }
    class Provider1 extends Thread{
        @Override
        public void run(){
            provide();
        }
        public void provide(){
            for (int i = 0; i < 5; i++) {
                synchronized (queue) {
                    while (queue.size() == queue_size) {
                        try {
                            System.out.println("the queue has become full");
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            queue.notify();
                        }
                    }
                    queue.offer(1);
                    queue.notify();
                    System.out.println("add into one element， then can push : " + (queue_size - queue.size()));
                }
            }
        }
    }
    // try 2 : ReentrankLock.condition : await(),signal()
    // 验证用一个condition和分别用两个condion的实现效果一致， 另外需注意的是要在 InterruptedException 异常处理中执行 signal
    class Consumer2 extends Thread{
        @Override
        public void run(){
            consume();
        }
        public void consume(){
            for (int i = 0; i < 10; i++) {
                lock.lock();
                try {
                    while (queue.size() == 0) {
                        try {
                            System.out.println("the queue has been empty");
                            empty.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            empty.signal();
//                            condition.signal();
                        }
                    }
                    queue.poll();
                    empty.signal();
                    System.out.println("take off one element， then left : " + queue.size());
                } finally {
                    lock.unlock();
                }
            }
        }
    }
    class Provider2 extends Thread{
        @Override
        public void run(){
            provide();
        }
        public void provide(){
            for (int i = 0; i < 5; i++) {
                lock.lock();
                try {
                    while (queue.size() == queue_size) {
                        try {
                            System.out.println("the queue has become full");
                            full.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            full.signal();
//                            condition.signal();
                        }
                    }
                    queue.offer(1);
                    full.signal();
                    System.out.println("add into one element， then can push : " + (queue_size - queue.size()));
                } finally {
                    lock.unlock();
                }
            }
        }
    }
    // try 3 : BlookQueue
    class Consumer3 extends Thread{
        @Override
        public void run(){
            consume();
        }
        public void consume(){
            for (int i = 0; i < 5; i++) {
                while (blockingQueue.size() == 0) {
                    System.out.println("the queue has been empty");
                }
                try {
                    blockingQueue.take(); // blookingqueue 在阻塞状态下使用的方法
                    System.out.println("take off one element， then left : " + blockingQueue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Provider3 extends Thread{
        @Override
        public void run(){
            provide();
        }
        public void provide(){
            for (int i = 0; i < 5; i++) {
                while (blockingQueue.size() == queue_size) {
                    System.out.println("the queue has become full");
                }
                try {
                    blockingQueue.put(1); // blookingqueue 在阻塞状态下使用的方法
                    System.out.println("add into one element， then can push : " + (queue_size - blockingQueue.size()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Semaphore
    class Consumer4 extends Thread{
        @Override
        public void run(){
            consume();
        }
        public void consume() {
            for (int i = 0; i < 5; i++) {
                while (queue.size() == 0) {
                    System.out.println("the queue has been empty");
                }
                try {
                    noEmpty.acquire();
                    mutex.acquire();
                    queue.poll();
                    System.out.println("take off one element， then left : " + queue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    noFull.release();
                    mutex.release();
                }
            }
        }
    }
    class Provider4 extends Thread{
        @Override
        public void run(){
            provide();
        }
        public void provide(){
            for (int i = 0; i < 5; i++) {
                while (queue.size() == queue_size) {
                    System.out.println("the queue has become full");
                }
                try {
                    noFull.acquire();
                    mutex.acquire();
                    queue.offer(1);
                    System.out.println("add into one element， then can push : " + (queue_size - queue.size()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    noEmpty.release();
                    mutex.release();
                }
            }
        }
    }
    // 管道
    class Consumer5 extends Thread{
        @Override
        public void run(){
            consume();
        }
        public void consume(){
            try {
                for (int i = 0; i < 5; i++) {
                    int num = pipedInputStream.read();
                    System.out.println("Consumer read : " + num);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    pipedOutputStream.close();
                    pipedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Provider5 extends Thread{
        @Override
        public void run(){
            provide();
        }
        public void provide(){
            try {
                for (int i = 0; i < 5; i++) {
                    System.out.println("Provider write : " + i);
                    pipedOutputStream.write(i);
                    pipedOutputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    pipedOutputStream.close();
                    pipedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}




