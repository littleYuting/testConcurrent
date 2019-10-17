## 使用场景
- 单个任务处理时间较短，需要处理的任务数量很大；  
## 使用线程池的好处
- 降低资源消耗，通过重复利用已创建的线程降低线程创建和销毁造成的消耗；
- 提高响应速度，当任务到达时，任务可以不需要的等到线程创建就能立即执行；
- 提高线程的可管理性，线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配，调优和监控；

## jdk 自带线程池类图
![](http://hi.csdn.net/attachment/201201/18/0_13268772813r7f.gif)
### 1. Executor 框架接口
- Executor 接口
    - 对于不同的Executor实现，execute()方法可能是创建一个新线程并立即启动，也有可能是使用已有的工作线程来运行传入的任务，也可能是根据设置线程池的容量或者阻塞队列的容量来决定是否要将传入的线程放入阻塞队列中或者拒绝接收传入的线程； 
- ExecutorService 接口
    - ExecutorService接口继承自Executor接口，提供了管理终止的方法，以及可为跟踪一个或多个异步任务执行状况而生成 Future 的方法。增加了shutDown()，shutDownNow()，invokeAll()，invokeAny()和submit()等方法。如果需要支持即时关闭，也就是shutDownNow()方法，则任务需要正确处理中断； 
- ScheduledExecutorService 接口  
    - ScheduledExecutorService扩展ExecutorService接口并增加了schedule方法。调用schedule方法可以在指定的延时后执行一个Runnable或者Callable任务。ScheduledExecutorService接口还定义了按照指定时间间隔定期执行任务的scheduleAtFixedRate()方法和scheduleWithFixedDelay()方法； 

## 线程池的创建

### 1. ThreadPoolExecutor
- 构造函数
```
public ThreadPoolExecutor(  
    int corePoolSize,  // 队列没满时，线程最大并发数
    int maximumPoolSize,  // 队列满后线程能够达到的最大并发数
    long keepAliveTime, // 空闲线程过多久被回收的时间限制
    TimeUnit unit, // keepAliveTime 的时间单位
    BlockingQueue<Runnable> workQueue, // 阻塞的队列类型
    ThreadFactory threadFactory,
    RejectedExecutionHandler handler // 超出 maximumPoolSizes + workQueue 时，任务会交给RejectedExecutionHandler来处理
) 
```
- 向线程池提交任务

![](https://user-gold-cdn.xitu.io/2017/12/7/1602fee11fcf165d?w=745&h=794&f=png&s=105406)
- 补充： 
runnableTaskQueue 用于保存等待执行的任务的阻塞队列  
    - ArrayBlockingQueue：是一个基于数组结构的有界阻塞队列，此队列按 FIFO（先进先出）原则对元素进行排序；
    - LinkedBlockingQueue：一个基于链表结构的阻塞队列，此队列按FIFO （先进先出） 排序元素，吞吐量通常要高于ArrayBlockingQueue；
    - SynchronousQueue：一个不存储元素的阻塞队列。每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQueue；
    - PriorityBlockingQueue：一个具有优先级得无限阻塞队列；

### 2. newFixedThreadPool

- 实现：创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待；
- 构造函数
```
public static ExecutorService newFixedThreadPool(int nThreads){
    return new ThreadPoolExecutor(
            nThreads,   // corePoolSize
            nThreads,   // maximumPoolSize == corePoolSize
            0L,         // 空闲时间限制是 0
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>() // 无界阻塞队列
        );
}
```
- 向线程池提交任务
![](https://user-gold-cdn.xitu.io/2017/12/7/1602ff914d170169?w=700&h=624&f=png&s=75270)

### 3. newCachedThreadPool
- 实现： 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程；
- 构造函数
```
public static ExecutorService newCachedThreadPool(){
    return new ThreadPoolExecutor(
        0,                  // corePoolSoze == 0
        Integer.MAX_VALUE,  // maximumPoolSize 非常大
        60L,                // 空闲判定是60 秒
        TimeUnit.SECONDS,
        // 神奇的无存储空间阻塞队列，每个 put 必须要等待一个 take
        new SynchronousQueue<Runnable>()  
    );
}
```
- 向线程池提交任务
![](https://user-gold-cdn.xitu.io/2017/12/7/1603006f3e0871c6?w=539&h=703&f=png&s=69124)

### 4. newSingleThreadExecutor
- 实现：创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行；

- 构造函数

```
public static ExecutorService newSingleThreadExecutor() {
        return 
            new FinalizableDelegatedExecutorService
                (
                    new ThreadPoolExecutor
                        (
                            1,
                            1,
                            0L,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<Runnable>(),
                            threadFactory
                        )
                );
    }
```
除了多了个 FinalizableDelegatedExecutorService 代理，其初始化和 newFiexdThreadPool 的 nThreads = 1 的时候是一样的。
区别就在于：

- newSingleThreadExecutor返回的ExcutorService在析构函数finalize()处会调用shutdown();
- 如果我们没有对它调用shutdown()，那么可以确保它在被回收时调用shutdown()来终止线程;
- 使用ThreadFactory，可以改变线程的名称、线程组、优先级、守护进程状态，一般采用默认;

### 5. ScheduledThreadPool

## 关闭线程池
- shutdown : 将线程池的状态设置成SHUTDOWN状态，然后中断所有没有正在执行任务的线程;
- shutdownNow : 
    - 将线程池的状态设置成 STOP ;
    - 遍历线程池中的工作线程，逐个调用线程的 interrupt 方法来中断线程;
    - 无法响应中断的任务可能永远无法终止；
- 所有任务均已关闭，说明线程池关闭成功，调用 isTerminaed 方法会返回 true；
- 

## 配置线程池
## 监控线程池

- taskCount：线程池需要执行的任务数量；
- completedTaskCount：线程池在运行过程中已完成的任务数量，不大于 taskCount；
- largestPoolSize：线程池曾创建的最大线程数量，通过此参数可判断线程池是否满过；
- getPoolSize：线程池的线程数量；若线程池不销毁，池里线程不会自动销毁；
- getActiveCount：获取活动的线程数；

## 提交任务 execute 与 submit 的区别
- execute 方法无返回值，无法判断任务是否被线程池执行成功；
- submit 方法会返回一个 future 对象，future.get() 方法获取返回值；

## 参考文献

1. [深入理解线程和线程池（图文详解）](https://blog.csdn.net/weixin_40271838/article/details/79998327)
2. [通俗易懂，各常用线程池的执行 流程图](https://www.cnblogs.com/linguanh/p/8000063.html)
3. [(精)JAVA线程池原理以及几种线程池类型介绍](https://blog.csdn.net/it_man/article/details/7193727)
4. [深入理解 Java 线程池：ThreadPoolExecutor](https://juejin.im/entry/58fada5d570c350058d3aaad)
5. [聊聊并发（三）Java线程池的分析和使用](http://ifeve.com/java-threadpool/)
