## 一、I/O 模型
### 1. 同步阻塞
![](https://pic2.zhimg.com/80/16ef4bcfbd8319535edeb45f597dfc61_hd.jpg)
### 2. 同步非阻塞（论询）
![](https://pic3.zhimg.com/80/2cb0550b87ca28336d0411e58b45b013_hd.jpg)
### 3. 信号驱动式
### 4. I/O 多路复用【事件驱动】
背景：多线程
- 使用一个线程来监控多个文件描述符（Socket）的就绪状态
- 少量线程数，减少内存开销和上下文切换的CPU开销；
![](https://pic3.zhimg.com/80/9155e2307879cd7ce515e7a997b9d532_hd.jpg)
- select、poll 和 epoll 的区别
    - select
    ![](https://user-gold-cdn.xitu.io/2017/11/1/e133e7da305c768772bd108f3df9f1d2?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)
        - 改进：
        - 缺点：
            - 每次调用 select ，都需要把fd（文件描述符）集合从用户态拷贝到内核态，这个开销在fd很多时会很大；
            - 每次调用 select 都需要在内核遍历传递进来的所有fd，这个开销在fd很多时也很大
            - select支持的文件描述符数量太小，默认是1024；
    - poll
        - 与 select 非常相似，只是描述 fd 集合的方式不同，poll 使用 pollfd 结构而不是 select 的 fd_set 结构；
    - epoll
        - 三个函数：
            - epoll_create：创建一个 epoll 句柄；
            - epoll_ctl：注册监听的事件类型；
            - epoll_wait：等待事件的产生；
        - 三点改进： 
            - 1）每次注册新的事件到 epoll 句柄中时（在 epoll_ctl 中指定 EPOLL_CTL_ADD ），会把所有的 fd 拷贝进内核，而不是在 epoll_wait 的时候重复拷贝，即 epoll 保证了每个 fd 在整个过程中只会拷贝一次；
            - 2）epoll_ctl 只把 current 挂一遍（这一遍必不可少）并为每个 fd 指定一个回调函数，当设备就绪，唤醒等待队列上的等待者时，就会调用这个回调函数，而这个回调函数会把就绪的fd加入一个就绪链表，epoll_wait的工作实际上就是在这个就绪链表中查看有没有就绪的fd；
            - 所支持的 fd 无限制，为最大可以打开文件的数目；
    - 相同点：select，poll，epoll本质上都是同步阻塞 I/O，应用程序需在读写事件就绪后自己负责进行读写；
    - 不同点：
        - 1）select，poll 实现需要自己不断轮询所有 fd 集合，直到设备就绪，期间可能要睡眠和唤醒多次交替，而 epoll 其实也需要调用epoll_wait不断轮询就绪链表，期间也可能多次睡眠和唤醒交替，但是它是设备就绪时，调用回调函数，把就绪 fd 放入就绪链表中，并唤醒在 epoll_wait 中进入睡眠的进程，虽然都要睡眠和交替，但是 select 和 poll 在“醒着”的时候要遍历整个 fd 集合，而 epoll 在“醒着”的时候只要判断一下就绪链表是否为空就行了，这节省了大量的 CPU 时间；
        - 2）select，poll 每次调用都要把fd集合从用户态往内核态拷贝一次，并且要把 current 往设备等待队列中挂一次，而 epoll 只要一次拷贝，而且把 current 往等待队列上挂也只挂一次（在 epoll_wait 的开始，注意这里的等待队列并不是设备等待队列，只是一个 epoll 内部定义的等待队列）；

### 5. 异步非阻塞
- 异步 IO ：无需应用程序自己读写，回调后数据会直接从内核拷贝到用户空间；
### 6. 异步 IO + 协程

## 二、 Java 的 I/O 操作类的基本划分  
- 传输数据的数据格式：  
    - 基于字节操作的 I/O 接口：InputStream 和 OutputStream  
    - 基于字符操作的 I/O 接口：Writer 和 Reader  
- 传输数据的方式：
    - 基于磁盘操作的 I/O 接口：File  
    - 基于网络操作的 I/O 接口：Socket  
## 1. 基于字节操作的 I/O 接口  

### 1.1 InputStream 输入流
- 类继承层次图
    ![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image003.jpg)
### 1.2 OutputStream 输出流
- 类继承层次图
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image005.jpg)
### 1.3 补充
- 操作数据的方式可组合使用：OutputStream out = new BufferedOutputStream(new ObjectOutputStream(new FileOutputStream("fileName"))；
- 必须要指定流的终点，写网络相比写磁盘需多一步操作：底层操作系统再将数据传送到其他地方；
- 磁盘或网络传输的最小存储单元均是字节，即 I/O 操作的是字节而非字符；

## 2. 基于字符的 I/O 操作接口
### 2.1 Writer 写字符
- 类层次结构  
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image007.jpg)

### 2.2 Reader 读字符
- 类层次结构  
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image009.jpg)
### 2.3 补充
-  Writer 或 Reader 类只定义读取或写入的数据字符的方式，不指定写入的地方；
## 3. 字节与字符的转化接口
另外数据持久化或网络传输都是以字节进行的，所以必须要有字符到字节或字节到字符的转化。
### 3.1 字符解码相关类结构：
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image011.jpg)
InputStreamReader 类是字节到字符的转化桥梁，InputStream 到 Reader 的过程要指定编码字符集，否则将采用操作系统默认字符集，很可能会出现乱码问题。StreamDecoder 正是完成字节到字符的解码的实现类。也就是当你用如下方式读取一个文件时：
### 3.2 字符编码相关类结构：
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image013.jpg)
类是字节到字符的转化桥梁，InputStream 到 Reader 的过程要指定编码字符集，否则将采用操作系统默认字符集，很可能会出现乱码问题。StreamDecoder 正是完成字节到字符的解码的实现类。也就是当你用如下方式读取一个文件时：
通过 OutputStreamWriter 类完成，字符到字节的编码过程，由 StreamEncoder 完成编码过程。

## 参考文献
1. [深入分析 java IO 的工作机制](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/index.html)
2. [了解 Linux 五种 IO 模型](https://www.jianshu.com/p/486b0965c296)
3. [IO 多路复用原理剖析](https://juejin.im/post/59f9c6d66fb9a0450e75713f)
