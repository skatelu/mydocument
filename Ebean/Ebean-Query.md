# Ebean-Query
## Query beans
* Query Beans 是可选择的，但是他们提供了一套很不错的方式 type safe compile time checking 来编写查询；
* 当使用java 注解处理的时候就会生成Query beans
* 对于每个实体，都会生成一个名称相同但前缀为Q的查询bean。因此，对于名称为Customer的实体bean，会生成一个名称为QCustomer 的查询bean
* 当实体bean模型发生更改时，将重新生成查询bean，作为开发人员，我们应该在编译时检查我们应用程序build all；如果我们的查询对模型不再有效，我们将得到一个编译时错误。
* **Examples:**
  ```java
    Contact contact =
        new QContact()                               // Contact query bean
            .email.equalTo("rob@foo.com")              // type safe expression with IDE auto-completion
            .findOne();
  ```
  ```java
    List<Customer> customers =
        new QCustomer()                              // Customer query bean
            .status.equalTo(Status.NEW)
            .billingAddress.city.equalTo("Auckland")
            .contacts.isEmpty()
            .findList();
  ```
#### APT / KAPT (generating query beans)
* **Query bean generation with Maven**
  We can optionally add a Java annotation processor (APT) to generate query beans. This allows us to write type safe queries like:
  ```java
        List<Customer> customers =
            new QCustomer()
            .name.startsWith("Rob")
            .findList();
  ```
    
  To generate Java query beans with maven add provided scope dependency for querybean-generator
  要生成 java query beans 我们需要在maven中添加范围依赖
  ```xml
        <!-- annotation processor to generate query beans -->
        <dependency>
        <groupId>io.ebean</groupId>
        <artifactId>querybean-generator</artifactId>
        <version>11.39.1</version>
        <scope>provided</scope>
        </dependency>
  ```
### Enhancement
* Query bean 需要增强才能工作。在 src/main/resources/ebean.mf 清单文件中，我们需要通过querybean-packages指定要为那个包进行query beans 增强
* 注意，对于java程序，我们需要增强调用 query ebeans 的代码；
* **Example**
  ```java
    entity-packages: org.example.domain
    transactional-packages: org.example
    querybean-packages: org.example
  ```

#### Caveats(警告)
* **Refactor rename entity bean**
  When we refactor rename an entity bean in an IDE the query beans we should trigger a build all.（当我们在IDE中重新弄构建重命名一个 实体 bean的时候，我们应该一起重新build all query benas）；This is because the query beans for the old bean names are kept around until the next build all.
* Once we do a build all this will effectively delete the old query beans and generate the new ones.At this point we get compiler errors for the queries that we need to adjust (use the new query bean names).

## where
* Query beans 的属性提供了一种安全的方式来添加where 从句表达式。我们会在编译时对 查询路径、表达式和绑定值类型，进行校验
  ```java
    // using query bean
    Customer rob =
    new QCustomer()
        .name.equalTo("Rob") // type safe expression with IDE auto-completion
        .findOne();
  ```
* 在不使用 Query beans的时候，编写相同的查询：
  ```java
    // using standard query
    Customer rob = DB.find(Customer.class)
        .where().eq("name", "Rob")
        .findOne();
  ```

* 以上两个查询都生成相同的sql:
  ```java
    select ...
    from customer t0
    where t0.name = ?
  ```

* 上述情况中使用的 name 的属性的类型是 String/varchar，因此只有字符串类型的表达式是有效的，如 like, startsWith, contains等才是被允许使用的，并且表达式的绑定之必须是字符串

* 另外关于query beans，如果他的属性被重新命名（如：e.g. "name" became "fullName"）或者 属性的数据类型发生改变，这个查询将不会被编译；

