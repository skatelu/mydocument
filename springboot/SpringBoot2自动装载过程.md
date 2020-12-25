# SpringBoot2 自动装配组件原理

## 一、自动装配开始

* 每个**SpringBoot** 应用程序在 **main**方法，开始的时候，都会有一个 **@SpringBootApplication** 注解，自动装备就是从这个注解开始的

* ```java
  @SpringBootApplication
  @MapperScan("testhttp.demo.sqlitetest.mapper")
  public class DemoApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(DemoApplication.class, args);
  
          /**
           * 可以设置 Banner 是否打印 Banner就是打印时候的 SpringBoot
           */
  //        SpringApplication springApplication = new SpringApplication(DemoApplication.class);
  //        springApplication.setBannerMode(Banner.Mode.OFF);
  //        springApplication.run(args);
      }
  
      // 自定义视图解析器
  /*    public ViewResolver myViewReolver(){
          return new MyViewResolver();
      }
  
      public static class MyViewResolver implements ViewResolver{
  
          @Override
          public View resolveViewName(String viewName, Locale locale) throws Exception {
              return null;
          }
      }*/
  }
  
  ```



* **在该注解中，有其它四个注解**

* ```java
  @Target(ElementType.TYPE) // @Target用来表示注解作用范围，超过这个作用范围，编译的时候就会报错。
  @Retention(RetentionPolicy.RUNTIME) // @Retention定义了该Annotation被保留的时间长短
  @Documented // Documented注解表明这个注释是由 javadoc记录的，在默认情况下也有类似的记录工具。 如果一个类型声明被注释了文档化，它的注释成为公共API的一部分
  @Inherited // 通过上述描述可知，使用该注解的注解父类的子类可以继承父类的注解。
  @SpringBootConfiguration // @SpringBootConfiguration继承自@Configuration，二者功能也一致，标注当前类是配置类
  @EnableAutoConfiguration // 这个就是实现自动装配的核心注解，是用来激活自动装配的，其中默认路径扫描以及组件装配、排除等都通过它来实现。
  // 这是用来扫描被 @Component标注的类 ，只不过这里是用来过滤 Bean 的，指定哪些类不进行扫描，而且用的是自定义规则
  @ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
  		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) }) 
  ```



## 二、@EnableAutoConfiguration 实现（如何进行自动装配的）

* **@EnableAutoConfiguration 是实现自动装配的核心注解，是用来激活自动装配的，看注解前缀应该是Spring @Enable 模块驱动的设计模式,所以它必然会有 `@Import` 导入的被 `@Configuration` 标注的类或实现 `ImportSelector` 或 `ImportBeanDefinitionRegistrar` 接口的类。**

* ```java
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Inherited
  // @AutoConfigurationPackage 这是用来将启动类所在的包，以及下面所有子包里面的所有组件扫描到Spring容器中，这里的组件是指被 @Component 或其派生注解标注的类。这也就是为什么不用标注 @ComponentScan 的原因
  @AutoConfigurationPackage 
  // 这里导入的是实现了 ImportSelector 接口的类，组件自动装配的逻辑均在重写的 selectImports 方法中实现
  @Import(AutoConfigurationImportSelector.class)
  public @interface EnableAutoConfiguration {
  
  	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";
  
  	/**
  	 * Exclude specific auto-configuration classes such that they will never be applied.
  	 * @return the classes to exclude
  	 */
  	Class<?>[] exclude() default {};
  
  	/**
  	 * Exclude specific auto-configuration class names such that they will never be
  	 * applied.
  	 * @return the class names to exclude
  	 * @since 1.3.0
  	 */
  	String[] excludeName() default {};
  
  }
  ```



### 2.1 获取默认包扫描路径

* **@AutoConfigurationPackage 注解获取默认包扫描路径**

* ```java
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Inherited
  @Import(AutoConfigurationPackages.Registrar.class)
  public @interface AutoConfigurationPackage {
  
  }
  ```

