## 前言 
- ForkJoinPool 并不是 ThreadPoolExecutor 的替代品，而是作为对 ThreadPoolExecutor 的补充。
## 1. ThreadPool Exector
### 1.1 基本组成
- 线程池管理器（ThreadPool）：创建、销毁、管理（添加新任务等）；

- 工作线程（PoolWorker）：线程池中线程，在无任务时处于等待状态，可循环执行任务；

- 任务接口（Task）：每个任务必须实现的接口，以供工作线程调度任务的执行，主要规定了任务的入口，任务执行完后的收尾工作，任务的执行状态等；

- 任务队列（taskQueue）：一种缓冲机制，存放待处理的任务；

### 1.2 工作方式

- 一个总任务队列，线程空闲时，从队列中认领工作，ThreadPool 允许线程重用，以减少线程创建与销毁次数，提高效率；  
![](https://inews.gtimg.com/newsapp_bt/0/8243680714/1000)

### 1.3 适用场景
- 数量少、任务由外部生成且相互独立、处理耗时、有时阻塞的任务；

## 2. ForkJoinPool Executor

### 2.1 基本组成

- 线程池管理器（ForkJoinPool）：负责控制框架内 workerThread 数量、创建与激活，负责 workQueue 队列的创建和分配，即对一个 workerThread 分配相应的 workQueue，workerThread  处理 workQueue 中的任务；

- 工作线程（PoolWorker）ForkJoinWorkerThread：依附于 ForkJoinPool，首先在 ForkJoinPool 中注册(registerWorker)，获取相应的 workQueue，然后从 workQueue 里面拿任务出来处理；

- 任务接口（ForkJoinTask）：任务抽象接口，包括两个子类 RecursiveTask、RecursiveAction，两者区别在于 RecursiveTask 任务有返回值，RecursiveAction 无返回值，任务的具体切分和处理逻辑在 compute() 方法中；
    - ForkJoinPool 实现了 ExecutorService 接口，可通过 submit、invokeAll 和 invokeAny 等方法来执行 Runnable 和 Callable 类型的任务； 

- 任务队列（ForkJoinPool.WorkQueue）: 属于 Thread ，存储接收任务，底层数据结构是 双端队列；
- 线程池工作队列（submitting queue） ：属于 ForkJoinPool ，用于接收由外部线程（非 ForkJoinThread 线程）提交的任务；
    - submit 操作：最初的任务是 push 到外部线程的 submitting queue 里的；
    - submit() 和 fork() 无本质区别，只是提交对象变成了 submitting queue 而已（还有一些同步，初始化的操作），submitting queue 和其他 work queue 一样，是工作线程”窃取“的对象，因此当其中的任务被一个工作线程成功窃取时，就意味着提交的任务真正开始进入执行阶段；

### 2.2 工作方式

- fork/join ： 任务分治，通过递归将任务分割成更小的子任务，其中阈值可自定义配置，将子任务分配给不同线程并发执行，最后收集结果；【单机的 map/reduce】
    - fork ：开启一个新线程或是重用线程池内的空闲线程，将任务推入当前工作线程的工作队列里进行处理；
    - join ：等待该任务的处理线程处理完毕，获得返回值，并不是每个 join 都会造成，具体处理步骤如下：
        - 1）检查调用 join() 的线程是否是 ForkJoinThread 线程，如果不是（eg main 线程），则阻塞当前线程，等待任务完成，如果是，则不阻塞；
        - 2）查看任务的完成状态，如果已经完成，直接返回结果；
        - 3）如果任务尚未完成，但处于自己的工作队列内，则完成它；
        - 4）如果任务已经被其他的工作线程偷走，则窃取这个小偷的工作队列内的任务（以 FIFO 方式），执行，以期帮助它早日完成欲 join 的任务；
        - 5）如果偷走任务的小偷也已经把自己的任务全部做完，正在等待需要 join 的任务时，则找到小偷的小偷，帮助它完成它的任务；
        - 6）递归地执行第5步； 
- workSteal ： 允许空闲线程“窃取”分配给另一个线程的工作，高效地利用硬件资源；【任务阻塞而线程不阻塞】  
![](https://inews.gtimg.com/newsapp_bt/0/8243690512/1000)

- workSteal 的图示说明：
![](http://blog.dyngr.com/images/20160915/forkjoinpool-structure.png)
![](https://www.jdon.com/simgs/soa/forkjoin.png)


### 2.3 适用场景

- 数量多、处理耗时短、可生成子任务且其间存在父子依赖、几乎无阻塞(即计算密集型)任务

### 2.4 补充
- invoke 、 submit 和 execute 的区别
    - execute 执行不带返回值的 ForkJoinTask；
    - submit 执行带返回值的ForkJoinTask（通常继承自 RecursiveTask），非阻塞；
    - invoke 执行带返回值的ForkJoinTask（通常继承自 RecursiveTask），阻塞；

- [数组求和实现（对比 ThreadPool 与 ForkJoin）](https://github.com/littleYuting/testConcurrent/blob/master/src/testForkJoin.java)
    - 求和数据比较小的时候无需考虑哪种方式；
    - 求和数据比较大的时候java8新特性是优异于for循环的；
    - 求和数据比较大的时候使用ForkJoin需要考虑临界值的设置，否则可能效率不如for循环和java8新特性。 

## 3. 参考文献
- [最新 ThreadPool 与 ForkJoinPool 详细解析](https://new.qq.com/rain/a/20190322A03N1A)
- [Java-5 ThreadPoolExecutor比Java-7 ForkJoinPool有什么优势？](https://codeday.me/bug/20171121/99342.html)
- [线程池与 forkjoin 比较](https://www.jdon.com/performance/threadpool-forkjoin.html)
- [Java 并发编程笔记：如何使用 ForkJoinPool 以及原理](http://blog.dyngr.com/blog/2016/09/15/java-forkjoinpool-internals/)
- [爬虫 ForkJoinPool VS ExecutorService](https://www.iteye.com/topic/1117483)
- [窃取算法完整示例图 + 代码示例](https://zhuanlan.zhihu.com/p/38204373)
- [Java 多线程（5）：Fork/Join 型线程池与 Work-Stealing 算法](https://segmentfault.com/a/1190000008140126)
- [JCIP-39-Fork/Join 框架、工作窃取算法](https://houbb.github.io/2019/01/18/jcip-39-fork-join)
- 较为完整 [多线程并发之线程池Executor与Fork/Join框架](https://blog.csdn.net/J080624/article/details/82888787)
