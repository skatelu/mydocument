# Ebean的创建与基本使用

##  Database（数据源）

* The [Database API](https://ebean.io/apidoc/11/io/ebean/Database.html) provides almost all of the functionality including `queries`, `saving`, `deleting` and `transactions`.

* ```java
  // persist ...
  Customer customer  = ...
  database.save(customer);
  
  
  // fetch ...
  List<Customer> customers =
    database.find(Customer.class)
    .findList();
  
  
  // transactions ...
  try (Transaction transaction = database.beginTransaction()) {
  
    // fetch and persist beans etc ...
  
    transaction.commit();
  }
  ```

* Note that a Database can have a second read only DataSource that can be used for performance reasons (for read only queries) and potentially connect to database read replicas.
* 注意，数据库可以有第二个只读数据源，该数据源可用于性能原因(用于只读查询)，并可能连接到数据库读副本。



#### Default database（默认数据源）

* One database can be specified as the `default database`. The default Database instance is registered with `DB` and then can be obtained via `DB.getDefault()`.

  ```java
  // obtain the "default" database
  Database database = DB.getDefault();
  ```

  #### Named database

  Each Database instance has a name and can be registered with `DB` and then obtained by it's name via `DB.byName()`

  ```java
  // obtain a database by it's name
  Database hrDatabase = DB.byName("hr");
  ```

#### Create database（创建数据源）

* We can programmatically create a Database instance using [DatabaseFactory](https://ebean.io/docs/introduction/databasefactory) and [DatabaseConfig](https://ebean.io/docs/introduction/databaseconfig) or it can be created automatically from properties file configuration (for testing we often use application-test.yaml and have the database instance created automatically).

* 我们可以通过编程使用DatabaseFactory和DatabaseConfig创建数据库实例，也可以通过属性文件配置自动创建数据库实例(对于测试，我们通常使用应用程序测试)。并自动创建数据库实例)。

```java
// configuration ...
DatabaseConfig config = new DatabaseConfig();
config.addClass(MyEntityBean.class);
...

// create database instance
Database database = DatabaseFactory.create(config);
```

## DB (io.ebean.DB)

- DB holds a **registry** of database instances (keyed by name)
- DB holds one Database instance known as the **default database**
- DB has **convenience** methods to operate on the default database

#### Obtain(获得) default or named database instance（获得默认或者命名的数据库实例）

* When an Database instance is created it can be registered with DB. We can then use `DB` to obtain those Database instances by name and we can obtain the default database via `DB.getDefault()`.

* ```java
  // obtain the "default" database
  Database server = DB.getDefault();
  
  // obtain the HR database by name
  Database hrDB = DB.byName("hr");
  ```

#### DB convenience methods（默认数据库 便利的方法）

* DB has convenience methods that effectively proxy through to the default database. This is useful and convenient as many applications only use a single database.（对于大多数的单数据库应用程序来说，这些是非常有效的）

* ```java
  Customer customer  = ...
  
  // save using the 'default' database
  DB.save(customer);
  
  // which is the same as
  Database defaultDatabase = DB.getDefault()
  defaultDatabase.save(customer);
  ```

#### DB is optional （可选择的）

* Note that you don't have to use the DB singleton in your application at all - it's use is completely optional. Instead we can create the Database instance(s) and `inject` them where needed. We typically do this using `Spring` or `Guice` or a similar DI framework.

## EbeanServer

* Database and EbeanServer are the same thing.
* 数据库和EbeanServer是一回事
* `io.ebean.EbeanServer` is the older original name for `io.ebean.Database`. They can be used interchangably. In time EbeanServer will be deprecated and code should migrate to use io.ebean.Database.
* io.ebean.EbeanServer 是 io.ebean.Database 最开始的名字。他们可以相互交换使用；不推荐使用EbeanServer，代码应该迁移到使用io.ebean.Database。

## Ebean

* DB and Ebean are the same thing.
* `io.ebean.Ebean` is the older original name for `io.ebean.DB`. They can be used interchangably. In time use of Ebean will be deprecated and code should migrate to use io.ebean.DB.



## Model（保存和删除bean使用）

* `io.ebean.Model` is a base class that is extended by entity beans or MappedSuperclass beans to provide convenience methods for saving and deleting beans.

* io.ebean.Model是一个基类，它继承实体bean或MappedSuperclass bean，提供方便的方法来保存和删除bean。
* That is, the entity beans themselves then have methods such as `save()` and `delete()` for persisting.

#### Entity extending Model

```java
@Entity
@Table(name="customer")
public class Customer extends Model {

  @Id
  long id;

  @Version
  long version;

  String name;
  ...
}
```

* With Customer entity bean extending model we can `save()` or `delete()`it.

* ```java
  Customer customer = new Customer("Joe", "Montana");
  
  // save using the default database
  customer.save();
  ```

#### Default database

* When we call the save() and delete() methods on Model it is obtaining the default database and using that to save or delete the entity bean.
* 当我们调用模型上的save()和delete()方法时，它将获得默认数据库并使用该数据库保存或删除实体bean。

#### Typical MappedSuperclass （典型的 MappedSuperclass）

* Most often entity beans do not directly extend Model but instead we have a common MappedSuperclass that has commmon properties that our entity beans share.

* 大多数情况下实体bean 不能直接继承 Model 但是我们有一个公共的MappedSuperclass，它具有实体bean共享的commmon属性。   可以使用这个MappedSuperclass 这个类，来继承Model，实体类继承MappedSuperclass 这个类 来实现属性共享

* ```java
  @MappedSuperclass
  public abstract class BaseDomain extends Model {
  
    @Id
    long id;
  
    @Version
    long version;
  
    @WhenCreated;
    Instant whenCreated;
  
    @WhenModified;
    Instant whenModified;
  
    ...
  }
  ```

* Our entity beans then extend BaseDomain inheriting the common properties.

* ```java
  @Entity
  @Table(name="customer")
  public class Customer extends BaseDomain {
  
    String firstName;
  
    String lastName;
    ...
  }
  ```



## Finder（查找器）

* `io.ebean.Finder` is a base class that we can extend to create "finders" for each entity bean.

*  The purpose(目的) of "finders" is to group all the queries for the given bean type together in one place rather than have the queries spread out across the cost base.

* We don't have to use Finders and when we don't we typically either put queries in the service layer code or use `repositories`.

#### Example

```java
public class CustomerFinder extends Finder<Long,Customer> {

  public CustomerFinder() {
    super(Customer.class);
  }

  // Add customer finder methods ...

  public Customer byName(String name) {
    return query().eq("name", name).findOne();
  }

  public List<Customer> findNew() {
    return query().where()
      .eq("status", Customer.Status.NEW)
      .orderBy("name")
      .findList()
  }
}

@Entity
public class Customer extends BaseModel {

  public static final CustomerFinder find = new CustomerFinder();
  ...
 }
```

* Once we have a finder as a field on an entity bean we can then use it via.

```java
Customer rob = Customer.find.byName("Rob");

List<Customer> customers = Customer.find.findNew();
```

#### Testing with mocki-ebean

* For testing purposes we can use mocki-ebean to replace the finder implementation with a test double.For testing purposes we can use mocki-ebean to replace the finder implementation with a test double. Note that with docker based testing this is rarely used now and instead component testing against real databases (docker based) is used instead.

## Finder generation via ebeaninit

* We can use the [ebeaninit](https://ebean.io/docs/tooling/cli-tool) command line tool to generate finders for us (both for Java and Kotlin).



## ebean.mf（配置清单，用来表述增强Ebean的哪些东西）

* The `ebean.mf` manifest file controls the enhancement. It controls:
  * What packages are enhanced   增强了哪些包？
  * What enhancement features are used   使用了哪些增强功能

* It is typically located in `src/main/resources/ebean.mf`.

### Example 1

```xml
entity-packages: org.example.domain
transactional-packages: org.example
querybean-packages: none
```

* With example 1 we are enhancing entity beans in the `org.example.domain` package，we are also enhancing(增强) `@Transactional` methods but we are not using query bean enhancement.

### Example 2

```xml
profile-location: true
entity-packages: org.example.domain
transactional-packages: org.example
querybean-packages: org.example
```

* With example 2 turns on `profile-location` enhancement and `query bean` enhancement.

### Tooling

The ebean.mf file is read by all the tooling that performs enhancement. It is read by:

- IntelliJ IDEA plugin
- Eclipse plugin
- Maven enhancement plugin
- Gradle enhancement plugin



## DatabaseConfig

* `io.ebean.DatabaseConfig` has all the configuration available to then create a a Database instance.

* `io.ebean.DatabaseConfig` 提供了所有可获得的用来创建数据源实例的配置

* ```java
  DatabaseConfig config = new DatabaseConfig();
  
  // set configuration options
  config.set...
  ...
  // and/or load configuration properties file
  config.loadFromProperties();
  
  // create the Database instance
  Database database = DatabaseFactory.create(config);
  ```



## DatabaseFactory

* `io.ebean.DatabaseFactory` has the static factory method that takes a DatabaseConfig and creates a Database instance.

* ```java
  DatabaseConfig config = new DatabaseConfig();
  
  // set configuration options
  config.set...
  ...
  // and/or load configuration properties file
  config.loadFromProperties();
  
  // create the Database instance
  Database database = DatabaseFactory.create(config);
  ```

* 自动创建数据源的例子

* ```java
  @Entity
  public class Customer {
  
      @Id
      long id;
  
      String name;
  
      public long getId() {
          return id;
      }
  
      public void setId(long id) {
          this.id = id;
      }
  
      public String getName() {
          return name;
      }
  
      public void setName(String name) {
          this.name = name;
      }
  }
  ```

* 测试类   相关自建表配置连接 <https://www.jianshu.com/p/999255118025>

* ```java
      @Test
      public void insertFindDelete() {
  
        Customer customer = new Customer();
        customer.setName("Hello world1");
  
        DatabaseConfig config = new DatabaseConfig();
  
        DataSourceConfig sourceConfig = new DataSourceConfig();
        sourceConfig.setDriver("com.mysql.jdbc.Driver");
        sourceConfig.setInitDatabaseForPlatform("test");
        sourceConfig.setUrl("jdbc:mysql://172.22.5.6:3306/test");
        sourceConfig.setUsername("root");
        sourceConfig.setPassword("123456");
        sourceConfig.setMaxConnections(100);
        sourceConfig.setMaxInactiveTimeSecs(120);
  
        config.setDataSourceConfig(sourceConfig);
             
        //是否执行建表SQL
        //config.setDdlRun(true);
        //是否生成建表SQL
        //config.setDdlGenerate(true);
        //是否跳过删表SQL
        //config.setDdlCreateOnly(true);
        
        Database database = DatabaseFactory.create(config);
  
        // insert the customer in the DB
        database.save(customer);
  
  
        // Find by Id
        Customer foundHello = database.find(Customer.class, 1);
  
        System.out.print("hello " + foundHello.getName());
  
        // delete the customer
  //        database.delete(customer);
      }
  ```

## Spring    FactoryBean （程序中未使用，使用的原生的jar包自带的类，并未用整合的Spring-Ebean）


