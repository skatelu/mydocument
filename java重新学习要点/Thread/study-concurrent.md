### 1、介绍三种线程安全下懒加载的单例模式   目前看到的地方为：P51
  ```java
  // 双检索模式
    public class SingletonObject1 {

    /**
    * lazy load
    */
    private volatile static SingletonObject1 instance;

    private SingletonObject1(){
        //
    //    BeforeTest beforeTest = new BeforeTest();
    //    String s = beforeTest.testRun();
    //    System.out.println(Thread.currentThread().getName()+s+"===="+beforeTest);
    }

    public static SingletonObject1 getInstance(){
        if(null == instance){
        System.out.println(Thread.currentThread().getName()+"实例为null");
        synchronized (SingletonObject1.class) {
            if(null == instance){
            instance = new SingletonObject1();
            System.out.println(Thread.currentThread().getName()+"成功初始化单例");
            }
        }
        }
        System.out.println(Thread.currentThread().getName()+"返回实例"+ instance);
        return instance;
    }
    }


    // 第二种  时不加锁模式的单例
    /**
      * 双检索之外的
      * 既能保证单例模式懒加载，又能保证线程安全  这种方式不加锁
      */
      public class SingletonObject6 {

            private SingletonObject6(){

            }

            /**
            * static 在类加载过程中只加载一次并且严格保证线程执行的顺序
            * 2、InstanceHolder主动加载时会将 instance 构建出来，static 是线程友好的 保证了只能初始化一次
            */
            public static class InstanceHolder{
                private final static SingletonObject6 instance = new SingletonObject6();
            }

            /**
            * 1、当第一个线程调用getInstance方法的时候，InstanceHolder会进行主动加载
            * 2、当第二个线程去调用的时候因为是 static
            * @return
            */
            public static SingletonObject6 getInstance(){
                return InstanceHolder.instance;
            }
      }

    // 第三种是枚举类型的单例
    /**
      * 枚举类型实现线程安全懒加载的单例模式(比较推荐的模式)
      */
        public class SingletonObject7 {

            private SingletonObject7(){

            }

            private enum Singleton{
                INSTANCE;

                private final SingletonObject7 instance;
                /**
                * 枚举类型是线程安全的，并且构造函数只会被装载一次
                */
                Singleton(){
                instance = new SingletonObject7();
                }

                public SingletonObject7 getInstance(){
                return instance;
                }
            }
            public static SingletonObject7 getInstance(){
                return Singleton.INSTANCE.getInstance();
            }

            /**
            * 验证实例
            * @param args
            */
            public static void main(String[] args) {
                IntStream.rangeClosed(1,100).forEach(i -> new Thread(String.valueOf(i)){
                @Override
                public void run() {
                    System.out.println(SingletonObject7.getInstance());
                }
                }.start());
            }
        }

  ```


### WaitSet 调用wait的时候会存在这么一个抽象概念  是否存在，是什么东西，怎么用？
```java
public class WaitSet {

  // 当使用wait的时候，cpu会放弃线程的执行权，会放弃线程的锁，此时放弃的锁去了哪里？去了什么位置？
  // 怎么去唤醒？唤醒后怎么出来

  private static final Object LOCK = new Object();

  private static void work(){
    synchronized (LOCK){
      System.out.println("Begin....");
      try {
        System.out.println("Thread will coming");
        // 执行LOCK.wait() 之后，线程加入到 waitSet之内去了,重新唤醒之后，线程需要去拿锁，此时会不会执行 System.out.println("Begin...."); 这个语句？
        LOCK.wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Thread will out.");
    }
  }

  /**
   * 1、所有的对象都会有一个wait set，用来存放调用了该对象wait方法之后进入block状态的线程
   * 2、线程被notify之后，不一定立即得到执行
   * 3、线程从wait set 中被唤醒顺序不一定是FIFO（先进先出的）。
   * 此处三个在main中的程序进行解释
   * 4、
   * @param args
   */
  public static void main(String[] args) {
    // wait 之后进行唤醒确实是需要去进行抢锁，但是wait 时会记录你代码的执行地址，唤醒后地址恢复，按照此地址继续往后运行，并不会重复运行
      new Thread(){
          @Override
          public void run() {
              work();
          }
      }.start();
      try {
          Thread.sleep(1000);
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
      synchronized (LOCK){
          LOCK.notify();
      }

            //1、首先定义10个线程
        //    IntStream.rangeClosed(1, 10).forEach(i -> new Thread(String.valueOf(i)) {
        //      @Override
        //      public void run() {
        //          synchronized (LOCK){
        //            try {
        //              Optional.of(Thread.currentThread().getName() + " will come to wait set.").ifPresent(System.out::println);
        //              LOCK.wait();
        //              Optional.of(Thread.currentThread().getName() + " will leave to wait set.").ifPresent(System.out::println);
        //            } catch (InterruptedException e) {
        //              e.printStackTrace();
        //            }
        //          }
        //      }
        //    }.start());

        //    try {
        //      Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        //    } catch (InterruptedException e) {
        //      e.printStackTrace();
        //    }

            /**
            * 依次去唤醒线程进行
            */
        //    IntStream.rangeClosed(1, 10).forEach(i -> {
        //      synchronized (LOCK){
        //        LOCK.notify();
        //        try {
        //          // 休眠一秒
        //          Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        //        } catch (InterruptedException e) {
        //          e.printStackTrace();
        //        }
        //      }
        //    });
        }

    }
```


