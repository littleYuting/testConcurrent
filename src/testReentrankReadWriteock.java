import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class testReentrankReadWriteock {

    public static void main(String[] args) {
//        testQueue();
        testCacheDemo();
    }

    public static void testQueue(){
        final Queue q = new Queue();
        for (int i = 0; i < 2; i++) {
            new Thread(){
                public void run() {
                    for (int i = 0; i < 3; i++) {
                        q.get();
                    }
                }
            }.start();
        }
        for (int i = 0; i < 2; i++) {
            new Thread(){
                public void run() {
                    for (int i = 0; i < 3; i++) {
                        q.put(new Random().nextInt(1000));
                    }
                }
            }.start();
        }
    }

    public static void testCacheDemo(){
        final CacheDemo cacheDemo = new CacheDemo();
        final String key = "cyt";
        for (int i = 0; i < 50; i++) {
            new Thread(){
                public void run(){
                    System.out.println(cacheDemo.getData(key));
                };
            }.start();
        }
    }
}
class Queue{
    private Object data = null; // 共享数据，只有一个线程能写该数据，但可以有多个线程读取该数据；
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public void get(){
        rwl.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to read data !");
            try {
                Thread.sleep((long)(Math.random()*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " have read data : " + data);
        } finally {
            rwl.readLock().unlock();
        }
    }
    public void put(Object data){
        rwl.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to write data !");
            try {
                Thread.sleep((long)(Math.random()*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.data = data;
            System.out.println(Thread.currentThread().getName() + " have write data : " + data);
        } finally {
            rwl.writeLock().unlock();
        }
    }
}
// 应用读写锁设计一个缓存系统，读读不互斥，读写互斥，写写互斥；
class CacheDemo{
    private Map<String, Object> map = new HashMap<>();
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public Object getData(String key) {
        rwl.readLock().lock();
        Object value = null;
        try {
            value = map.get(key);
            if (value == null) {
                rwl.readLock().unlock();
                rwl.writeLock().lock();
                try {
                    value = map.get(key);
                    if (value == null) {
                        value = new Random().nextInt(1000) + " test";
                        map.put(key, value);
                        System.out.println("DB completed ~");
                    }
                    rwl.readLock().lock();
                } finally {
                    rwl.writeLock().unlock();
                }
            }
        } finally {
            rwl.readLock().unlock();
        }
        return value;
    }

}