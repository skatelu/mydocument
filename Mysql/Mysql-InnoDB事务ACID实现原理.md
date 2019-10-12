# 一文快速搞懂MySQL InnoDB事务ACID实现原理
* 这一篇主要讲一下 InnoDB 中的事务到底是如何实现 ACID 的：
* 原子性（atomicity）
* 一致性（consistency）
* 隔离性（isolation）
* 持久性（durability）
### 隔离性
* 隔离性的实现原理就是锁，因而隔离性也可以称为并发控制、锁等。事务的隔离性要求每个读写事务的对象对其他事务的操作对象能互相分离。
* 再者，比如操作缓冲池中的 LRU 列表，删除，添加、移动 LRU 列表中的元素，为了保证一致性那么就要锁的介入。
* InnoDB 使用锁为了支持对共享资源进行并发访问，提供数据的完整性和一致性。
  
* <span style="font-size: 15px;letter-spacing: 1px;color: rgb(71, 193, 168);">那么到底 InnoDB 支持什么样的锁呢？我们先来看下 InnoDB 的锁的介绍：</span>

### InnoDB 中的锁
* 你可能听过各种各样的 InnoDB 的数据库锁，Gap 锁，共享锁，排它锁，读锁，写锁等等。但是 InnoDB 的标准实现的锁只有 2 类，一种是行级锁，一种是意向锁。
* InnoDB 实现了如下两种标准的行级锁：
  * 共享锁（读锁 S Lock），允许事务读一行数据。
  * 排它锁（写锁 X Lock），允许事务删除一行数据或者更新一行数据。