### 一个解释 volatile 关键字作用的好例子，介绍 volatile 到底是什么？

```java
    /**
    * 有关Volatile 一个比较好的例子
    * 此例子说明了 volatile 关键字的线程间可见性与运行的有序性
    */
    public class VolatileTest {

        private volatile static int INIT_VALUE = 0;
        private final static int MAX_LIMIT = 5;

        public static void main(String[] args) {
            //1、首先创建两个线程，一个线程读数据，一个线程修改数据
            new Thread(() -> {
            int localValue = INIT_VALUE;
            while (localValue < MAX_LIMIT) {
                if(localValue != INIT_VALUE){
                System.out.printf("The Value update to {%d}\n", INIT_VALUE);
                localValue = INIT_VALUE;
                }
            }
            }, "READER").start();

            // 通过结果可以看到，两个线程都没有进行加锁，但是输出的结果，做到了线程之间 数据同步的效果
            // 如果去掉 volatile 关键字，可以在结果中看到  UPDATER 线程并没有感知到 INIT_VALUE 值的变化，说明线程间没有进行数据同步
            new Thread(()->{
            int localValue = INIT_VALUE;
            while (INIT_VALUE < MAX_LIMIT) {
                System.out.printf("Update the value to {%d}\n", ++localValue);
                INIT_VALUE = localValue;
                try {
                Thread.sleep(500);
                } catch (InterruptedException e) {
                e.printStackTrace();
                }
            }
            },"UPDATER").start();
        }

    }
```

* CPU 执行的时候会将RAM主内存中的数据缓存到Cache中，  那么RAM的中的数据只取一次么？
* 其实，当没有写与修改的操作的时候，java对多线程进行了优化，当没有写操作的时候，就不会再取主内存中读取数据，会从CPU 的cache中获取   这个样就会导致线程之间的缓存不一致
* 当程序在高速运算过程中会将主内存中的数据，缓存在CPU 高速cache中，从CPU 高速cache中获取数据，再将CPU 高速cache中的数据，刷新到主内存中
* 解决方案
  * 1. 给数据总线加锁  总线（数据总线、地址总线、控制总线） 这样就会使得多核CPU 串行执行，丧失效率
  * 2. CPU 高速缓存一致性协议 （Intel MESI协议）
    * 核心思想：
      * 1. 当CPU写入数据的时候，如果发现该变量被共享（也就是说，在其它CPU中也存在该变量的副本），会发出一个信号，通知其它CPU该变量的缓存无效
      * 2. 当其他CPU访问该变量的时候，重新到主内存进行获取
      * ![内存一致性解决方案](../file/内存一致性解决方案.jpg)


