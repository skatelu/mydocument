* MAVEN 环境配置
  * 给maven的settings.xml配置文件中的profiles标签添加
  ```java
    <profile>
      <id>jdk-1.8</id>  
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>1.8</jdk>  
      </activation>  
      <properties>  
        <maven.compiler.source>1.8</maven.compiler.source>  
        <maven.compiler.target>1.8</maven.compiler.target>  
        <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>  
      </properties>
    </profile>
  ```

* POM 文件 中会有一个父项目
  ```java
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.5.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>2.2.5.RELEASE</version>
        <relativePath>../../spring-boot-dependencies</relativePath>
    </parent>
  ```


    // 在 spring-boot-dependencies 定义了所有需要引用的jar包的版本

  ```
## 1、SpringBoot 的版本仲裁中心
  * 以后我们导入依赖默认是不需要写版本的；（没有在 spring-boot-dependencies 中声明的依赖 依然要声明版本号）

## 2、导入的依赖
  ```java
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

  ```
* spring-boot-starter-web:
  * spring-boot-starter : Spring 场景启动器；帮我们导入了web模块正常运行所依赖的组件；
  * Spring Boot 将所有的功能场景都抽取出来，做成一个个starters（启动器），只需要在项目中引入这些starter相关场景的所有依赖都会导入进来。**要什么功能就导入什么场景的启动器**

## 3、Spring-Boot  启动类在根目录的原因时因为
  ```java
      	static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
          register(registry, new PackageImport(metadata).getPackageName());
        }

        @Override
        public Set<Object> determineImports(AnnotationMetadata metadata) {
          return Collections.singleton(new PackageImport(metadata));
        }

      }
  ```
  * 程序启动时会运行上面这段代码，而这段代码中  new PackageImport(metadata).getPackageName() 就是启动类的跟目录名，程序会自动扫描该目录下所有的文件

  ```java
    @Import(AutoConfigurationImportSelector.class)
  ```
  * 这个注解 @Import 时将组件导入Spring使用的
  * AutoConfigurationImportSelector 导入哪些组件选择器
  * 将所有需要导入的组件以全类名的方式返回；这些组件就会被添加到容器中
  * 在 AutoConfigurationImportSelector 这个类中的 这个方法 将组件装配进来 （过时）
  ```java
    	@Override
      public String[] selectImports(AnnotationMetadata annotationMetadata) {
        if (!isEnabled(annotationMetadata)) {
          return NO_IMPORTS;
        }
        AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader
            .loadMetadata(this.beanClassLoader);
        AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(autoConfigurationMetadata,
            annotationMetadata);
        return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
      }
  ```

* SpringBoot 
  * 从类路径下 "META-INF/spring.factories" 中获取配置文件信息

* SpringBoot 自动配置

## 二、配置文件
  #### 1、配置文件
  * SpringBoot 使用一个全局的配置文件，配置文件名是固定的；
    * application.properties
    * application.yml

  #### 2、@ConfigurationProperties(prefix = "") 与 @value的区别
  * 默认从全局配置文件中获取值
  * 1、@ConfigurationProperties 可以批量注入配置文件中的属性  @Value 需要一个个指定
  * 2、@ConfigurationProperties 支持松散绑定   @Value 不支持松散绑定
  * 3、@ConfigurationProperties 不支持SpEL语法    @Value 支持SpEL语法
  * 4、@ConfigurationProperties 支持JSP303数据校验   @Value 不支持JSP303数据校验
  * 5、在复杂类型封装方面 如配置文件中是map，list等时 @ConfigurationProperties 支持   @Value 不支持
  * 配置文件无论是 yml 还是 peoperties 都可以获取到数据的值 

  #### 3、@PropertySource() 与 @ImportResource 的区别
  * 1. @PropertySource() 加载指定的配置文件; **注意这里的 @PropertySource()不支持yaml格式配置文件，通过查询注解源码可以发现spring不是通过空格识别内容的，故识别不出yaml内的属性**
  * 2. @ImportResource 导入Spring的配置文件，让配置文件里面的内容生效; 
  ```java
    @ImportResource(locations = {"classpath:beans.xml"})
    导入Spring的配置文件
  ```

* 3、SpringBoot 推荐给容器添加组件的方式：**推荐使用全配置模式**
  1. 配置类 == 配置文件
  2. 使用@Bean给容器中添加组件

#### 4、配置文件中的占位符

* 1、**随机数**

```java
myConfig:
  myObject:
    myName: 张三${random.int}
    myAge: 29${java.version.date}