#### Paths
* 我们可以通过添加有效表达式来“导航”这些路径,从而实现与bean相关属性关联(OneToOne, one etomany, ManyToOne, ManyToMany)，即：**关联查询**
* **e.g. path "billingAddress.city" for Customer**
  ```java
    // using query beans
    List<Customer> customers =
    new QCustomer()
        .billingAddress.city.equalTo("Auckland")
        .findList();
  ```
  ```java
    // using standard query
    List<Customer> customers = DB.find(Customer.class)
    .where()
        .eq("billingAddress.city", "Auckland")
    .findList();
  ```
  Ebean will automatically add appropriate SQL JOINS to support the expressions in the query.（Ebean将自动添加适当的SQL JOINS 来完成SQL表达式）
  ```SQL
    select ...
    from customer t0
    join address t1 on t1.id = t0.billing_address_id
    where t1.city = ?
  ```
* 这些path可以是任意深度；在下面的示例中，路径将我们从Order bean带到Customer bean，再通过Customer.billingaddress到达Address bean。
  **e.g. path "customer.billingAddress.city" for Order**
  ```java
    List<Order> orders =
        new QOrder()
            .customer.billingAddress.city.equalTo("Auckland")
            .findList();
  ```

#### AND
* By default multiple expressions are added via AND.(默认情况下，通过AND连接多个表达式。)
  ```java
    List<Customer> customers = 
        new QCustomer()
            .status.equalTo(Sattus.NEW)
            .whereCreated.greaterThan(lastWeek)
            .findList();
  ```
  ```SQL
    select ...
    from customer t0
    where t0.status = ? and t0.when_created > ?
  ```
  ```java
    // using standard query
    List<Customer> customers = DB.find(Customer.class)
        .where()
            .eq.("status",Status.NEW)
            .gt("whenCreated",lastWeek)
            .findList()
  ```
* **e.g. multiple paths for Order**
  ```java
    List<Order> orders =
        new QOrder()
            .orderDate.greaterThan(lastWeek)
            .customer.status.equalTo(Status.NEW)
            .customer.billingAddress.city.equalTo("Auckland")
            .findList();
  ```
  通过上面的查询，我们得到了customer和customer. billingaddress的附加路径;Ebean 将决定需要的支持的表达式进行连接；
  ```SQL
    select ...
    from orders t0
    join customer t1 on t1.id = t0.order_id
    join address t2 on t2.id = t1.billing_address_id
    where t0.order_date >= ? and t1.status = ? and t2.city = ?
  ```
* **连接类型说明**
  * 连接类型(left join or join)的使用是根据关系的基数和可选性来确定的。例如，如果 order 的外键 customer 为空，然后，这是一个可选关系，并使用左联接。
  * 对于任何的path，如果“higher level”连接是左连接，那么所有“child joins”(对于该路径)也必须是左连接。例如：在上面的例子中-如果 order到customer的连接是左连接，那么从customer到address的连接也必须是左连接。
  * 对于ToMany path 表达式连接类型的使用，取决于表达式中是否包含 OR。如果表达式内部存在OR，则使用 left join

#### OR
* 当我们想通过OR添加多个表达式时，我们使用的 or() 和 endOr() 他们之间的所有表达式都是通过 OR 连接。例如(name is null OR name = 'Rob')
  ```java
    Customer customer
        = new QCustomer()
            .or()
                .name.isNull()
                .name.equalTo("Rob")
            .endOr()
            .findOne()
  ```
  ```SQL
    select ...
    from customer t0
    where (t0.name is null or t0.name = ? )
  ```
  ```java
    Customer customer = DB.find(Customer.class)
    .where()
        .or()
            .isNull("name")
            .eq("name", "Rob")
        .endOr()
        .findOne()
  ```
* **或者使用原始表达式**
  我们也可以使用 raw() 表达式，而不是使用带有or()和endOr()的流体样式。例如：
  ```java
    Customer customer
    = new QCustomer()
        .raw("(name is null or name = ?)", "Rob")
        .findOne()
  ```