* **可以看到它是通过 @Import 导入了 AutoConfigurationPackages.Registrar类，该类实现了  ImportBeanDefinitionRegistrar 接口，所以他是在重写的方法中直接注册相关组件**

* ```java
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

* ```java
  	private static final class PackageImport {
    
    		private final String packageName;
    
    		PackageImport(AnnotationMetadata metadata) {
    			this.packageName = ClassUtils.getPackageName(metadata.getClassName());
    		}
    
    		@Override
    		public int hashCode() {
    			return this.packageName.hashCode();
    		}
    
    		@Override
    		public boolean equals(Object obj) {
    			if (obj == null || getClass() != obj.getClass()) {
    				return false;
    			}
    			return this.packageName.equals(((PackageImport) obj).packageName);
    		}
    
    		public String getPackageName() {
    			return this.packageName;
    		}
    
    		@Override
    		public String toString() {
    			return "Package Import " + this.packageName;
    		}
    
    	}
  ```

* **这里主要是通过 metadata 元数据信息构造 PackageImport 类，先获取启动类的类名，再通过 ClassUtils.getPackageName 获取启动类所在的包名**

* ```java
  	public static void register(BeanDefinitionRegistry registry, String... packageNames) {
    		if (registry.containsBeanDefinition(BEAN)) {
    			BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN);
    			ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();
    			constructorArguments.addIndexedArgumentValue(0, addBasePackages(constructorArguments, packageNames));
    		}
    		else {
    			GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    			beanDefinition.setBeanClass(BasePackages.class);
    			beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, packageNames);
    			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    			registry.registerBeanDefinition(BEAN, beanDefinition);
    		}
    	}
  ```

* **最后就是将这个包保存至 BasePackages 类中，然后通过 BeanDefinitionRegistry 将其注册，进行后续处理，至此该流程结束**。



### 2.2 获取自动装配的组件

* **该部分就是实现自动装配的入口 AutoConfigurationImportSelector，从上面得知这里也是通过 @Import 来实现的**

* ```java
  public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware,
  		ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered {
  
      ....
      
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
  	
  	....
  }
  ```

* **关注重写的 selectImports 方法，其中**

**AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader); 是加载自动装配的元信息**

**AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(autoConfigurationMetadata,annotationMetadata) 该方法返回的就是自动装配的组件**

```java
	protected AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata,
			AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
        // 获取 @EnableAutoConfigoration 标注类的元信息，也就是获取该注解 exclude 和 excludeName 属性值
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
        
        // 该方法就是获取自动装配的类名集合
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
        
        // 去除重复的自动装配组件，就是将List转为Set进行去重
		configurations = removeDuplicates(configurations);
        
        // 这部分就是根据上面获取的 exclude 及 excludeName 属性值，排除指定的类
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		checkExcludedClasses(configurations, exclusions);
		configurations.removeAll(exclusions);
        
        // 这里是过滤那些依赖不满足的自动装配 Class
		configurations = filter(configurations, autoConfigurationMetadata);
		fireAutoConfigurationImportEvents(configurations, exclusions);
        
        // 返回的就是经过一系列去重、排除、过滤等操作后的自动装配组件
		return new AutoConfigurationEntry(configurations, exclusions);
	}
```

* **该方法中就是先获取待自动装配组件的类名集合，然后通过一系列的去重，排除，过滤，最终返回自动装配的类名集合。主要关注 getCandidateConfigurations(annotationMetadata, attributes) 这个方法，里面是如何获取自动装配的类名集合 **

* ```java
  	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
    		List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
    				getBeanClassLoader());
    		Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
    				+ "are using a custom packaging, make sure that file is correct.");
    		return configurations;
    	}
  ```

* **其中`getSpringFactoriesLoaderFactoryClass()`返回的是`EnableAutoConfiguration.class`**

* **继续往下，执行的是 `SpringFactoriesLoader#loadFactoryNames` 方法：**