```

#### 5、Profile

* 是用来给SpringBoot做多环境配置使用的

* 1、多Profile文件

  * 我们在主配置文件编写的时候，文件名可以是 application-{profile}.properties/yml
  * 默认使用application.yml配置文件

* 2、yml支持多文档块方式

  ```json
    profiles:
      active: dev
  
  # 设置访问端口
  server:
    port: 8081
  
  #  自定义配置 用Value注解进行获取
  myConfig:
    myObject:
      myName: 张三${random.int}
      myAge: 29${java.version.date}
  # 三个减号  表示切分文档
  --- 
  # 设置访问端口
  server:
    port: 8082
  spring:
    profiles: dev
  ---
  # 设置访问端口
  server:
    port: 8083
  spring:
    profiles: prod
  ```

  

* 3、激活指定的profile

  * 在配置文件中指定

    ```json
    spring:  
      profiles:
        active: dev
    ```

  * 在命令行中指定激活

    ```jso
    --sping.profiles.active=dev
    可以直接在测试的时候，配置传入的命令行
    ```

    

#### 6、配置文件的加载位置

* SpringBoot 启动会扫描以下位置的application.properties或者application.yml文件作为SpringBoot的默认配置文件

  * file: ./config/
  * file: ./
  * classpath: /config/
  * classpath: /

* 以上是按照优先级从高到低的顺序，所有位置的文件都会被被加载，**高优先级配置**内容会**覆盖低优先级配置**内容

* 我们也可以通过配置

  * spring.config.location 来改变默认配置

* SpringBoot会从这四个位置全部加载主配置文件；

* spring.config.additional-location:**互补配置**

  

## 三、配置-自动配置原理

**在官方文档中  Common application properties** 在这个附录中标明了 application.properties/application.yml 文件中可以指定的属性

#### 1、SpringBoot启动的时候，加载主配置类，开启了自动配置功能 @EnableAutoConfiguration

* **@EnableAutoConfiguration作用**

  * 利用 AutoConfigurationImportSelector 给容器中导入一些组件

  * 可以用插件 getAutoConfigurationEntry（） 方法的内容

    ```json
    // 获取候选配置
    List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
    
    // 扫描所有jar包类路径下的 "META-INF/spring.factories" 目录
    SpringFactoriesLoader.loadFactoryNames()
    把扫描到的这些文件中的内容包装成  properties 对象
    从 properties 中获取到 EnableAutoConfiguration.class 类（类名）对应值，然后把他们添加在容器中
    
    ```

    **将 类路径下 META-INF/spring.factories 里面配置的所有 EnableAutoConfiguration 的值加入到了容器中 **

    ```properties
    org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
    org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
    org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
    org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
    org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
    org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,\
    org.springframework.boot.autoconfigure.cloud.CloudServiceConnectorsAutoConfiguration,\
    org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration,\
    org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration,\
    org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration,\
    org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration,\
    org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.couchbase.CouchbaseRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveRestClientAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.ldap.LdapRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.solr.SolrRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration,\
    org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration,\
    org.springframework.boot.autoconfigure.elasticsearch.jest.JestAutoConfiguration,\
    org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration,\
    org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,\
    org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration,\
    org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration,\
    org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration,\
    org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration,\
    org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration,\
    org.springframework.boot.autoconfigure.hazelcast.HazelcastJpaDependencyAutoConfiguration,\
    org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration,\
    org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration,\
    org.springframework.boot.autoconfigure.influx.InfluxDbAutoConfiguration,\
    org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration,\
    org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration,\
    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration,\
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
    org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,\
    org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration,\
    org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration,\
    org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,\
    org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration,\
    org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration,\
    org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration,\
    org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration,\
    org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration,\
    org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration,\
    org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration,\
    org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration,\
    org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration,\
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,\
    org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration,\
    org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration,\
    org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration,\
    org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration,\
    org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration,\
    org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration,\
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,\
    org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration,\
    org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration,\
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,\
    org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration,\
    org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration,\
    org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration,\
    org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration,\
    org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration,\
    org.springframework.boot.autoconfigure.sendgrid.SendGridAutoConfiguration,\
    org.springframework.boot.autoconfigure.session.SessionAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,\
    org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration,\
    org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration,\
    org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration,\
    org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration,\
    org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration,\
    org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration,\
    org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration,\
    org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration,\
    org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration,\
    org.springframework.boot.autoconfigure.websocket.reactive.WebSocketReactiveAutoConfiguration,\
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration,\
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketMessagingAutoConfiguration,\
    org.springframework.boot.autoconfigure.webservices.WebServicesAutoConfiguration,\
    org.springframework.boot.autoconfigure.webservices.client.WebServiceTemplateAutoConfiguration
    ```

    * 每一个这样的 xxxAutoConfiguration类都是容器中的一个组件，都加入到容器中；用他们来做自动配置

* 每一个自动配置类进行自动配置功能

#### 2、以HttpEncodingAutoConfiguration 为例解释自动配置原理

* // 根据当前不同的条件判断，决定这个配置类是否生效

* ```java
  // 表示这是一个配置类，以前编写的配置文件一样，也可以给容器中添加组件 指定是一个配置列，关了proxyBeanMethods，其它配置类就不能调相应的@bean类
  @Configuration(proxyBeanMethods = false)
  // 启动 指定类的ConfigurationProperties 功能；将配置文件中对应的值和 HttpProperties 绑定起来
  @EnableConfigurationProperties(HttpProperties.class)
  // spring 底层 @Conditional注解，根据不同的条件，如果满足指定的条件，整个配置类里面的配置就会生效   判断当前应用是否是web应用  type = ConditionalOnWebApplication.Type.SERVLET 表示当前web应用是servlet web时才会生效
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET) 
  // 判断当前项目有没有 CharacterEncodingFilter.class：SpringMVC 中进行乱码解决的过滤器 这个类
  @ConditionalOnClass(CharacterEncodingFilter.class)
  // 判断配置文件中是否存在某个配置 spring.http.encoding.enabled； matchIfMissing = true 如果不存在，判断也是成立的 
  @ConditionalOnProperty(prefix = "spring.http.encoding", value = "enabled", matchIfMissing = true)
  public class HttpEncodingAutoConfiguration {
  
      // 我们能在配置文件中配置的属性 都源自properties文件中的属性
  	private final HttpProperties.Encoding properties;
  
  	public HttpEncodingAutoConfiguration(HttpProperties properties) {
  		this.properties = properties.getEncoding();
  	}
  
  	@Bean // 给容器中添加一个组件，这个组件的某些值，需要从properties中获取
  	@ConditionalOnMissingBean // 表示如果容器中没有配置过这个组件才会生效
  	public CharacterEncodingFilter characterEncodingFilter() {
  		CharacterEncodingFilter filter = new OrderedCharacterEncodingFilter();
  		filter.setEncoding(this.properties.getCharset().name());
  		filter.setForceRequestEncoding(this.properties.shouldForce(Type.REQUEST));
  		filter.setForceResponseEncoding(this.properties.shouldForce(Type.RESPONSE));
  		return filter;
  	}
  
  	@Bean
  	public LocaleCharsetMappingsCustomizer localeCharsetMappingsCustomizer() {
  		return new LocaleCharsetMappingsCustomizer(this.properties);
  	}
  ```

  * 一旦这个配置类生效；这个配置类就会给容器中添加各种组件；这些组件的属性是从对应的properties类中获取的，这些类里面的每一个属性又是和配置文件绑定的

  

  * 所有在配置文件中能配置的属性 都是在  xxxProperties 类中封装着；配置文件能配置什么就可以参照某个功能对应的这个属性类

  ```java
  @ConfigurationProperties(prefix = "spring.http") // 从配置文件中获取指定的值和bean的属性进行绑定
  public class HttpProperties {
  
     /**
      * Whether logging of (potentially sensitive) request details at DEBUG and TRACE level
      * is allowed.
      */
     private boolean logRequestDetails;
  
     /**
      * HTTP encoding properties.
      */
     private final Encoding encoding = new Encoding();
  ```

* **精髓-约定大于配置：按照约定好的方式去写配置文件**

  * ​	**springBoot启动会加载大量的自动配置类**
  * **我们看我们需要的功能有没有SpringBoot默认写好的自动配置类**
  * **我们再来看这个自动配置类中到底配置了那些组件；（只要我们要用的组件有，我们就不需要再来配置了）**
  * **给容器中自动配置类添加组件的时候，会从properties类中获取某些属性。我们就可以再配置文件中指定这些属性的值；**

* xxxAutoConfiguration: 自动配置类 
  1. 判断条件通过，给容器中添加组件
  2. xxxProperties: 封装配置文件中相关属性

* @Conditional派生类

  作用：必须是@Conditional指定的条件成立，才给容器中添加组件，配置配里面的所有内容才生效；		

  | @Conditional扩展注解            | 作用（判断是否满足当前指定条件）                 |
  | ------------------------------- | ------------------------------------------------ |
  | @ConditionalOnJava              | 系统的java版本是否符合要求                       |
  | @ConditionalOnBean              | 容器中存在指定Bean；                             |
  | @ConditionalOnMissingBean       | 容器中不存在指定Bean；                           |
  | @ConditionalOnExpression        | 满足SpEL表达式指定                               |
  | @ConditionalOnClass             | 系统中有指定的类                                 |
  | @ConditionalOnMissingClass      | 系统中没有指定的类                               |
  | @ConditionalOnSingleCandidate   | 容器中只有一个指定的Bean，或者这个Bean是首选Bean |
  | @ConditionalOnProperty          | 系统中指定的属性是否有指定的值                   |
  | @ConditionalOnResource          | 类路径下是否存在指定资源文件                     |
  | @ConditionalOnWebApplication    | 当前是web环境                                    |
  | @ConditionalOnNotWebApplication | 当前不是web环境                                  |
  | @ConditionalOnJndi              | JNDI存在指定项                                   |

  **自动配置类必须在一定的条件下才能生效。**

  

* 我们怎么知道那些自动配置类生效：

  * 我们可以通过启动 debug=true（再配置文件中添加此配置）；来让控制台打印自动配置报告，这样我们就可以很方便的知道哪些配置类生效

    ```java
    ============================
    CONDITIONS EVALUATION REPORT
    ============================
    
    
    Positive matches:(自动配置类启用的)
    -----------------
    
       AopAutoConfiguration matched:
          - @ConditionalOnProperty (spring.aop.auto=true) matched (OnPropertyCondition)
    
       AopAutoConfiguration.ClassProxyingConfiguration matched:
          - @ConditionalOnMissingClass did not find unwanted class 'org.aspectj.weaver.Advice' (OnClassCondition)
          - @ConditionalOnProperty (spring.aop.proxy-target-class=true) matched (OnPropertyCondition)
              
    Negative matches:（没有启动，没有匹配成功的自动配置类）
    -----------------
    
       ActiveMQAutoConfiguration:
          Did not match:
             - @ConditionalOnClass did not find required class 'javax.jms.ConnectionFactory' (OnClassCondition)
    
       AopAutoConfiguration.AspectJAutoProxyingConfiguration:
          Did not match:
             - @ConditionalOnClass did not find required class 'org.aspectj.weaver.Advice' (OnClassCondition)
    ```

    

## 四、SpringBoot 与 日志

#### 1、日志框架

* SpringBoot : 底层是Spring框架，Spring 框架默认是用 JCL；
  * SpringBoot选用 SLF4j 和 logback

#### 2、SLF4j 使用

* 如何在系统中使用SLF4j

  * 以后开发的时候，日志记录方法的调用，不应该来直接调用日志的实现类，而是调用日志抽象层里面的方法；

  * 给系统中导入slf4j的jar 和 logback的实现jar

    ```java
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    public class HelloWorld {
      public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(HelloWorld.class);
        logger.info("Hello World");
      }
    }
    ```

    ![SLF4j实现](D:\IDEAStudyWorkSpace\mydocument\springboot\img\SLF4j实现.jpg)

  * 每一个日志的实现框架都有自己的配置文件。使用slf4j以后，**配置文件还是做成日志实现框架的自己本身的配置文件**





中华人民共和国二级建造师**职业**资格证书   11100000000013338W001  国办那边，这个证照名称 貌似错了  应该是 中华人民共和国二级建造师**执业**资格证书 吧   在推送这个证照数据的时候  质检全部失败，麻烦您能联系国办那边修改一下么？

#### 3、遗留问题

a (slf4j+logback):Spring(commons-logging)、Hibernate(jboss-logging)、MyBatis、XXX

统一日志记录，即使 

![](D:\IDEAStudyWorkSpace\mydocument\springboot\img\其他日志统一使用slf4j.png)

* **如何让系统中所有的日志都统一到slf4j:**
  1. 将系统中其他日志框架先排除出去
  2. 用中间包来替换原有的日志框架
  3. 我们导入slf4j其他的实现

* 如果我们要引入其他框架，一定要把这个框架的默认日志依赖移除掉
  * Spring框架用的是commons-logging;
* **SpringBoot 能自动适配所有的日志，而且底层使用slf4j+logBack的方式记录日志，引入其他框架的时候，只需要把这个框架依赖的日志框架排除掉**

#### 4、日志使用

* **1、默认配置**

  * SpringBoot默认帮我们配置好了日志；

* 2、SpringBoot 官方文档 详细的介绍了 logger  包括如何做异步的日志记录

* 3、**指定配置**

  * 给类路径下放上每个日志框架自己的配置文件即可；SpringBoot就不适用他默认配置的了

  * logback.xml ：直接就被日志框架识别了

  * **logback-spring.xml**：日志框架就不直接加载日志的配置项，由SpringBoot解析日志配置，可以使用SpringBoot的高级**Profile**功能

    ```xml
    <spingProfile name="dev">
        <pattern></pattern>
    </spingProfile>
    ```

  * 否则就会报错

## 五、Spring-WEB开发

#### 1、SpringBoot对静态资源的映射规则

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
   if (!this.resourceProperties.isAddMappings()) {
      logger.debug("Default resource handling disabled");
      return;
   }
   Duration cachePeriod = this.resourceProperties.getCache().getPeriod();
   CacheControl cacheControl = this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
   if (!registry.hasMappingForPattern("/webjars/**")) {
      customizeResourceHandlerRegistration(registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
            .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));
   }
   String staticPathPattern = this.mvcProperties.getStaticPathPattern();
   if (!registry.hasMappingForPattern(staticPathPattern)) {
      customizeResourceHandlerRegistration(registry.addResourceHandler(staticPathPattern)
            .addResourceLocations(getResourceLocations(this.resourceProperties.getStaticLocations()))
            .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));
   }
}
```

* 所有的 /webjars/** 都去 classpath:/META-INF/resources/webjars/
* 

#### 2、SpringMVC 自动配置

相关的Url地址

SpringBoot 自动配置好了MVC

以下是SpringBoot对SpringMVC的默认

##### Spring MVC Auto-configuration

Spring Boot provides auto-configuration for Spring MVC that works well with most applications.

The auto-configuration adds the following features on top of Spring’s defaults:

- Inclusion of `ContentNegotiatingViewResolver` and `BeanNameViewResolver` beans.

  * 自动配置了ViewResolver（视图解析器：根据方法的返回值得到视图对象（View），视图对象决定如何渲染（转发？重定向？））

  * ContentNegotiatingViewResolver：组合所有的视图解析器的；

  * 如何定制：我们可以自己给容器中添加一个视图解析器；自动的将其组合起来例子：

  * ```java
    // 自定义视图解析器
    // 这个方法可以放到其他配置文件中，返回时加上@Bean
    public ViewResolver myViewReolver(){
        return new MyViewResolver();
    }
    
    public static class MyViewResolver implements ViewResolver{
    
        @Override
        public View resolveViewName(String viewName, Locale locale) throws Exception {
            return null;
        }
    }
    ```

    

- Support for serving static resources, including support for WebJars (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-static-content))).

  - 静态资源文件夹路径，webjars

- Automatic registration（自动注册了） of `Converter`, `GenericConverter`, and `Formatter` beans.

  - Converter：转换器；将页面传来的数据进行类型转换

  - `Formatter` ：格式化数据；将字符串转换成时间

    ```java
    // 目前再 2.2.5版本中 会自动注册 日期格式化器
    @Bean
    @Override
    public FormattingConversionService mvcConversionService() {
        WebConversionService conversionService = new WebConversionService(this.mvcProperties.getDateFormat());
        addFormatters(conversionService);
        return conversionService;
    }
    ```

    **自己添加的格式化转换器，我们只需要放在容器中即可**

- Support for `HttpMessageConverters` (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-message-converters)).

  - HttpMessageConverters：SpringMVC 用来转换Http请求和响应的；User---Json  将User类以Json的形式传出去

    **自己给容器中添加HttpMessageConverter**，只需要将自己的组件注册到容器中（@Bean）

- Automatic registration of `MessageCodesResolver` (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-spring-message-codes)).

  - 定义错误代码生成规则

- Static `index.html` support. 

  - 静态首页访问使用

- Custom `Favicon` support (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-favicon)).

- Automatic use of a `ConfigurableWebBindingInitializer` bean (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-web-binding-initializer)).

**org.springframework.boot.autoconfigure.web**：**web的所有自动场景**

If you want to keep those Spring Boot MVC customizations and make more [MVC customizations](https://docs.spring.io/spring/docs/5.2.3.RELEASE/spring-framework-reference/web.html#mvc) (interceptors, formatters, view controllers, and other features), you can add your own `@Configuration` class of type `WebMvcConfigurer` but **without** `@EnableWebMvc`.

If you want to provide custom instances of `RequestMappingHandlerMapping`, `RequestMappingHandlerAdapter`, or `ExceptionHandlerExceptionResolver`, and still keep the Spring Boot MVC customizations, you can declare a bean of type `WebMvcRegistrations` and use it to provide custom instances of those components.

If you want to take complete control of Spring MVC, you can add your own `@Configuration` annotated with `@EnableWebMvc`, or alternatively add your own `@Configuration`-annotated `DelegatingWebMvcConfiguration` as described in the Javadoc of `@EnableWebMvc`.



#### 3、扩展SpringMVC(interceptors, formatters, view controllers, and other features)

* **编写一个配置类（@Configuration）,这个类必须是`WebMvcConfigurer` 类型；不能标注 `@EnableWebMvc`**

* 既保留了所有的自动配置，也能用我们扩展配置

  ```java
  /**
   * 扩展SpringMVC
   * 使用 WebMvcConfigurer 可以来扩展SpringMVC的功能
   */
  @Configuration
  public class MyMvcConfig implements WebMvcConfigurer {
  
      @Override
      public void addViewControllers(ViewControllerRegistry registry) {
          // 浏览器发送 /yangjunlu 请求来到 success
          registry.addViewController("/yangjunlu").setViewName("success");
      }
  }
  ```

* **扩展配置实现原理**

  * 1、WebMvcAutoConfiguration是SpringMVC的自动配置类，其中有 WebMvcAutoConfigurationAdapter 这个类也是实现 WebMvcConfigurer 接口
  * 2、在做其他自动配置时会导入；@Import(EnableWebMvcConfiguration.class)

  ```java
  	@Configuration(proxyBeanMethods = false)
  	@Import(EnableWebMvcConfiguration.class)
  	@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
  	@Order(0)
  	public static class WebMvcAutoConfigurationAdapter implements WebMvcConfigurer {
  ```

  * 3 容器中所有的WebMvcConfigurer 都会一起起作用

  ```java
  	/**
  	 * Configuration equivalent to {@code @EnableWebMvc}.
  	 */
  	@Configuration(proxyBeanMethods = false)
  	public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration implements ResourceLoaderAware {
  
          
  @Configuration(proxyBeanMethods = false)
  public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
  
  	private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();
  
  	// 从容器中获取所有的 WebMvcConfigurer
  	@Autowired(required = false)
  	public void setConfigurers(List<WebMvcConfigurer> configurers) {
  		if (!CollectionUtils.isEmpty(configurers)) {
  			this.configurers.addWebMvcConfigurers(configurers);
  		}
  	}
      
      
  	//一个参考实现 将所有的WebMvcConfigurer相关配置都来一起调用
  	public void addWebMvcConfigurers(List<WebMvcConfigurer> configurers) {
  		if (!CollectionUtils.isEmpty(configurers)) {
  			this.delegates.addAll(configurers);
  		}
  	}
  
  ```

  * 4、我们的配置类也会被调用；

  效果：SpringMVC的自动配置和我们的扩展配置都会起作用；



#### 4、全面接管SpringMVC

**SpringBoot对SpringMVC的自动配置不需要了，所有都是我们自己配；所有的SpringMVC的自动配置都失效了**

**我们需要在配置类中添加 @EnableWebMvc即可：**

```java
/**
 * 扩展SpringMVC
 * 使用 WebMvcConfigurer 可以来扩展SpringMVC的功能
 * @EnableWebMvc 加上后将全面接管 SpringMVC
 */
@EnableWebMvc
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 浏览器发送 /yangjunlu 请求来到 success
        registry.addViewController("/yangjunlu").setViewName("success");
    }
}

```

* **原理**（加上 @EnableWebMvc  为什么自动配置就失效了）

  * 1、主要是引入了一个 DelegatingWebMvcConfiguration 这个类

  ```java
  // 主要是引入了一个 DelegatingWebMvcConfiguration 这个类
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Documented
  @Import(DelegatingWebMvcConfiguration.class)
  public @interface EnableWebMvc {
  }
  
  ```

  * 这个类会**继承 WebMvcConfigurationSupport 这个类** 并将这个 Bean 装配到springBoot容器、组件中

  ```java
  @Configuration(proxyBeanMethods = false)
  public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
  
  	private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();
  ```

  * 3、WebMvcAutoConfiguration 在 **@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)**这个条件成立时才会自动装配

  ```java
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication(type = Type.SERVLET)
  @ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
  // 容器中没有这个类时才会自动装配 WebMvcAutoConfiguration 这个类，这个自动配置类才会生效
  @ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
  @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
  @AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
  		ValidationAutoConfiguration.class })
  public class WebMvcAutoConfiguration {
  ```

  * 4、@EnableWebMvc 将 WebMvcConfigurationSupport  这个子健导入进来了
  * 5、导入的 WebMvcConfigurationSupport 知识 SpringMVC最基本的功能





#### 5、如何修改SpringBoot的默认配置

模式：

* **SpringBoot在自动配置很多组件的时候，先看容器中有没有用户自己配置的（@Bean、@Component）如果有就用用户配置的，如果没有，才自动配置；如果有些组件可以有多个（ViewResolver）将用户配置的和自己默认的组合起来**


#### 6、RestfulCRUD

* 6.1 **国际化** **配置相关的配置文件，前后端分离的话，可以不在后台设置**

  **原理：**

  * 国际化Local(区域信息对象)；LocaleResolver（获取区域信息对象）：  根据请求头head 中的排列顺序 是 zh_CN 还是 en_US 来判断是英文还是中文
  * 



#### 7、拦截器的创建

* 首先需要创建一个拦截器类，需要实现 HandlerInterceptor 接口 

  ```java
  /**
   * 进行登录检查,创建完成之后，需要在 扩展SpringMVC 的实现 WebMvcConfigurer 中 添加到mvc中
   * addInterceptors 方法中加入进去  让mvc进行管理，控制
   */
  public class LoginHandlerInterceptor implements HandlerInterceptor {
      /**
       * 目表方法执行之前
       * @param request
       * @param response
       * @param handler
       * @return
       * @throws Exception
       */
      @Override
      public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
          Object loginUser = request.getSession().getAttribute("loginUser");
          if (ObjectUtils.isEmpty(loginUser)) {
              // 未登录，返回 登录页面
              return false;
          }
          return true;
      }
  
      @Override
      public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
  
      }
  
      @Override
      public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
  
      }
  }
  ```

* 第二步 需要在 SprngMVC 的扩展配置类中加入 相关的配置  需要重写 addInterceptors 这个方法

  ```java
  /**
   * 扩展SpringMVC
   * 使用 WebMvcConfigurer 可以来扩展SpringMVC的功能
   * @EnableWebMvc 加上后将全面接管 SpringMVC
   */
  //@EnableWebMvc
  @Configuration
  public class MyMvcConfig implements WebMvcConfigurer {
  
      @Override
      public void addViewControllers(ViewControllerRegistry registry) {
          // 浏览器发送 /yangjunlu 请求来到 success
          registry.addViewController("/yangjunlu").setViewName("success");
      }
  
      /**
       * 注册拦截器
       * @param registry
       */
      @Override
      public void addInterceptors(InterceptorRegistry registry) {
          // 用 InterceptorRegistry 将拦截器的实现添加到 SpringMVC 中  addPathPatterns 这个用来添加需要拦截哪些请求，支持通配符
          registry.addInterceptor(new LoginHandlerInterceptor()).addPathPatterns("/**")
                  .excludePathPatterns("/index.html","/","/user/login"); // excludePathPatterns 表示需要排除哪些请求，即哪些请求不需要进行拦截
  
      }
  }
  ```

  

## 六、错误处理机制

### 1、SpringBoot默认的错误处理机制

* 默认效果：

  * 返回一个默认的错误页面  或者客户端接收的json数据

    ```json
    {
        "timestamp": "2020-04-14T01:33:03.475+0000",
        "status": 404,
        "error": "Not Found",
        "message": "No message available",
        "path": "/"
    }
    ```

  * 这块返回这样的原因是在 SpringBoot 的自动配置中进行的配置

  * **原理：可以参照 ErrorMvcAutoConfiguration ；错误处理的自动配置**

    给容器中添加了以下组件：

    1、DefaultErrorAttributes

    2、BasicErrorController

    ```java
    // 处理3中的 /error 请求
    	public static final String TEXT_HTML_VALUE = "text/html";
    
    	/**
    	* 产生 html 页面 返回页面错误
    	* 浏览器发送请求的请求头中  在 Request Headers 中 会有 Accept：text/html 这样的数据
    	*/
    	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
    		HttpStatus status = getStatus(request);
    		Map<String, Object> model = Collections
    				.unmodifiableMap(getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
    		response.setStatus(status.value());
            // 这个方法  去哪个页面作为错误页面，包含页面地址与页面内容
    		ModelAndView modelAndView = resolveErrorView(request, response, status, model);
    		return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
    	}
    
    	/**
    	* 返回客户端需要的json数据
    	* 客户端中的请求 Request Headers 中 会有 Accept："*/*" 这样的数据
        * 没有指定 要使用哪种形式的处理
    	*/
    	@RequestMapping
    	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    		HttpStatus status = getStatus(request);
    		if (status == HttpStatus.NO_CONTENT) {
    			return new ResponseEntity<>(status);
    		}
    		Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
    		return new ResponseEntity<>(body, status);
    	}
    
    	// 遍历所有的 ErrorViewResolver 就可以得到 modelAndView
    	protected ModelAndView resolveErrorView(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
    			Map<String, Object> model) {
    		for (ErrorViewResolver resolver : this.errorViewResolvers) {
    			ModelAndView modelAndView = resolver.resolveErrorView(request, status, model);
    			if (modelAndView != null) {
    				return modelAndView;
    			}
    		}
    		return null;
    	}
    ```

    

    3、ErrorPageCustomizer

    ```java
    	/**
    	 * Path of the error controller.
    	 */
    	@Value("${error.path:/error}")
    	private String path = "/error";  // 系统出现错误后来到 error 请求，进行处理（类似 web.xml中注册的错错误页面规则）
    		// 此处是核心处理代码
    		public void registerErrorPages(ErrorPageRegistry errorPageRegistry) {
    			ErrorPage errorPage = new ErrorPage(
    					this.dispatcherServletPath.getRelativePath(this.properties.getError().getPath()));
    			errorPageRegistry.addErrorPages(errorPage);
    		}
    
    ```

    

    4、DefaultErrorViewResolver

    ```java
    	@Override
    	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
    		ModelAndView modelAndView = resolve(String.valueOf(status.value()), model);
    		if (modelAndView == null && SERIES_VIEWS.containsKey(status.series())) {
    			modelAndView = resolve(SERIES_VIEWS.get(status.series()), model);
    		}
    		return modelAndView;
    	}
    
    
    	private ModelAndView resolve(String viewName, Map<String, Object> model) {
            // 默认SpringBoot 可以去找到一个页面  error/404 页面
    		String errorViewName = "error/" + viewName;
            // 模板引擎可以解析这个页面地址就用模板引擎解析
    		TemplateAvailabilityProvider provider = this.templateAvailabilityProviders.getProvider(errorViewName,
    				this.applicationContext);
    		if (provider != null) {
                // 模板引擎可用的情况下返回到 errorViewName 指定的视图地址
    			return new ModelAndView(errorViewName, model);
    		}
            // 模板引擎不可用，就在静态资源文件夹下找 errorViewName 对应的页面  error/404.html
    		return resolveResource(errorViewName, model);
    	}
    ```

    

  * 步骤：

    * 一但系统出现 4xx 或者 5xx 之类的错误；ErrorPageCustomizer 就会生效（定制错误的响应规则）；就会来到 /error 请求
    * 响应页面；去哪个页面是由 

* **如何定知错误响应**

  * 1、如何定制错误页面
    * 有模板引擎的情况下，在静态资源文件夹下创建  error/状态码 如：error/404、error/50x 的html页面 放在模板引擎的 error 文件夹下
    * 

### 2、如何定制错误的json数据

#### **自定义异常处理&返回定制的 JSON 数据:**

* 1、自定义异常

  ```java
  public class UserNotExistException extends RuntimeException {
      public UserNotExistException() {
          super("用户不存在！");
      }
  }
  ```

  #### 2.1 不是自适应效果的异常处理（简单、易实现）

​	**即浏览器与客户端返回的都是JSON 数据，而不是浏览器请求返回页面，客户端请求返回JSON数据**

* ```java
  /**
   *  @ControllerAdvice  这是一个增强的Controller
   *  1、全局异常处理
   *  2、全局数据绑定
   *  3、全局数据预处理
   */
  @ControllerAdvice
  public class MyExceptionHandler {
  
      /**
       * @return
       * @ExceptionHandler 这个注解  表示要处理什么样的异常
       * UserNotExistException.class  表示处理用户不存在异常
       * 如果为 Exception.class 表示处理所有异常
       */
      @ResponseBody
      @ExceptionHandler(UserNotExistException.class)
      public String handleException(Exception e) {
          Map<String, Object> map = new HashMap<>();
          map.put("code", "user.notexist");
          map.put("message", e.getMessage());
          return JSON.toJSONString(map);
      }
  }
  ```

  #### 2.2 自适应效果的异常处理（简单、易实现）

* **浏览器请求返回页面，客户端请求返回JSON数据**

  ```java
      /**
       * 实现自适应效果的错误处理
       * @param e
       * @return
       */
      @ExceptionHandler(UserNotExistException.class)
      public String handleException(Exception e) {
          Map<String, Object> map = new HashMap<>();
          map.put("code", "user.notexist");
          map.put("message", e.getMessage());
          // 转发到 /Error 请求
          return "forward:/error";
      }
  ```

  * **存在问题  未能将自己定制的信息传输到返回信息中**

  #### 2.3 自适应效果的异常处理

* **将定制的错误信息出送过去**

  出现错误以后，会来到/error请求，会被BasicErrorController处理，响应出去的数据是由getErrorAttributes() 这个方法得到的； 添加 BasicErrorController 的前提是 **@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)** 容器中没有ErrorController 时，SpringMVC才会进行加载，而 ErrorController 就是 AbstractErrorController （public abstract class AbstractErrorController implements ErrorController）；

* 完全编写一个 ErrorController 实现类（或者是编写 AbstractErrorController  规定的方法 ，放在容器中

* 页面上能用的数据，或者是json返回能用的数据都是通过  **this.errorAttributes.getErrorAttributes**  这个errorAttributes 

  而容器中的 errorAttributes 的创建  在 ErrorMvcAutoConfiguration 有这个方法

  ```java
  @Bean
  @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
  public DefaultErrorAttributes errorAttributes() {
      return new DefaultErrorAttributes(this.serverProperties.getError().isIncludeException());
  }
  ```

  **说明 DefaultErrorAttributes 默认进行数据处理的,可以用下面的代码 自定义ErrorAttributes**

  ```java
  //给容器中加入自己定义的容器属性
  @Component
  public class MyErrorAttributes extends DefaultErrorAttributes {
  
      @Override
      public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
          Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
          Map<String,Object> ext = (Map<String, Object>) webRequest.getAttribute("1", 0);
          errorAttributes.put("ext", ext);
          return errorAttributes;
      }
  }
  ```

  

* 3、实际应用场景

  ```java
      // 测试Controller
      @RequestMapping("/hello")
      public String hello(@RequestParam("user") String user) {
          if (user.equals("aaa")) {
              throw new UserNotExistException();
          }
          return "Hello world";
      }
  ```

* 4、返回结果：

  ```json
  {
      "timestamp": "2020-04-27T16:30:50.272+0000",
      "status": 500,
      "error": "Internal Server Error",
      "message": "用户不存在！",
      "path": "/api/hello",
      "ext": {
          "code": "user.notexist",
          "message": "用户出错了========="
      }
  }
  ```




## 七、配置嵌入式Servlet容器

* **SpringBoot 默认使用的是嵌入式的Servlet容器（Tomcat）；**

  ![](E:\IDEAStudyWorkSpace\mydocument\springboot\img\SpringBootTomcat.jpg)

* **如何定制和修改servlet容器的相关配置：**

  * **1、修改和server有关的配置(ServerProperties类中)**

    ```properties
    server:
      port: 8081
      tomcat:
        uri-encoding: UTF-8
      
    // 通用的Servlet容器设置
    server.xxx
    // Tomcat的设置
    server.tomcat.xxx
      
    ```

  * 2、编写一个  WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> 嵌入式的Servlet容器的定制器；来修改Servlet容器的配置

    ```java
        /**
         * 自定义配置
         * 定制和修改servlet容器的相关配置
         *
         * @return
         */
        @Bean
        public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> myWebServerFactoryCustomizer() {
    
            return new WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>() {
                @Override
                public void customize(ConfigurableServletWebServerFactory factory) {
                    factory.setPort(8085);
                }
            };
        }
    ```

* **SpringBoot 注册servlet、filter、Listener 三大组件（拦截器与过滤器的区别）**

  * 由于

* **SpringBoot支持其他的Servlet容器：**

  * jetty（适合长连接）

    ```xml
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
                <exclusions>
                    <exclusion>
                        <artifactId>spring-boot-starter-tomcat</artifactId>
                        <groupId>org.springframework.boot</groupId>
                    </exclusion>
                </exclusions>
            </dependency>
    
    <!--        引入其他的servlet容器-->
            <dependency>
                <artifactId>spring-boot-starter-jetty</artifactId>
                <groupId>org.springframework.boot</groupId>
            </dependency>
    ```

    

  * Undertow(不支持JSP 并发性能好)

  * tomcat（默认使用）

####  嵌入式Servlet容器自动配置原理