#### Raw expressions
* 有时，我们希望将 raw 表达式添加到where子句中。我们可以使用的 raw 表达式 将 任意的SQL、函数和存储过程 放入到query where子句中。
  **e.g. simple raw expression**
  ```java
    List<Order> orders =
        new QOrder()
        .raw("orderDate > shipDate ")
        .findList()
  ```
  **e.g. use sql function**
  ```java
    List<Order> orders =
        new QOrder()
        .raw("add_days(orderDate, 10) < ?", someDate)
        .findList();
  ```
  **e.g. sql subquery(子查询)**
  ```java
    List<Order> orders =
        new QOrder()
        .status.equalTo(Status.NEW)
        .raw("t0.customer_id in (select customer_id from customer_group where group_id = any(?::uuid[]))", groupIds)
        .findList()
  ```
* **Property paths to sql joins**
  如果我们在raw()中使用属性路径，那么Ebean将自动添加适当的连接来支持表达式。 例如；
  ```java
    List<Order> orders =
        new QOrder()
            .raw("(customer.name = ? or customer.billingAddress.city = ?)", "Rob", "Auckland")
            .findList();
  ```
  会将customer 和 address 表使用以下的连接添加到生成的SQL中；
  ```SQL
    select t0.id, t0.status, t0.order_date, t0.ship_date, ...
    from orders t0
    left join customer t1 on t1.id = t0.customer_id            -- supports customer.name
    left join address t2 on t2.id = t1.billing_address_id      -- supports customer.billingAddress.city
    where (t1.name = ? or t2.city = ?); --bind(Rob,Auckland)
  ```
* **Combing expressions (组合表达式)**
  我们可以将raw()表达式与其他表达式结合使用。 例如：
  ```java
    List<Order> orders =
        new QOrder()
            .status.eq(Order.Status.NEW)       // before raw()
            .raw("(customer.name = ? or customer.billingAddress.city = ?)", "Rob", "Auckland")
            .orderDate.greaterThan(lastWeek)   // after raw()
            .findList();
  ```
  ```SQL
    select ...
    where t0.status = ?  and (t1.name = ? or t2.city = ?) and t0.order_date > ?
    --bind(NEW,Rob,Auckland,2019-01-17)
  ```
* **Ode to Raw expressions**
  如果开发人员的生活很简单，我们就不需要raw表达式。许多现实世界的项目经常有一些查询，我们的ORM查询可以让我们得到90%的结果，但是所需要的谓词最好用SQL表示，或者使用数据库特定的函数或特性。
  Raw 表达式是一个很好的小特性，它允许我们将任意SQL表达式放入where子句中。我们可以去findNative等，为查询提供所有的SQL，但这是一个更大的跳跃，原始表达式为我们提供了灵活性。
  我不确定是否允许我使用最喜欢的特性，但如果允许的话，我会投票支持raw表达式。

## Expressions  
#### raw()
* Raw 表达式允许我们在 query 的 where子句中使用 数据库任意的特定函数或表达式
  ```java
    // e.g. use a database function
    .raw("add_days(orderDate, 10) < ?", someDate)

    // e.g. subquery
    .raw("customer.id in (select o.customer_id from orders o where o.id in (?1))", orderIds);
  ```
--------------------------------
###Convenience(便利的) expressions
  这些表达式将其他简单的表达式组合在一起。我们这样做是因为它们在应用程序中经常出现。
#### inRange() 区间表达式
* <code><font color = 'red'>property >= value1 and property < value2</font></code>
 ```java
    .orderDate.inRange(today.minusDays(7), today)
 ```
 * 区间表达式类似于BETWEEN，除了是“半开区间”。属性严格`小于最大值`，而不是小于或等于。
  这使得 inRange 在定义时间戳和日期等间隔时更加实用。

#### inOrEmpty()
* 这是一个“条件IN”，只有当集合不为空时才添加IN表达式。为空时不生效
  ```java
    List<Long> customerIds = ...

    // only add the expression if the customerIds is not empty
    .customer.id.inOrEmpty(customerIds)
  ```
  如果customerIds集合不是空的，那么上面的代码只会添加IN表达式。

