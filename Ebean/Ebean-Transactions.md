# Ebean Transactions(事务)

* We can use explicit transactions to wrap multiple database actions such as queries or persisting via save() etc.
* 我们可以使用显示的事务来包装多个数据库的操作，例如通过save()等查询或持久化。
  ```java
    try (Transaction transaction = ebeanServer.beginTransaction()) {

        // do stuff...
        Customer customer = ...
        customer.save();

        Order order = ...
        order.save();

        transaction.commit();
    }
  ```
* We can alternatively use Ebean's @Transactional annotation on methods or Spring Transactions or JTA Transactions.
  ```java
    @Transactional
    public void process(OffsetDateTime startOffset) {
        ...
    }
  ```

### Implicit transactions(隐式的事务)
* When no transactions are specified explicitly Ebean will create a transaction to perform the action.

#### Query - read only transaction
* For queries Ebean will look to use a read only transaction.If a Read Only DataSource has be configured Ebean will look to use that by default.

#### Save, Insert, Update
* For all persist requests like save, insert, update, delete Ebean will create a transaction and perform a COMMIT at the end of the operation.
* 对于像 save, insert, update, delete 所有的持久性操作，Ebean将创建事务并在操作结束时执行提交。
  
### Programmatic（正规的启动事务）
* We programmatically create transactions using ebeanServer.beginTransaction(). When we do so we generally should use try with resources to ensure that the transaction is closed regardless of an exception throw.
* 我们使用ebeanServer.beginTransaction()以编程方式创建事务。当我们这样做时，我们通常应该使用try with resources来确保无论异常抛出与否，事务都是关闭的。

### @Transactional
* We can put @Transactional annotation on methods (including private methods) and Ebean enhancement will add transaction demarcation to those methods.
* 我们可以将@Transactional注释放在方法上(包括私有方法)，Ebean增强将向这些方法添加事务界定。
  ```java
    @Transactional
    public void process(OffsetDateTime startOffset) {
        ...
    }
  ```
#### ebean.mf
* For the @Transactional to work we need to make sure the ebean.mf manifest file specifies the packages that contain the classes annotated with @Transactional such that they are enhanced.
* 为了让 @Transactional 工作，我们需要确定在 ebean.mf 清单文件中明确的指定 包含用@Transactional注释类的 packsge，以便增强他们
* The ebean.mf file is typically located in src/main/resources/ebean.mf.
* ebean.mf 清单文件 典型的本地存放位置在 src/main/resources/ebean.mf.

#### Example ebean.mf
```java
    entity-packages: org.example.domain
    transactional-packages: org.example
    querybean-packages: none
```
* With the example above we are enhancing @Transactional methods for classes in org.example and any sub-packages.
* 上面的例子 在 in org.example 包和任何子包中 增强了 通过@Transactional注解的类

## Batch(批处理)
* We can explicitly turn on the use of JDBC batch via transaction.setBatchMode(true)
* 我们可以明确的 通过transaction.setBatchMode(true) 打开使用JDBC的批处理
  ```java
    try (Transaction transaction = server.beginTransaction()) {

        // use JDBC batch
        transaction.setBatchMode(true);

        // these go to batch buffer
        aBean.save();
        bBean.save();
        cBean.save();

        // batched
        server.saveAll(someBeans);

        // flush batch and commit
        transaction.commit();
    }
  ```

### @Transactional(batchSize)
* When using @Transactional setting the batchSize attribute will turn on JDBC batch.
* 当使用 @Transactional 设置batchSize属性时，将会打开 JDBC的批处理
  ```java
    @Transactional(batchSize = 50)
    public void doGoodStuff() {

        ...
    }
  ```
### Batch size
* The JDBC batch buffer has a size at which it will flush executing the statements(执行的语句) in the batch buffer.
* When the number of beans (or UpdateSql or CallableSql) hits the buffer size then the batch is flushed and all the statements in the buffer are executed.
* 当bean(或UpdateSql或CallableSql)的数量达到缓冲区大小时，将刷新批处理并执行缓冲区中的所有语句。
* If we persist different types of beans, for example "order" and "order details" then when any of the buffers hits the batch size all buffers are flushed. This allows Ebean to preserve the execution order, for example persisting all the "orders" before persisting all the "order details".
* The batch buffer size default value is set via serverConfig.persistBatchSize which <font color = 'red'>defaults to 20</font>. We can set the buffer size on the transaction via <font color = 'red'>transaction.setBatchSize()  OR  @Transactional(batchSize).</font>
* 这个缓冲数量的大小通过在 serverConfig.persistBatchSize 设置  默认值是 20
  ```java
    try (Transaction transaction = server.beginTransaction()) {

        // use JDBC batch
        transaction.setBatchMode(true);
        transaction.setBatchSize(50);

        // these go to batch buffer
        aBean.save();
        ...

        // flush batch and commit
        transaction.commit();
    }
  ```