* ```java
  	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
          
          // 前面可以看到，这里的 factoryClass 是 EnableAutoConfiguration.class
    		String factoryTypeName = factoryType.getName();
    		return loadSpringFactories(classLoader).getOrDefault(factoryTypeName, Collections.emptyList());
    	}
  ```

* ```java
  	private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
    		MultiValueMap<String, String> result = cache.get(classLoader);
    		if (result != null) {
    			return result;
    		}
    
    		try {
              // 获取 META-INF/spring.factories 中的类的url地址信息
    			Enumeration<URL> urls = (classLoader != null ?
    					classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
    					ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
    			result = new LinkedMultiValueMap<>();
    			while (urls.hasMoreElements()) {
    				URL url = urls.nextElement();
    				UrlResource resource = new UrlResource(url);
    				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
    				for (Map.Entry<?, ?> entry : properties.entrySet()) {
    					String factoryTypeName = ((String) entry.getKey()).trim();
    					for (String factoryImplementationName : StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {
    						result.add(factoryTypeName, factoryImplementationName.trim());
    					}
    				}
    			}
    			cache.put(classLoader, result);
    			return result;
    		}
    		catch (IOException ex) {
    			throw new IllegalArgumentException("Unable to load factories from location [" +
    					FACTORIES_RESOURCE_LOCATION + "]", ex);
    		}
    	}
  ```

* **最终的实现逻辑都在这里，主要过程如下：**

  * **搜索classpath路径下以及所有外部jar包下的META-INF文件夹中的`spring.factories`文件。主要是`spring-boot-autoconfigur`包下的**

  * ```java
    # Initializers
    org.springframework.context.ApplicationContextInitializer=\
    org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
    org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener
    
    # Application Listeners
    org.springframework.context.ApplicationListener=\
    org.springframework.boot.autoconfigure.BackgroundPreinitializer
    
    # Auto Configuration Import Listeners
    org.springframework.boot.autoconfigure.AutoConfigurationImportListener=\
    org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener
    
    # Auto Configuration Import Filters
    org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
    org.springframework.boot.autoconfigure.condition.OnBeanCondition,\
    org.springframework.boot.autoconfigure.condition.OnClassCondition,\
    org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition
    
    # Auto Configure
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
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
    
    # Failure analyzers
    org.springframework.boot.diagnostics.FailureAnalyzer=\
    org.springframework.boot.autoconfigure.diagnostics.analyzer.NoSuchBeanDefinitionFailureAnalyzer,\
    org.springframework.boot.autoconfigure.flyway.FlywayMigrationScriptMissingFailureAnalyzer,\
    org.springframework.boot.autoconfigure.jdbc.DataSourceBeanCreationFailureAnalyzer,\
    org.springframework.boot.autoconfigure.jdbc.HikariDriverConfigurationFailureAnalyzer,\
    org.springframework.boot.autoconfigure.session.NonUniqueSessionRepositoryFailureAnalyzer
    
    # Template availability providers
    org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider=\
    org.springframework.boot.autoconfigure.freemarker.FreeMarkerTemplateAvailabilityProvider,\
    org.springframework.boot.autoconfigure.mustache.MustacheTemplateAvailabilityProvider,\
    org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAvailabilityProvider,\
    org.springframework.boot.autoconfigure.thymeleaf.ThymeleafTemplateAvailabilityProvider,\
    org.springframework.boot.autoconfigure.web.servlet.JspTemplateAvailabilityProvider
    
    ```

  * **可以看到其中内容，存储的是 key-value 格式的数据，且key是一个类的全路径名，value是多个类的全路径名称，且以逗号分割**

  * **将所有的 spring.factories 文件转成 Properties 格式，将里面key-value 格式的数据转成 Map，该Map的value是一个List，之后将相同Key的value合并到List中，将该Map作为方法返回值返回**

  * **返回到 loadFactoryNames 方法，通过上面得知 factoryClassName 的值为 EnableAutoConfiguration，所以通过  getOrDefault(factoryTypeName, Collections.emptyList())  方法，获取Key 为 EnableAutoConfiguration 的类名集合**

  > **ps：`getOrDefault`第一个入参是key的name，如果key不存在，则直接返回第二个参数值**