#### rawOrEmpty()
* 这是一个“条件 raw 表达式”，其中 raw 表达式使用一个集合(类似于 raw 子查询表达式) 并且 我们只在集合不是空的情况下才添加 raw 表达式。
  ```java
    List<String> names = ...

    // only add the expression if the names is not empty
    .rawOrEmpty("customer.id in (select c.id from customer c where c.name in (?1))", names)
  ```
--------------------------------------------
### Simple expressions
  下面的表达式是简单的表达式。
#### isNull()
* 在许多关联的属性上的 IsNull转换为 `isEmpty()`

#### isEmpty()
* isEmpty() 表达式被用在ToMany属性上。sql exists 子查询就是被 isEmpty 表达式实现的；
  
#### in()
#### inPairs()
#### like
#### startsWith
#### endsWith
#### contains
#### eq - equal to
#### ne - not equal
#### gt - greater than
#### ge - greater than or equal
#### lt - less than
#### le - less than or equal
#### between
#### betweenProperties
#### example
#### bitwiseAny
#### bitwiseAnd
#### bitwiseAll
#### bitwiseNot
#### arrayContains
#### arrayNotContains
#### arrayIsEmpty
#### arrayIsNotEmpty

## OrderBy
* Query bean 具有 `asc()` and `desc()` 方法，以便将它们添加到orderBy子句中。
  ```java
    List<Customer> customers =
        new QCustomer()
            .status.in(Status.NEW, Status.ACTIVE)
            .orderBy()
                .name.desc() // order by t0.name desc
            .findList();
  ```
  生成以下SQL:
  ```SQL
    select ...
    from customer t0
    where t0.status in (?, ?)
    order by t0.name desc
  ```
  我们可以向orderBy子句添加多个属性。
  ```java
    List<Customer> customers =
        new QCustomer()
            .status.in(Status.NEW, Status.ACTIVE)
            .orderBy()
                .name.desc()
                .id.asc()
            .findList();
  ```
* **Standard orderBy**
  于标准查询，我们使用orderBy().desc()或orderBy().asc()，它们也可以被链接.在不使用 query bean的情况下编写与上面相同的查询:
  ```java
    List<Customer> customers = database.find(Customer.class)
        .where().in("status"), Status.NEW, Status.ACTIVE)
        .orderBy()
            .desc("name")
            .asc("id")
        .findList();
  ```
### OrderBy expression
* We can specify an order by expression which can include sql formula via orderBy(string).
* 我们可以通过表达式指定一个order，它可以通过orderBy(string)包含sql公式。
* **e.g. simple expression**
  ```java
    new QCustomer()
      .orderBy("name asc,status desc")
      .findList()
  ```
* **e.g. expression using DB function**
  ```java
    List<Customer> customers =
      new QCustomer()
        .status.in(Status.NEW, Status.ACTIVE)
        .orderBy("coalesce(activeDate, now()) desc, name, id")
        .findList();
  ```
### Nulls high, Nulls low
* The order by expression can include nulls high or nulls low.
* order by 表达式，可以将null值设置成最高值,也可以将null值设置成最小值
  ```java
    List<Customer> customers =
      new QCustomer()
        .orderBy("name nulls high, id desc")
        .findList();
  ```
### Collate
* We can specify the collate to use via asc() and desc().
  ```java
    List<Customer> customers =
      new QCustomer()
        .orderBy()
          .asc("name", "latin_2");
          .desc("id", "latin_1");
        .findList();
  ```
### ToMany ordering(未看懂)
* Note that when an ORM query is executed as multiple sql queries due to the use of maxRows or fetchQuery (refer to fetch rules ) - then the associated orderBy expressions for the toMany relationship is automatically moved to the appropriate sql query.
  ```java
    List<Customer> customer =
      new QCustomer()
        .contacts.fetch("firstName, lastName")
        .orderBy()
          .name.asc()
          .contacts.email.asc()              // (1) automatically moved to secondary sql query
        .setMaxRows(10)                      // (2) maxRows
        .findList();
  ```