### Flush(刷新)
* The batch is flushed when:
  * A buffer hits the batch size [缓冲区达到批处理数量]
  * We execute a query - depending on setBatchFlushOnQuery()[我们执行一个查询——这取决于setBatchFlushOnQuery()这个参数]
  * We mix UpdateSql, CallableSql with bean persisting - depending on setBatchFlushOnMixed()-[]
  * We call a getter or setter on a bean property that is already batched e.g. bean.getId()
  * We explicitly call flush()-[我们显示的调用 flush() 方法]
#### setBatchFlushOnQuery
* We set setBatchFlushOnQuery(false) on a transaction when we want to execute a query but don't want that to trigger a flush.
* 我们可以可以在事务上 setBatchFlushOnQuery(false) 当我们想执行查询但又不想引发刷新的时候
  ```java
    try (Transaction transaction = server.beginTransaction()) {
        transaction.setBatchMode(true);
        transaction.setBatchFlushOnQuery(false);

        // these go to batch buffer
        aBean.save();
        ...

        // execute this query but we don't
        // want that to trigger flush
        SomeOtherBean.find.byId(42);

        ...

        transaction.commit();
    }
  ```
#### setBatchFlushOnMixed
* We set setBatchFlushOnMixed(false) on a transaction when we want to execute a UpdateSql or CallableSql mixed with bean save(), delete() etc and don't want that to trigger a flush.
* 我们可以在事务上设置 setBatchFlushOnMixed(false) 当我们想执行 UpdateSql or CallableSql 语句但这个事务中又存在 save(), delete() 等操作，又不想初发批处理刷新的时候；
  ```java
    try (Transaction transaction = server.beginTransaction()) {
        transaction.setBatchMode(true);
        transaction.setBatchFlushOnQuery(false);

        // these go to batch buffer
        aBean.save();
        ...

        // execute UpdateSql but we don't
        // want that to trigger flush
        UpdateSql update = ...
        update.execute();

        ...

        transaction.commit();
    }
  ```
#### Flush on getter/setter
* For beans that are in the batch buffer, if we call getters/setters on a generated property or "unloaded" Id property this will trigger a flush.
  ```java
    try (Transaction transaction = server.beginTransaction()) {
        transaction.setBatchMode(true);

        // bean goes to batch buffer
        myBean.save();
        ...

        // will trigger flush if id is an "unloaded" property and myBean is in the buffer (not flushed)
        long id = myBean.getId();

        // will trigger flush if myBean is in the buffer (not flushed)
        Instant whenCreated = myBean.getWhenCreated();

        ...

        transaction.commit();
    }
  ```
#### Explicit flush()[显示的刷新]
* We may wish to perform an explicit flush() when we want to ensure that any batched statements have been executed. This typically means that we know that all SQL statements have been executed and that all the DB constraints are tested at that point in the application logic.
* 当我们希望确保任何批处理语句都执行过时，就会希望执行显式flush()；这通常意味着我们知道已经执行了所有SQL语句，并且在应用程序逻辑中的该点测试了所有DB约束。
  ```java
    try (Transaction transaction = server.beginTransaction()) {
        transaction.setBatchMode(true);

        // bean goes to batch buffer
        myBean.save();
        ...


        // ensure all SQL statements are executed which means that
        // all DB constraints (unique, foreign key etc) are tested
        transaction.flush();


        // carry on with stuff ...
        ...

        transaction.commit();
    }
  ```

### GetGeneratedKeys
* When we are performing a large bulk insert it is common to turn off getGeneratedKeys as we typically don't use the beans after they have been inserted (so we don't need the keys).
* 当我们在执行大批量插入的时候，通常都会关闭掉 getGeneratedKeys 因为我们通常不会在插入bean之后使用它们（所以我们不需要key）
  ```java
    try (Transaction transaction = server.beginTransaction()) {
        transaction.setBatchMode(true);
        transaction.setBatchSize(100);

        // turn off GetGeneratedKeys ... as we don't need them
        transaction.setBatchGetGeneratedKeys(false);

        // maybe even turn off persist cascade ...
        transaction.setPersistCascade(false)

        // perform lots of bean inserts ...
        ...

        transaction.commit();
    }
  ```
### Configuration via ServerConfig
* We can set serverConfig.setPersistBatch(PersistBatch.ALL) so that JDBC batch mode is the default being used for all transactions.
* 我们可以设置 set serverConfig.setPersistBatch(PersistBatch.ALL) 以便所有事务都默认使用JDBC批处理模式。
  ```java
    // use JDBC batch by default
    serverConfig.setPersistBatch(PersistBatch.ALL);
  ```

