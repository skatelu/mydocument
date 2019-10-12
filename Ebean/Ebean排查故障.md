# Ebean 故障检修

## 相关故障
### 1、NullPointerException using Query beans
* **Example**
    ```java
        new QCustomer
        .name.istartsWith("rob")  //   <-- Getting NPE here
        .findList()
    ```
* **Diagnosis(诊断)**
  * Java：The query beans or the code using the query beans is not being enhanced（增强）.

* **Actions**
*  Check the querybean-packages entry in ebean.mf
*  For java： We need to enhance both the code that is using the query beans and the query beans themselves. The packages specified by querybean-packages needs to include the code that is using the query beans.

### RuntimeException: Is class org.example.domain.Foo registered?
* **Diagnosis**
  Ebean does not think the bean in question（疑问） is an entity bean, or we are explicitly（明确的） registering（注册） entity beans (via ServerConfig.addClass()) and have not added the entity bean to that.（Ebena 认为我们使用的实体类 并没有加入到实体ben中）

* **Actions**
  1. Check that the bean has @Entity annotation
  2. Register the entity bean if necessary
    If the entity beans are being explicit registered（显示注册） via serverConfig.addClass() then it could be that this entity bean class needs to also be included.

### DataSource user is null?
* **Actions**
  1. Check that there is a application-test.yml (or equivalent) to specify the datasource
  2. Check that io.ebean.test : ebean-test-config is a test dependency
    
   See docs / testing for setting up ebean-test-config

### java.lang.IllegalStateException: Bean class ___ is not enhanced?
* **Actions**
  1. Check that the IDE enhancement plugin for IDEA or Eclipse is installed
  2. Check that the enhancement plugin for maven or gradle is being used
  3. 