### 并发编程的三个重要概念，原子性、可见性、有序性
* 1. 原子性 简单来说就是一个操作或者多个操作，要么都成功，要么都失败，中间不能由于任何的因素中断
* 2. 可见性： 线程之间的数据是同步的
* 3. 有序性： 有序性的本义是指程序在执行的时候，程序的代码执行顺序和语句的顺序是一致的。
  * 但是java的JVM 中存在一个重排序的过程，执行结果是有顺序的，但执行过程并不一定是按顺序来的
  * 重排序只要求最终一致性  对代码间存在依赖关系的代码则会按顺序执行
    ```java
        // 如
        {
            int i = 0;              (1)
            i = 1;                  (2)
            boolean flag = false;   (3)
            flag = true;            (4)
        }
        /**
            理想情况下，执行顺序应该是 1 -> 2 -> 3 -> 4
            但是 (1)(2) 与 (3)(4) 的代码之间并不存在依赖关系，可能出现 (1)->(3)->(2)-> (4) 这样的代码执行顺序  但显示出来的结果是按顺序执行的
        */

        // 但是在多线程执行过程中，重排序可能会引起一些问题与错误
        ---------Thread -1 ------------------
        Object obj = createObj();         (1)
        init = true;                      (2)

        ---------Thread -2 ------------------
        while(!init){
            sleep();
        }
        useTheObj(obj);
        ------------------------------------

        /**
            正常情况下运行的时候，线程1执行完成后 线程2 检测到init 发生改变 就会调用useTheObj方法，但是当线程1进行重排序后，执行顺序可能发生改变，先执行(2)再去执行(1) 操作，此时就会出现 线程2 中报错，因为 可能线程2 运行的时候 obj 并没有被进行初始化操作
        */
    ```

### 指令重排序、happens-before规则讲解（java 多线程如何保证以上三个特性的） 即JMM
* 1. 原子性： 对基本数据类型的变量读取和赋值是保证了原子性的，要么都成功，要么都失败，这些操作不可被中断
    ```java
        a=10; 原子性
        b=a; 不满足 1、read a 2、assign b; 两个合起来就不满足原子性
        c++; 不满足：1、read c 2、add； 3、assign to c
        c = c+1; 不满足
    ```
* 2. 可见性
  * 使用volatile关键字保证可见性  当数据发生赋值时，会到主存 RAM 中获取数据，而不是去CPU 高效缓存中获取数据
  
  1. 有序性 Java天生保证了一些有序性 叫 happend-before relationship 
     1. 代码的执行顺序，编写在前面的发生在编写在后面的
     2. unlock必须发生在lock之后 （释放锁必须在加锁之后）
     3. volatile 修饰的变量，对该变量的写操作先于该变量的读操作
     4. 传递规则，操作A先于B，B先于C，那么A肯定先于C
     5. 线程启动规则，start方法肯定先于线程的执行操作，即run操作中的代码
     6. 线程的中断规则，interrupt这个动作，必须发生在捕获该动作之前
     7. 对象销毁规则： 一个对象的初始化必须发生在finalize之前
     8. 线程终结规则：所有的操作都发生在线程死亡之前
  2. 可以看看这篇文章 <https://www.cnblogs.com/null-qige/p/9481900.html>


### volatile 深入理解 理解这个关键字 存在以上三个特性的哪几个？

* 一旦一个共享变量被volatile修饰，具备两层语义
  * 保证了不同线程间的可见性
  * 禁止对其进行重排序，也就是保证了有序性
  * 并未保证原子性
  
* 像下面这个操作

    ```java
        new Thread(() -> {
        while (INIT_VALUE < MAX_LIMIT){
            System.out.println("T1->"+ (++INIT_VALUE));
            try{
                Thread.sleep(10);
            }catch (Exception e){
            e.printStackTrace();
            }
        }
        }, "ADDER-1").start();
        
        new Thread(()->{
            while (INIT_VALUE < MAX_LIMIT){
            System.out.println("T2->"+ (++INIT_VALUE));
            try{
                Thread.sleep(10);
            }catch (Exception e){
                e.printStackTrace();
            }
            }
        },"ADDER-2").start();

    ```
* 当线程1读到10的时候 会分成三步执行
* 1. read from main memory INIT_VALUE->10
* 2. INIT_VALUE = 10 + 1;
* 3. INIT_VALUE = 11;
* 但是当线程1在做完第一步的时候，CPU放弃了该线程的执行权，线程2继续执行，此时并未对 volatile修饰的INIT_VALUE变量进行修改，这时内存中的INIT_VALUE 还是10 线程2就回去执行以上三步，这个内存中的数值就是11了，当CPU再回来执行线程1的时候，此时会进行后两步，并没有因为线程暂停而去重新执行，这样线程1的结果也是11 就会将线程2的结果进行覆盖。故volatile并不能保证原子性

* 保证有序性

* 使用场景：
  * 状态量标记  （可见性特点）
  * 保障前后的一致性