* We can change the global default batch size via serverConfig.setPersistBatchSize(). The default is otherwise set at 20.
  ```java
    // default batch size
    serverConfig.setPersistBatchSize(50);
  ```
* When using batch update we have the option to include the dirty properties or all loaded properties in the update. If we choose dirty properties we will include less properties in the update statement but we may get more distinct update statements being executed (in the case where the application logic doesn't update the same properties on all the beans being updated).
  ```java
    // update all loaded properties or just dirty properties
    serverConfig.setUpdateAllPropertiesInBatch(true);
  ```

## Scopes（作用域）
name | Description
-|-
REQUIRED（必需的）	| Uses an existing transaction and if none exists will starts a new Transaction. This is the default. 
MANDATORY（强制的） |A transaction MUST already have been started. Throws TransactionRequiredException.
SUPPORTS（支持的，可有可无） | Uses the existing transaction if one exists, otherwise the method does not run with a transaction. Used this with caution.
REQUIRES_NEW（每次都创建新的）  |  Always start a new transaction. Suspend an existing once if required.
NOT_SUPPORTED （不支持）| Suspends an existing transaction if required. Method runs without a transaction.（如果运行的话会挂起现有的事务。方法将在没有事务的情况下运行）
NEVER |  If there is an existing transaction throws an Exception. Method runs without a transaction.

### REQUIRED is default
* The default transaction scope when not specified is REQUIRED. This is by far（到目前为止） the most common transaction scope used in most applications.

#### Example with @Transactional
* We can specify(指定，说明) other transaction scopes on methods annotated with @Transactional.
  ```java
    @Transactional(type = TxType.REQUIRES_NEW)
    public void doInner() {
      // execute using a new transaction ...

    }
  ```

### Example with beginTransaction
* We can specify other transaction scopes via beginTransaction(TxScope).
  ```java
    try (Transaction transaction = server.beginTransaction(TxScope.requiresNew())) {
      // using a new transaction ...
      transaction.commit();
    }
  ```

## Savepoint
* Ebean supports using Savepoints which are a feature of most Relational Databases in which you can create a "Save point" inside a transaction to which you can rollback to in case of（假如、一旦、万一） an error or some other business logic.
* Ebean支持使用保存点，这是大多数关系数据库的一个特性，您可以在事务中创建一个“保存点”，假如出现错误或其他业务逻辑错误时回滚到该保存点。
* If you have part of a task that performs some operations which can fail/rollback, but we don't want to loose all the work, Savepoint provides a mechanism to do this. You can check more information on Oracle's JDBC tutorial
* 如果任务的一部分执行一些可能失败/回滚的操作，但我们不想丢失所有工作，那么Savepoint提供了一种机制来实现这一点。您可以在Oracle的JDBC教程中查看更多信息

### Transaction.setNestedUseSavepoint()
* For a transaction we can use setNestedUseSavepoint() to enable it to use Savepoint for nested transactions. This means that these nested transactions can be rolled back but leaving the outer transaction to continue and potentially commit.
* 对于事务，我们可以使用setNestedUseSavepoint()使其能够对嵌套事务使用保存点。这意味着可以让这些嵌套事务进行回滚，但让外部事务继续并可能提交。
#### Example
  ```java
    // start 'outer' transaction
    try (Transaction outerTxn = server.beginTransaction()) {

      outerTxn.setNestedUseSavepoint();

      // do stuff with the 'outer' transaction
      bean.save();

      try (Transaction nestedTransaction = server.beginTransaction()) {
        // nested transaction is a savepoint ...

        // do some piece of work which we might want to either commit or rollback ...
        otherBean.save();

        if (...) {
          nestedTransaction.rollback();

        } else {
          nestedTransaction.commit();
        }
      }

      // continue using 'outer' transaction ...

      outerTxn.commit();
    }
  ```

## Explicit Savepoint（显示的保存点）
* As an alternative(另类 替换物 取舍) to using transaction.setNestedUseSavepoint() we can instead explicitly create and use savepoints by obtaining the Connection from the transaction.
* 作为使用transaction. setnestedusesavepoint()的替代方法，我们可以通过从事务中获取连接显式地创建和使用保存点。
* For example:
  ```java
    Customer newCustomer = new Customer();
    newCustomer.setName("John");
    newCustomer.save();

    try (Transaction transaction = Ebean.beginTransaction()) {

      Connection connection = transaction.getConnection();
      // Create a Save Point
      Savepoint savepoint = connection.setSavepoint();

      newCustomer.setName("Doe");
      newCustomer.save();

      // Rollback to a specific save point
      connection.rollback(savepoint);
      transaction.commit();
    }

    Customer found = Ebean.find(Customer.class, newCustomer.getId());
    System.out.println(found.getName()); // Prints "John"
  ```