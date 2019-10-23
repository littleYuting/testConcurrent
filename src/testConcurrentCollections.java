import java.lang.String;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class testConcurrentCollections   {

    public static void main(String[] args){
//        testConcurrentLinkedDeque();
//        testConcurrentHashMap();
//        testCopyOnWriteArrayList();
        testCopyOnWriteArraySet();

    }

    public static void testConcurrentLinkedDeque() {
        // 使用非阻塞线程安全的并发列表: 如果操作不能立即完成，将根据这个操作抛出异常或返回 null 值；
        // 大量添加数据到列表
        class AddTask implements Runnable{
            private ConcurrentLinkedDeque<String> list;
            public AddTask(ConcurrentLinkedDeque<String> list){
                this.list = list;
            }
            @Override
            public void run(){
                String name = Thread.currentThread().getName();
                for (int i = 0; i < 10000; i++) {
                    list.add(name + i);
                }
            }
        }
        class PollTask implements Runnable{
            private ConcurrentLinkedDeque<String> list;
            public PollTask(ConcurrentLinkedDeque<String> list){
                this.list = list;
            }
            @Override
            public void run(){
                String name = Thread.currentThread().getName();
                for (int i = 0; i < 5000; i++) {
                    list.pollFirst();
                    list.pollLast();
                }
            }
        }
        ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();
        Thread[] threads = new Thread[100];
        AddTask addTask = new AddTask(list);
        for (int i = 0; i < threads.length; i++) {
//            AddTask addTask = new AddTask(list);
            threads[i] = new Thread(addTask);
            threads[i].start();
        }
        System.out.println("AddTask threads have been launched");
        for (int i = 0; i < threads.length; i++) {
            try{
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Size of List : " + list.size());
        PollTask pollTask = new PollTask(list);
        for (int i = 0; i < threads.length; i++) {
//            PollTask pollTask = new PollTask(list);
            threads[i] = new Thread(pollTask);
            threads[i].start();
        }
        System.out.println("PollTask threads have been launched");
        for (int i = 0; i < threads.length; i++) {
            try{
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Size of List : " + list.size());
        /** output
         * AddTask threads have been launched
         * Size of List : 1000000
         * PollTask threads have been launched
         * Size of List : 0
         */
    }

    public static void testConcurrentHashMap () {
        // 并发执行时，线程安全的容器只能保证自身的数据不被破坏，无法保证业务的行为是否正确。
//        final Map<String, Integer> count = new ConcurrentHashMap<>();
        final Map<String, AtomicInteger> count = new ConcurrentHashMap<>();
        final CountDownLatch endLatch = new CountDownLatch(2);
        Runnable task = new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 5; i++) {
//                    Integer value = count.get("a");
//                    if (null == value) {
//                        count.put("a", 1);
//                    } else {
//                        count.put("a", value + 1);
//                    }
//                }
                // 改进 2 ：
//                Integer oldValue, newValue;
//                for (int i = 0; i < 5; i++) {
//                    while (true) {
//                        oldValue = count.get("a");
//                        if (null == oldValue) {
//                            newValue = 1;
//                            if (count.putIfAbsent("a", newValue) == null) {// putIfAbsent() : 若 key 对应的 value 不存在，则 put 进去，返回 null， 否则不 put，返回已存在的 value；
//                                break;
//                            }
//                        } else {
//                            newValue = oldValue + 1;
//                            if (count.replace("a", oldValue, newValue)) { // replace() : 若 key 对应的当前值是 oldValue， 则替换为 newValue，返回 true，否则不替换，返回 false；
//                                break;
//                            }
//                        }
//                    }
//                }
                // 改进 3 ： concurrentHashMap 的 key 和 value 均不能为 null；
                AtomicInteger oldValue;
                for (int i = 0; i < 5; i++) {
                    oldValue = count.get("a");
                    if (null == oldValue) {
                        AtomicInteger zeroValue = new AtomicInteger(0);
                        oldValue = count.putIfAbsent("a", zeroValue);
                        if (null == oldValue) {
                            oldValue = zeroValue;
                        }
                    }
                    oldValue.incrementAndGet();
                }
                endLatch.countDown();
            }
        };
        new Thread(task).start();
        new Thread(task).start();// 使用多线程操作 concurrentHashMap，并发会造成结果覆盖，所以 “a” 的 value <= 10 ；
        try {
            endLatch.await();
            System.out.println(count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 改进 1 ： public synchronized void run()
    }

    public static void testCopyOnWriteArrayList(){
        // CopyOnWriteArrayList 适合使用在读操作远大于写操作的场景中，如缓存；
        class ListReader implements Runnable{
            private List<String> list;
            public ListReader(List list){
                this.list = list;
            }
            @Override
            public void run(){
                if (list != null) {
                    for (String str : this.list) {
                        System.out.println(Thread.currentThread().getName() + " : " + str);
                    }
                }
            }
        }
        class ListWriter implements Runnable{
            private List<String> list;
            private int index;
            public ListWriter(List list, int index){
                this.list = list;
                this.index = index;
            }
            @Override
            public void run(){
                if (this.list != null) {
                    this.list.add("...... add " + this.index + " ......");
                }
            }
        }
//        List<String> list = new ArrayList<>();// 多线程下，一个线程对 list 进行修改，另一个线程对 list 进行 for 时会抛出 ConcurrentModificationException 并发异常
        List<String> list = new CopyOnWriteArrayList<>();// 每次修改时，会生成一个新的 copy ，并把新修改元素加入 copy 尾部，生成一个新的引用；
        for (int i = 0; i <= 5; i++) {
            list.add("...... Line " + (i + 1) + "......");
        }
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i <= 3; i++) {
            service.execute(new ListReader(list));
            service.execute(new ListWriter(list, i));
        }
        service.shutdown();
    }

    public static void testCopyOnWriteArraySet(){
        /**
         * 1）适用场景：Set 大小通常保持很小，只读操作远多于可变操作，需在遍历期间防止线程间的冲突；
         * 2）线程安全的无序集合，与 hashSet 的相同点：继承父体-AbstractSet，不同点：hashSet 通过散列表 hashMap 实现，CopyOnWriteArraySet 通过动态数组 CopyOnWriteArrayList 实现；
         * 3) 可变操作（add、set、remove等）开销很大，需复制整个 list；
         * 4）迭代器支持 hasNext()、 next() 等不可变操作，不支持可变 remove()  操作；
         * 5）使用迭代器进行遍历的速度很快，并且不会与其他线程发生冲突。在构造迭代器时，迭代器依赖于不变的数组快照；
         */
        Set<String> set = new CopyOnWriteArraySet();
        class MyThread extends Thread{
            public MyThread(String name){
                super(name);
            }
            @Override
            public void run(){
                int i = 0;
                while (i++ < 10) {
                    String val = Thread.currentThread().getName() + "-" + i % 6;
                    set.add(val);
                    {
                        Iterator it = set.iterator();
                        while (it.hasNext()) System.out.print(it.next() + ",");
                        System.out.println();
                    }
                }
            }
        }
        new MyThread("thread_a").start();
        new MyThread("thread_b").start();
        /** 小困惑：不懂为什么会输出 两个 thread_a-1
         * thread_a-1,thread_a-1,thread_b-1,thread_a-2,thread_b-2,thread_a-3,thread_b-3,thread_b-4,thread_a-4,thread_b-1,thread_a-2,thread_b-2,thread_a-3,thread_b-3,thread_b-4,thread_a-4,
         *
         */
    }

    public static void testArrayBlockingQueue(){
        // 见 testConsumerAndProvider
    }


}
