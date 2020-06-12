# Redis学习

## Linux安装Redis

* 下载安装包 https://redis.io/download
* 解压redis安装包
* ![](E:\IDEAStudyWorkSpace\mydocument\Redis学习\file\1591714937.jpg)

* 进入解压目录，改变redis.conf文件
* ![](E:\IDEAStudyWorkSpace\mydocument\Redis学习\file\1591715055(1).jpg)

* 基本的环境安装，需要

  * ```json
    yum install gcc-c++
    make 命令  make PREFIX=/usr/redis install 安装到指定文件夹下面
    make install 
    ```

  * 默认安装在 /usr/local/bin 目录下

* 将redis配置文件考培到当前目录下
* redis 默认不是后启动需要修改配置文件
  
* ![](E:\IDEAStudyWorkSpace\mydocument\Redis学习\file\1591717023(1).jpg)
  
* 启动redis服务

  * ![](E:\IDEAStudyWorkSpace\mydocument\Redis学习\file\1591717163(1).jpg)

  * ```jso
    启动redis命令：./redis-server /root/redis-5.0.8/redis.conf
    
    ```

* 连接redis：redis-cli -p 6379

* 查看redis程序是否开启

  * ```jso
    ps -ef | grep redis
    ```

* 关闭redis 服务

  * ![](E:\IDEAStudyWorkSpace\mydocument\Redis学习\file\1591717459(1).jpg)

## 性能测试

**redis-benchmark  是一个压力测试工具**

官方自带的性能测试工具

redis 性能测试工具可选参数如下所示：

| 序号 | 选项      | 描述                                       | 默认值    |
| :--- | :-------- | :----------------------------------------- | :-------- |
| 1    | **-h**    | 指定服务器主机名                           | 127.0.0.1 |
| 2    | **-p**    | 指定服务器端口                             | 6379      |
| 3    | **-s**    | 指定服务器 socket                          |           |
| 4    | **-c**    | 指定并发连接数                             | 50        |
| 5    | **-n**    | 指定请求数                                 | 10000     |
| 6    | **-d**    | 以字节的形式指定 SET/GET 值的数据大小      | 2         |
| 7    | **-k**    | 1=keep alive 0=reconnect                   | 1         |
| 8    | **-r**    | SET/GET/INCR 使用随机 key, SADD 使用随机值 |           |
| 9    | **-P**    | 通过管道传输 <numreq> 请求                 | 1         |
| 10   | **-q**    | 强制退出 redis。仅显示 query/sec 值        |           |
| 11   | **--csv** | 以 CSV 格式输出                            |           |
| 12   | **-l**    | 生成循环，永久执行测试                     |           |
| 13   | **-t**    | 仅运行以逗号分隔的测试命令列表。           |           |
| 14   | **-I**    | Idle 模式。仅打开 N 个 idle 连接并等待。   |           |

进行简单测试

```bash
# 测试100个并发连接 100000 请求
redis-benchmark -h 192.168.204.5 -p 6379 -c 100 -n 100000
```

![](E:\IDEAStudyWorkSpace\mydocument\Redis学习\file\1591768861(1).jpg)

> redis是单线程的

redis 是很快的，官方表示，redis是基于内存操作的，CPU不是redis的性能瓶颈，Redis的瓶颈是根据机器的内存和网络带宽，既然可以使用单线程，就是用单线程了



> **redis为什么单线程还这么快**

1、误区1：高性能的服务器一定是多线程的？

2、误区二：多线程（CPU上下文切换！）一定比单线程效率高！错误的

核心：redis 是将所有的数据全部放在内存中的，所以说使用单线程去操作效率就是最高的，多线程（CPU上下文会切换：耗时的操作），对于内存系统来说，如果没有上下文的切换效率就是最高的！多次读写都是一个CPU上的，在内存情况下，这个就是最佳方案！

## 相关数据类型

### String

* 浏览量，访问量加1的操作

* ```bash
  ############################################ 类似java程序中 i++ 的操作
  步长
  192.168.204.5:6379> incr views   # 自增+1 的操作
  (integer) 1
  192.168.204.5:6379> incr views
  (integer) 2
  192.168.204.5:6379> incr views
  (integer) 3
  192.168.204.5:6379> incr views
  (integer) 4
  192.168.204.5:6379> get views
  "4"
  192.168.204.5:6379> decr views  # 递减 -1的操作
  (integer) 3
  # 可以设置步长  指定增量
  192.168.204.5:6379> incrby views 10 # i+= 固定长度的操作  
  (integer) 13
  192.168.204.5:6379> decrby views 10 # i-= 固定长度的操作
  (integer) 3
  192.168.204.5:6379> 
  ```

* 







1、配置文件 unit单位 对大小写不敏感！

> 包含

就是好比我们学习Spring、Improt， include   可以将多个配置文件整合到一起