**至此，流程结束，最后返回的就是自动装配的组件，其中有我们比较熟悉的Redis、JDBC、SpringMVC等，可以看到一个特点，这些自动装配的组件都是以 `AutoConfiguration` 结尾。但该组件列表只是候选组件，因为后面还有去重、排除、过滤等一系列操作，这里就不再详细述说。下面我们来看看自动装配的组件内部是怎么样的。**



### 2.3 自动装配的组件内部实现

* **就拿比较熟悉的 Web MVC 来看，看看是如何实现 Web MVC 自动装配的。**

* ```java
  @Configuration(proxyBeanMethods = false) // 标识，该类是一个配置类
   // @ConditionalXXX Spring 条件装配，只不过经由 SpringBoot 扩展形成了自己的条件化自动装配，且都是 @Conditional 的派生注解
  // @ConditionalOnWebApplication 参数是 Type 类型的枚举，当前项目类型是任意、Web、Reactive其中之一则实例化该 Bean。这里指定如果为 Web 项目才满足条件
  @ConditionalOnWebApplication(type = Type.SERVLET)
  // @ConditionalOnClass 参数是 Class 数组，当给定的类名在类路径上存在，则实例化当前Bean。这里当Servlet.class、 DispatcherServlet.class、 WebMvcConfigurer.class存在才满足条件。
  @ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
  // @ConditionalOnMissingBean  参数是也是 Class 数组，当给定的类没有实例化时，则实例化当前Bean。这里指定当 WebMvcConfigurationSupport 该类没有实例化时，才满足条件
  @ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
  // @AutoConfigureOrder 参数值是int类型的数值，越小越先初始化
  @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
  // 参数是 Class 数组，在指定的配置类初始化后再加载
  @AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
  		ValidationAutoConfiguration.class })
  public class WebMvcAutoConfiguration {
      ...
      @Configuration
      @Import(EnableWebMvcConfiguration.class)
      @EnableConfigurationProperties({WebMvcProperties.class, ResourceProperties.class})
      @Order(0)
      public static class WebMvcAutoConfigurationAdapter implements WebMvcConfigurer, ResourceLoaderAware {
          ...
          @Bean
          @ConditionalOnBean(View.class)
          @ConditionalOnMissingBean
          public BeanNameViewResolver beanNameViewResolver() {
              ...
          }
          ...
      }
  
      @Configuration
      public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration {
  
          @Bean
          @Override
          public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
              ...
          }
  
          @Bean
          @Primary
          @Override
          public RequestMappingHandlerMapping requestMappingHandlerMapping() {
            ...
          }
      }
      ...
  }
  ```
  
* **代码部分：**

* **这部分就比较直接了，实例化了和 Web MVC 相关的Bean，如 `HandlerAdapter`、`HandlerMapping`、`ViewResolver`等。其中，出现了 `DelegatingWebMvcConfiguration` 类，这是上篇文章所讲的 `@EnableWebMvc` 所 `@Import`导入的配置类。**

* 

## 总结

### Spring Boot 自动装配依赖的注解驱动、@Enable 模块驱动、条件装配等特性均来自 Spring Framework。而 自动装配的配置类来源于 spring.factories 文件中。核心则是基于 "约定大于配置" 理念，通俗的说，就是Spring boot 为我们提供了一套默认的配置，只有当默认的配置不满足我们的需求时，我们再去修改默认配置。

### 缺点

* **组件的高度集成，使用的时候很难知道底层实现，加深了理解难度**