import java.util.concurrent.atomic.AtomicReference;

public class testAtomicReference {
    private static Person person;
    private static AtomicReference<Person> atr;

    public static void main(String[] args){
        person = new Person("chenYuTing", 25);
        atr = new AtomicReference<Person>(person);
        System.out.println("the person : " + person.toString());
        Thread thread_1 = new Thread(new Task_1());
        Thread thread_2 = new Thread(new Task_2());

        thread_1.start();
        thread_2.start();
        try {
            thread_1.join();
            thread_2.join();
        } catch (InterruptedException  e) {
            e.printStackTrace();
        }
        /** 无原子操作引入 output （输出结果不确定）
         * the person : name : chenYuTing , age : 25
         * Task1 change : name : cyt , age : 26 // 也有可能输出 ：cyt , age : 28
         * Task2 change : name : tingYa , age : 28 // 也有可能输出 ：cyt , age : 28
         */

        System.out.println("Now Atomic Reference : " + atr.get().toString()); // 引入原子操作
        /** possible output
         * the person : name : chenYuTing , age : 25
         * Task_1 Atomic Reference : name : cyt , age : 26
         * Task_2 Atomic Reference : name : tingYa , age : 27
         * Now Atomic Reference : name : tingYa , age : 27
         */
    }

    static class Task_1 implements Runnable{
        @Override
        public void run(){
//            person.setName("cyt");
//            person.setAge(person.getAge() + 1);
//            System.out.println("Task1 change : " + person.toString());
            atr.getAndSet(new Person("cyt", person.getAge() + 1));
            System.out.println("Task_1 Atomic Reference : " + atr.get().toString());
        }
    }

    static class Task_2 implements Runnable{
        @Override
        public void run(){
//            person.setName("tingYa");
//            person.setAge(person.getAge() + 2);
//            System.out.println("Task2 change : " + person.toString());
            atr.getAndSet(new Person("tingYa", person.getAge() + 2));
            System.out.println("Task_2 Atomic Reference : " + atr.get().toString());
        }
    }

    static class Person{
        private String name;
        private int age;

        public Person(){}
        public Person(String name, int age){
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
        @Override
        public String toString(){
            return "name : " + this.name + " , age : " + this.age;
        }
    }
}
