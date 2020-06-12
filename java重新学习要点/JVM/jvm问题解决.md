# JVM内存问题解决方案

### [java.lang.OutOfMemoryError GC overhead limit exceeded原因分析及解决方案](https://www.cnblogs.com/airnew/p/11756450.html)

```tex
Exception in thread thread_name: java.lang.OutOfMemoryError: GC Overhead limit exceeded
Cause: The detail message "GC overhead limit exceeded" indicates that the garbage collector is running all the time and Java program is making very slow progress. After a garbage collection, if the Java process is spending more than approximately 98% of its time doing garbage collection and if it is recovering less than 2% of the heap and has been doing so far the last 5 (compile time constant) consecutive garbage collections, then a java.lang.OutOfMemoryError is thrown. This exception is typically thrown because the amount of live data barely fits into the Java heap having little free space for new allocations.
Action: Increase the heap size. The java.lang.OutOfMemoryError exception for GC Overhead limit exceeded can be turned off with the command line flag -XX:-UseGCOverheadLimit.
```

**原因：**
大概意思就是说，JVM花费了98%的时间进行垃圾回收，而只得到2%可用的内存，频繁的进行内存回收(最起码已经进行了5次连续的垃圾回收)，JVM就会曝出ava.lang.OutOfMemoryError: GC overhead limit exceeded错误。
![img](https://img2018.cnblogs.com/blog/110616/201910/110616-20191029000347915-1421459582.png)

如果没有这个异常，会出现什么情况呢？经过垃圾回收释放的2%可用内存空间会快速的被填满，迫使GC再次执行，出现频繁的执行GC操作， 服务器会因为频繁的执行GC垃圾回收操作而达到100%的时使用率，服务器运行变慢，应用系统会出现卡死现象，平常只需几毫秒就可以执行的操作，现在需要更长时间，甚至是好几分钟才可以完成。

**解决方法：**
1、增加heap堆内存。
2、增加对内存后错误依旧，获取heap内存快照，使用Eclipse MAT工具，找出内存泄露发生的原因并进行修复。
3、优化代码以使用更少的内存或重用对象，而不是创建新的对象，从而减少垃圾收集器运行的次数。如果代码中创建了许多临时对象(例如在循环中)，应该尝试重用它们。
4、升级JDK到1.8，最起码也是1.7，并使用G1GC垃圾回收算法。
5、除了使用命令-xms1g -xmx2g设置堆内存之外，尝试在启动脚本中加入配置:

```java
-XX:+UseG1GC -XX:G1HeapRegionSize=n -XX:MaxGCPauseMillis=m  
-XX:ParallelGCThreads=n -XX:ConcGCThreads=n
```

还有一个非常不建议使用的解决方法：
在启动脚本中添加`-XX:-UseGCOverheadLimit`命令。这个方法只会把*“java.lang.OutOfMemoryError: GC overhead limit exceeded”*变成更常见的*java.lang.OutOfMemoryError: Java heap space*错误。



