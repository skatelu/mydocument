# Java 8 中的 Streams API 详解
##简介：
* Stream 作为 Java 8 的一大亮点，它与 java.io 包里的 InputStream 和 OutputStream 是完全不同的概念。它也不同于 StAX 对 XML 解析的 Stream，也不是 Amazon Kinesis 对大数据实时处理的 Stream。Java 8 中的 Stream 是对集合（Collection）对象功能的增强，它专注于对集合对象进行各种非常便利、高效的聚合操作（aggregate operation），或者大批量数据操作 (bulk data operation)。Stream API 借助于同样新出现的 Lambda 表达式，极大的提高编程效率和程序可读性。同时它提供串行和并行两种模式进行汇聚操作，并发模式能够充分利用多核处理器的优势，使用 fork/join 并行方式来拆分任务和加速处理过程。通常编写并行代码很难而且容易出错, 但使用 Stream API 无需编写一行多线程的代码，就可以很方便地写出高性能的并发程序。所以说，Java 8 中首次出现的 java.util.stream 是一个函数式语言+多核时代综合影响的产物。
* Stream，用户只要给出需要对其包含的元素执行什么操作，比如 “过滤掉长度大于 10 的字符串”、“获取每个字符串的首字母”等，Stream 会隐式地在内部进行遍历，做出相应的数据转换。
* Stream 的另外一大特点是，数据源本身可以是无限的。
* **当我们使用一个流的时候，通常包括三个基本步骤：**
* 每次转换原有 Stream 对象不改变，返回一个新的 Stream 对象（可以有多次转换），这就允许对其操作可以像链条一样排列，变成一个管道，如下图所示。
  ![stream示意图](../file/java8_stream.png)

#### 生成Stream的方式
* 从 Collection 和数组中生成Stream,注意，此处包含继承Collectin的那些类  如<font color='red'>List、Set</font>等都可以使用这个方法
  ```java
    Collection.stream()   // 串行
    Collection.parallelStream()  // 并行
    Arrays.stream(T array) or Stream.of("","","") //自己创建一个流of 后的括号中方一个或多个元素. 
  ```
* 从 BufferedReader
  ```java
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

        Stream<String> lines = bf.lines();
  ```
* 静态工厂
  ```java
    java.util.stream.IntStream.range()
    /**
     * java.util.stream.IntStream.range()
     * IntStream(int start,int end)  此方法返回 左闭右开区间整数的流
     * @param start 开始数字
     * @param end 结束数字
     */
    public static void TestIntStream(int start,int end){
        IntStream.range(start, end).forEach( value -> {
            System.out.println(value);
        });
    }


    java.nio.file.Files.walk()
    /**
     * java.nio.file.Files.walk()
     */
    public static void TestFilesStream(){
        Stream<Path> walk = Files.walk();
    }

  ```
* 自己构建(暂时不会用)
  ```java
    java.util.Spliterator
  ```
* 其它
  ```java
    Random.ints()
    /**
     * 这个方法会产生一个无限流，会无限的往外流出数据
     */
    public static void TestOtherStream(){

        new Random(10).ints().forEach(value -> {
            System.out.println(value);
        });
    }

    //        应用场景：
    1.统计一组大数据中没有出现过的数；
        将这组数据映射到BitSet，然后遍历BitSet，对应位为0的数表示没有出现过的数据。
    2.对大数据进行排序；
        将数据映射到BitSet，遍历BitSet得到的就是有序数据。
    3.在内存对大数据进行压缩存储等等。
        一个GB的内存空间可以存储85亿多个数，可以有效实现数据的压缩存储，节省内存空间开销。
    BitSet.stream()

    Pattern.splitAsStream(java.lang.CharSequence)

    //java 获取 jar 包内文件列表
    JarFile.stream()
  ```
  * BitSet 简单说明

        在内存中是一串连续的内存空间，从0开始的正整数

        按位操作，每一位的值只有两种 0 或者 1，来表示某个值是否出现过。

        2：简单使用

        把 1 3 5 三个数放bitSet中

        BitSet bitSet=new BitSet();

            bitSet.set(1);

        bitSet.set(3);

        bitSet.set(5);

            这时候bitSet的长度是 最大数+1=5+1=6

        for(int i=0;i<bitSet1.length();i=i+1) {

        System.out.print(bitSet1.get(i)+"-");

        } 

        得到的结果

        false-true-false-true-false-true

        0      1      2    3     4      5


#### 流的操作类型分为两种：
* Intermediate（中间层）：一个流可以后面跟随零个或多个 intermediate 操作。其目的主要是打开流，做出某种程度的数据映射/过滤，然后返回一个新的流，交给下一个操作使用。这类操作都是惰性化的（lazy），就是说，仅仅调用到这类方法，并没有真正开始流的遍历。   
<br>
* Terminal（最终）：一个流只能有一个 terminal 操作，当这个操作执行后，流就被使用“光”了，无法再被操作。所以这必定是流的最后一个操作。Terminal 操作的执行，才会真正开始流的遍历，并且会生成一个结果，或者一个 side effect。
<br>
* 在对于一个 Stream 进行多次转换操作 (Intermediate 操作)，每次都对 Stream 的每个元素进行转换，而且是执行多次，这样时间复杂度就是 N（转换次数）个 for 循环里把所有操作都做掉的总和吗？其实不是这样的，转换操作都是 lazy 的，多个转换操作只会在 Terminal 操作的时候融合起来，一次循环完成。我们可以这样简单的理解，Stream 里有个操作函数的集合，每次转换操作就是把转换函数放入这个集合中，在 Terminal 操作的时候循环 Stream 对应的集合，然后对每个元素执行所有的函数。
<br>
* 还有一种操作被称为 short-circuiting(短路)。用以指：
  * 对于一个 intermediate 操作，如果它接受的是一个无限大（infinite/unbounded）的 Stream，但返回一个有限的新 Stream。
  * 对于一个 terminal 操作，如果它接受的是一个无限大的 Stream，但能在有限的时间计算出结果。
* 当操作一个无限大的 Stream，而又希望在有限时间内完成操作，则在管道内拥有一个 short-circuiting 操作是必要非充分条件。

* 示例
  ```java
    int sum = widgets.stream()
        .filter(w -> w.getColor() == RED)
        .mapToInt(w -> w.getWeight())
        .sum();
  ```
* stream() 获取当前小物件的 source，filter 和 mapToInt 为 intermediate 操作，进行数据筛选和转换，最后一个 sum() 为 terminal 操作，对符合条件的全部小物件作重量求和。

## 流的使用详解
* 简单说，对 Stream 的使用就是实现一个 filter-map-reduce 过程，产生一个最终结果，或者导致一个副作用（side effect）。
* 