* The above ORM query is executed as 2 sql queries due to the maxRows - see fetch rules.
  ```java
    -- Primary query
    select t0.id, t0.status, t0.name, ...
    from customer t0
    order by t0.name
    limit 10                                -- (2)
  ```
  ```java
    -- Secondary query
    select t0.customer_id, t0.id, t0.first_name, t0.last_name
    from contact t0
    where (t0.customer_id) in (? )
    order by t0.email                       -- (1)
  ```

## Select
* select allows us to control the properties that are fetched(捕获的). We can specify only selected properties to fetch.
* select允许我们控制获取的属性。我们可以明确的只获取选定的属性；
  ```java
    List<Customer> customers =
      query()
        .select("name, registered, version")
        .findList();
  ```
* With the above we specify the properties we want to fetch. We still get Customer entity beans returned but they are partially(部分的) populated(完全填充).
* The SQL generated by the above query has:(上述查询生成的sql)
  ```java
    select t0.id, t0.name, t0.registered, t0.version from customer t0
  ```
* Note that the @Id property is automatically(自动的) included(加入、包括) for most queries and it is automatically exludeded(排除) for **distinct**, **findSingleAttribute** and **aggregation queries**.
  
#### Query beans
* Query beans provide(提供) a type safe way to define(定义) what part of the object graph to fetch in addition to(除...之外) the approaches provided via the normal Query.
* 除了通过普通的查询之外，Query beans 提供了一种类型安全的方法来定义要获取对象图的哪一部分
* Each query bean provides an alias(别名) bean that can be used in select and fetch.
* 每一个query bean 都提供了一个别名 bean，可以在 select 和 fetch中使用
* In the example below we specify which properties to include in the select and only these properties are loaded into the entity beans. This is described as "partial object".
* 在下面的例子中，我们指定了哪些属性包含在 select 中，并且只有这些属性被加载到实例中；
  ```java
    // "alias" bean that can be used in select and fetch clauses
    QCustomer cust = QCustomer.alias();

    List<Customer> customers =
      new QCustomer()
      // only fetch some properties of customer (partial objects)
      .select(cust.name, cust.version, cust.whenCreated)
      .name.istartsWith("Rob")
      .findList();
  ```
* The above query produces the following sql:
  ```java
    select t0.id, t0.name, t0.version, t0.when_created
    from customer t0
    where lower(t0.name) like ? escape'|'; --bind(rob%)
  ```
### Formula（公式）
* We need to use this string select clause when we want to specify a formula.
* 当我们想使用指定的公式时，我们需要使用 String 类型的选择子句  即在select 中用string 类型的 公式
  ```java
    // java
    List<String> names =
      new QContact()
        .select("concat(lastName,', ',firstName)")
        .lastName.startsWith("A")
        .findSingleAttributeList();
  ```
  ```java
    // kotlin
    var names: List<String> =
      QContact()
        .select("concat(lastName,' ',firstName)")
        .lastName.startsWith("A")
        .findSingleAttributeList()
  ```
* **Example - Postgis ST_Distance formula**
* With this example we explicitly cast to a Java BigDecimal type using the `::BigDecimal` cast at the end of the formula.
* 在本例中，我们使用公式末尾的'::BigDecimal '显式强制的转换为Java BigDecimal类型。
  ```java
    // given route is a Postgis geometry(linestring,4326)
    // ST_Distance() 这个是Mysql数据库的距离函数 参数是两个经纬度的信息，计算结果是度；
    // SELECT st_distance (point (1, 1),point(2,2) ) * 111195
    // //输出结果：157253.47706807632 单位：米
    // st_distance 计算的结果单位是度，需要乘111195（地球半径6371000*PI/180）是将值转化为米。
    // return the distance between the start and end points

    BigDecimal routeDistance = query()
      .select("ST_Distance(ST_StartPoint(route), ST_EndPoint(route))::BigDecimal")
      .where()
      .idEq(tripId)
      .findSingleAttribute();
  ```
