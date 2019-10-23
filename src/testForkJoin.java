import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class testForkJoin {
    public static void main(String[] args){
//        testSumCompare();
        testCompareCallableAndForkJoin();
    }
    public static void testCompareCallableAndForkJoin(){
        int limit = 1000;
        testLimitCompare(limit);
        limit = 100000;
        testLimitCompare(limit);
        limit = 1000000;
        testLimitCompare(limit);
        limit = 100000000;
        testLimitCompare(limit);
        /** output
         * ************** start ***************
         * ForkJoin 求和结果 : 500500
         * forkJoin 求和，范围为 0 ~ 1000 , 耗时 ： 6168056
         * callable 求和结果 ： 500500
         * Callable 求和，范围为 0 ~ 1000 , 耗时 ： 7199986
         * ************** end ***************
         * ************** start ***************
         * ForkJoin 求和结果 : 705082704
         * forkJoin 求和，范围为 0 ~ 100000 , 耗时 ： 17508161
         * callable 求和结果 ： 705082704
         * Callable 求和，范围为 0 ~ 100000 , 耗时 ： 26749768
         * ************** end ***************
         * ************** start ***************
         * ForkJoin 求和结果 : 1784293664
         * forkJoin 求和，范围为 0 ~ 1000000 , 耗时 ： 162355485
         * callable 求和结果 ： 1784293664
         * Callable 求和，范围为 0 ~ 1000000 , 耗时 ： 112935769
         * ************** end ***************
         * ************** start ***************
         * ForkJoin 求和结果 : 987459712
         * forkJoin 求和，范围为 0 ~ 100000000 , 耗时 ： 448116141
         * callable 求和结果 ： 987459712
         * Callable 求和，范围为 0 ~ 100000000 , 耗时 ： 8536777831
         * ************** end ***************
         * analyse : under general cases, forkJoin seems better than Callable
         *
         */
    }
    public static void testLimitCompare(int limit){
        System.out.println("************** start ***************");
        Long time_1 = calculateTime(limit, 1);
        System.out.println("forkJoin 求和，范围为 0 ~ " + limit + " , 耗时 ： " + time_1);
        Long time_4 = calculateTime(limit, 4);
        System.out.println("Callable 求和，范围为 0 ~ " + limit + " , 耗时 ： " + time_4);
        System.out.println("************** end ***************");
    }
    public static void testCallable(int to){
        class TestCallable implements Callable {
            private int from;
            private int to;
            public TestCallable(int from, int to){
                this.from = from;
                this.to = to;
            }
            @Override
            public Integer call(){
                int sum_value = 0;
                for (int i = from; i <= to; i++) {
                    sum_value += i;
                }
                return sum_value;
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(16);
        List<Future<Integer>> arr = new ArrayList<>();
        for (int i = 0; i < to/10; i++) {
            arr.add(executor.submit(new TestCallable(i*10+1, (i+1)*10)));
        }
        executor.shutdown();
        int sum_all = 0;
        for (Future<Integer> a : arr) {
            try {
                sum_all += a.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("callable 求和结果 ： " + sum_all);
    }

    public static void testRecursiveTask(int to){
        ForkJoinPool pool = new ForkJoinPool(16);
        class TestTask extends RecursiveTask<Integer> {
            private int from;
            private int to;
            private int gap;

            public TestTask(int from, int to, int gap){
                this.from = from;
                this.to = to;
                this.gap = gap;
            }
            @Override
            public Integer compute(){
                int sum_value = 0;
                if (to - from <= gap) {
                    for (int i = from; i <= to; i++) {
                        sum_value += i;
                    }
//                    System.out.println("thread : " + Thread.currentThread().getName() + ", start : " + from + ", end : " + to + " , local_sum : " + sum_value);
                } else{
                    int middle = (from + to)>>1;
                    TestTask left = new TestTask(from, middle, gap);
                    TestTask right = new TestTask(middle + 1, to, gap);
                    left.fork();
                    right.fork();
                    sum_value = left.join() + (int)right.join();
                }
                return sum_value;
            }
        }
        int gap = 5;
        TestTask task = new TestTask(0, to, gap);
        Future<Integer> result =  pool.submit(task);
        try {
            System.out.println("ForkJoin 求和结果 : " + result.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }
    public static void testSumCompare(){
        // no perfect algorithm, just have to test and compare, please remember~
        int limit = 5000;
        testWithinSum(limit);
        limit = 50000;
        testWithinSum(limit);
        limit = 1000000;
        testWithinSum(limit);
        limit = 100000000;
        testWithinSum(limit);
        /** output
         * ************** start ***************
         * ForkJoin 求和结果 : 12502500
         * forkJoin 求和，范围为 0 ~ 5000 , 耗时 ： 6708610
         * stream 求和结果 ： 12502500
         * Stream 求和，范围为 0 ~ 5000 , 耗时 ： 3206123
         * foreach 求和结果 ： 12502500
         * foreach 求和，范围为 0 ~ 5000 , 耗时 ： 121026
         * ************** end ***************
         * ************** start ***************
         * ForkJoin 求和结果 : 1250025000
         * forkJoin 求和，范围为 0 ~ 50000 , 耗时 ： 38857513
         * stream 求和结果 ： 1250025000
         * Stream 求和，范围为 0 ~ 50000 , 耗时 ： 3958367
         * foreach 求和结果 ： 1250025000
         * foreach 求和，范围为 0 ~ 50000 , 耗时 ： 1468565
         * ************** end ***************
         * ************** start ***************
         * ForkJoin 求和结果 : 1784293664
         * forkJoin 求和，范围为 0 ~ 1000000 , 耗时 ： 169632021
         * stream 求和结果 ： 1784293664
         * Stream 求和，范围为 0 ~ 1000000 , 耗时 ： 8787865
         * foreach 求和结果 ： 1784293664
         * foreach 求和，范围为 0 ~ 1000000 , 耗时 ： 7219230
         * ************** end ***************
         * ************** start ***************
         * ForkJoin 求和结果 : 987459712
         * forkJoin 求和，范围为 0 ~ 100000000 , 耗时 ： 506719730
         * stream 求和结果 ： 987459712
         * Stream 求和，范围为 0 ~ 100000000 , 耗时 ： 51661052
         * foreach 求和结果 ： 987459712
         * foreach 求和，范围为 0 ~ 100000000 , 耗时 ： 54815428
         * ************** end ***************
         */
    }
    public static void testWithinSum(int limit){
        System.out.println("************** start ***************");
        Long time_1 = calculateTime(limit, 1);
        System.out.println("forkJoin 求和，范围为 0 ~ " + limit + " , 耗时 ： " + time_1);
        Long time_2 = calculateTime(limit, 2);
        System.out.println("Stream 求和，范围为 0 ~ " + limit + " , 耗时 ： " + time_2);
        Long time_3 = calculateTime(limit, 3);
        System.out.println("foreach 求和，范围为 0 ~ " + limit + " , 耗时 ： " + time_3);
        System.out.println("************** end ***************");
    }
    public static Long calculateTime(int limit, int type){
        int[] a = IntStream.range(0, limit + 1).toArray();
        Long start_1, end_1;
        start_1 = System.nanoTime();
        if (type == 1) {
            testRecursiveTask(limit);
        }
        else if (type == 2) {
            System.out.println("stream 求和结果 ： " + Arrays.stream(a).reduce(0, (c,b)->c+b));
        }
        else if (type == 3) {
            int sum_value = 0;
            for (int i:a
                 ) {
                sum_value += i;
            }
            System.out.println("foreach 求和结果 ： " + sum_value);
        }
        else if (type == 4) {
            testCallable(limit);
        }
        end_1 = System.nanoTime();
        return end_1 - start_1;
    }
}
