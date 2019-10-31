import java.util.HashMap;
import java.util.Map;

public class testCreatReadWriteLock {
    private int state = 0;
    public static void main(){
        /** 读写访问资源的条件
         *  1. 读取：没有现成正在做写操作，且没有线程在请求写操作；
         *  2. 写入：没有线程正在做读写操作；
         *  3. 若对写操作的请求更重要，则要提升写请求的优先级；（若读操作发生较频繁，且没有提升写操作的优先级，则会产生“写饥饿”）；
         */
        MyReadWriteLock lock = new MyReadWriteLock();


    }
    /** 使用 notifyAll 的两个原因：
     * 1. 若同时既有线程在等待获取读锁，也有线程在获取写锁，只唤醒其中一个 notify ，而此时仍有其他请求线程存在，所以被唤醒的线程会再次进入阻塞状态；
     * 2. 若有多个读线程在等待读锁而没有线程在等待写锁时，调用 unlockWrite 之后，所有等待读锁的线程都能立马获得读锁，而不是一次只允许一个；
     */
    static class MyReadWriteLock {
        private int state = 0;// 定义一个共享锁变量
        private int writeRequest = 0; //记录写请求数量

        public int getReadCount() throws InterruptedException{
            return state >>> 16; // 高 16 位为读锁数量
        }

        public  int getWriteCount(){
            return state & ((1 << 16) - 1); // 低 16 位为读锁数量
        }

        public synchronized void lockRead()  throws InterruptedException{
            // 写锁数量大于 0 或者写请求数量大于 0 的情况下都优先执行写
            while (getWriteCount() > 0 || writeRequest > 0) {
                wait();
            }
            System.out.println("lockRead : " + Thread.currentThread().getName());
            state += (1 << 16);
        }

        public synchronized void unLockRead(){
            state -= (1 << 16);
            notifyAll();
        }

        public synchronized void lockWrite()  throws InterruptedException{
            writeRequest += 1;
            while (getReadCount() > 0 || getWriteCount() > 0) {
                wait();
            }
            writeRequest -= 1;// 获取写锁后写请求 -1
            System.out.println("lockWrite: " + Thread.currentThread().getName());
            state += 1;
        }

        public synchronized void unLockWrite() {
            state -= 1;
            notifyAll();
        }
    }

    /** 读写锁重入
     *  case ： Thread1 获得读锁；
     *          Thread2 请求写锁，但由于 Thread1 持有读锁，所以写锁请求被阻塞；
     *          Thread1 再想请求一次读锁，但因为 Thread2 处于请求写锁状态，所以想再次获取读锁也会被阻塞；
     */

    static class MyReadWriteLock_1 {
        private Map<Thread, Integer> readingThreads = new HashMap<>();
        private int writers = 0;
        private int readers = 0;
        private int writeRequest = 0;
        private Thread writingThread = null;

        // 读锁重入规则：1）满足获取读锁的条件（没有写或写请求）； 2）已经持有读锁（不管是否有写请求）
        public synchronized void lockRead()  throws InterruptedException{
            Thread callingThread = Thread.currentThread();
            while (!canGrantReadAccess(callingThread)) {
                // 1.有写操作 false； 2.已经持有锁 true； 3.有写请求 false
                wait();
            }
            readingThreads.put(callingThread, (getReadAccessCount(callingThread) + 1));
        }

        public synchronized void unLockRead(){
            Thread callingThread = Thread.currentThread();
            int accessCount = getReadAccessCount(callingThread);
            if (accessCount == 1) {
                readingThreads.remove(callingThread);
            } else {
                readingThreads.put(callingThread, (accessCount - 1));
            }
            notifyAll();
        }
        // 写锁重入规则：仅当一个线程已经持有写锁，才允许写锁重入（再次获得写锁）
        public synchronized void lockWrite() throws InterruptedException{
            writeRequest++;
            Thread callingThread = Thread.currentThread();
            while (!canGrantWriteAccess(callingThread)) {
                // 1.有读操作 false； 2.writingThread == null, true; 3.writingThread == 当前线程, false 【此处还有点小迷糊】
                wait();
            }
            writeRequest--;
            writers++;
            writingThread = callingThread;
        }

        public synchronized void unLockSynchronized () {
            writers--;
            if (writers == 0) {
                writingThread = null;
            }
            notifyAll();
        }

        boolean canGrantReadAccess(Thread callingThread){
            if (writers > 0){
                return false;
            } else if (writingThread != null) {
                return false; // 写锁降级到读锁【对于一个拥有写锁的线程，再次获得读锁不会有什么危险】
            }else if (isReader(callingThread)) {
                return true;
            } else if (writeRequest > 0) {
                return false;
            }
            return true;
        }

        int getReadAccessCount(Thread callingThread){
            Integer accessCount = readingThreads.get(callingThread);
            if (accessCount == null) {
                return 0;
            }
            return accessCount;
        }
        boolean isReader(Thread callingThread) {
            return readingThreads.get(callingThread) != null;
        }

        boolean canGrantWriteAccess(Thread callingThread){
            if (hasReaders())  return false;
            if (writingThread == null) return true;
            if (!isWrite(callingThread)) return false;
            return true;
        }

        boolean hasReaders(){
            return readingThreads.size() > 0;
        }

        boolean isWrite(Thread callingThread){
            return writingThread == callingThread;
        }

        // 读锁升级到写锁  【规则】若希望一个拥有读锁的线程，也能获得写锁，则要求这个线程是唯一一个拥有读锁的线程；
        boolean isOnlyReader(Thread callingThread) {
            return readers == 1 && readingThreads.get(callingThread) != null;
        }
    }
}