### asDto
* When we use a select formula we sometimes want to return the result into a plain DTO bean rather than an entity bean. We do this using .asDto(Class<D>) to turn the ORM query into a DtoQuery.
* 当我们使用 select公式时，我们有时候希望返回的结果封装到普通DTO bean中而不是实体 bean中。我们可以使用 .asDto(Class<D>) 这个函数将ORM查询转换为DtoQuery。(DTO bean 可以理解为普通类，即在数据库中不存在映射表的类)
* For more details(详情) refer to DTO queries.
  ```java
    // ContactDto is a plain bean with email and fullName properties
    List<ContactDto> contacts =
      new QContact()
        .select("email, concat(lastName, ', ', firstName) as fullName")
        .lastName.startsWith("A")
        .orderBy()
          .lastName.asc()
        .setMaxRows(10)
        .asDto(ContactDto.class)
        .findList();
  ```
### Aggregation(聚合)
* Similar to formula queries we can use the standard aggregations of SUM, MAX, MIN, AVG and COUNT in the select clause.
* 与公式查询相类似，我们可以在select子句中使用SUM、MAX、MIN、AVG和COUNT的标准聚合。
* For more details refer to Aggregation queries.
* **Single aggregation queries**
  ```java
    // java
    Timestamp maxWhen  =
      new QContact()
        .select("max(whenModified)")
        .findSingleAttribute()
  ```
  ```java
    // kotlin
    var maxWhen: Timestamp =
      QContact()
        .select("max(whenModified)")
        .findSingleAttribute()
  ```
  ```java
    select max(t0.when_modified) from contact t0;
  ```
* **Group by aggregation queries**
* When multiple column aggregation query we use the string select clause.
* 当有多个列且有进行聚合查询操作时我们使用 string select clause；
  ```java
    List<MachineStats> result =
      new QMachineStats()
      .select("machine, date, max(rate)")
      .date.gt(LocalDate.now().minusDays(10))
      .query().having().gt("max(rate)", 4)
      .findList();
  ```
* The above query produces the following sql:
* 以上查询会产生以下的SQL语句
  ```SQL
    select t0.machine_id, t0.date, max(t0.rate)
      from d_machine_stats t0
      where t0.date > ?
      group by t0.machine_id, t0.date
      having max(t0.rate) > ?
  ```
## Fetch
* With fetch we specify the properties that should be fetched on associated(关联的) beans. Beans that are related by OneToOne, OneToMany, ManyToOne and ManyToMany.
* 使用fetch，我们可以从关联的bean上获取指定的属性。由一个对一个，一个对多个，许多对一个，许多对任何一个的关系
* Fetch means we want the query to eagerly load that path and that we prefer for that to happen as a SQL JOIN.
* Fetch 意味着我们希望急切地加载查询那个 path 并且 我们更喜欢以SQL 连接的形式产生
  ```java
    List<Customer> customers =
      database.find(Customer.class)
      .select("name, version, whenCreated")    // root level properties
      .fetch("contacts", "email")              // contacts is a OneToMany path

      .where()
      .istartsWith("name", "Rob")
      .findList();
  ```
* The resulting sql is:
  ```java
    select t0.id, t0.name, t0.version, t0.when_created,    -- customer columns
       t1.id, t1.email                                 -- contact columns
    from customer t0
    left join contact t1 on t1.customer_id = t0.id
    where lower(t0.name) like ? escape'|' order by t0.id
  ```
* The contacts path is a OneToMany so we get a left join to the contact table. We only fetch the id and email properties for the contacts.

### Select + Fetch = query tuning



