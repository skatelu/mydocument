# Spring Boot 使用AOP

## AOP简介

* **AOP可能对于广大开发者耳熟能详，它是Aspect Oriented Programming的缩写，翻译成中文就是：面向切面编程。**

## 添加依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
 
    <groupId>com.dalaoyang</groupId>
    <artifactId>springboot_aop</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>
 
    <name>springboot_aop</name>
    <description>springboot_aop</description>
 
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.4.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
 
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>
 
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
 
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
 
</project>
```



## 创建切面

### 一、直接使用切面类

* 新建一个日志切面类，假设我们需要一个类来打印进入方法或方法执行后需要打印的日志。

* **新建一个切面类**

* 新建类LogAspect，完整代码如下：**需注意，切面中首先执行的是 @Around 环绕通知，然后再执行其它注解**

* ```java
  
  import org.aspectj.lang.JoinPoint;
  import org.aspectj.lang.ProceedingJoinPoint;
  import org.aspectj.lang.annotation.After;
  import org.aspectj.lang.annotation.AfterReturning;
  import org.aspectj.lang.annotation.AfterThrowing;
  import org.aspectj.lang.annotation.Around;
  import org.aspectj.lang.annotation.Aspect;
  import org.aspectj.lang.annotation.Before;
  import org.aspectj.lang.annotation.Pointcut;
  import org.springframework.stereotype.Component;
  
  /**
   * @author yjl
   * @version $Id: LogAspect.java, v 0.1 2020-12-28 9:11 yjl Exp $$
   */
  @Aspect // 表明这是一个切面类
  @Component // 将当前类注入到Spring容器中
  public class LogAspect {
  
      // @Pointcut 切入点，其中execution用于使用切面的连接点。使用方法：execution(方法修饰符(可选) 返回类型 方法名 参数 异常模式(可选)) ，可以使用通配符匹配字符，*可以匹配任意字符。
      @Pointcut("execution(public * testhttp.demo.controller.*.*(..))")
      public void LogAspect() {
      }
  
  
      @Before("LogAspect()") // 在切面的方法执行前执行
      public void doBefore(JoinPoint joinPoint){
          System.out.println("doBefore");
      }
  
      @After("LogAspect()") // 在切面的方法执行后执行
      public void doAfter(JoinPoint joinPoint){
          System.out.println("doAfter");
      }
  
      @AfterReturning("LogAspect()") // 在切面方法执行后返回一个结果后执行
      public void doAfterReturning(JoinPoint joinPoint){
          System.out.println("doAfterReturning");
      }
  
      @AfterThrowing("LogAspect()") // 在方法执行过程中抛出异常的时候执行
      public void deAfterThrowing(JoinPoint joinPoint){
          System.out.println("deAfterThrowing");
      }
  
      /**
       *  环绕通知，就是可以在执行前后都使用这个方法的参数必须为：ProceedingJoinPoint
       *  proceed 方法就是被切面的方法，上面四个方法可以使用JoinPoint，JoinPoint包含了类名，被切面的方法名，参数等信息
       * @param joinPoint
       * @return
       * @throws Throwable
       */
      @Around("LogAspect()")
      public Object deAround(ProceedingJoinPoint joinPoint) throws Throwable{
          System.out.println("deAround");
          return joinPoint.proceed();
      }
  
  }
  ```



### 二、 利用自定义注解使用 AOP

#### 新建自定义注解

* **新建自定义注解，新建注解与新建接口类似，将interface改为@interface即可。**

* ```java
  import java.lang.annotation.ElementType;
  import java.lang.annotation.Retention;
  import java.lang.annotation.RetentionPolicy;
  import java.lang.annotation.Target;
  
  /**
   * @author yjl
   * @version $Id: DoneTime.java, v 0.1 2020-12-28 10:07 yjl Exp $$
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface DoneTime {
      String param() default "";
  }
  ```

#### 创建自定义注解对应的切面

* **创建自定义注解对应切面，与上一中情况的切面类似，代码如下**

* ```java
  import org.aspectj.lang.ProceedingJoinPoint;
  import org.aspectj.lang.annotation.Around;
  import org.aspectj.lang.annotation.Aspect;
  import org.springframework.stereotype.Component;
  
  import com.alibaba.fastjson.JSON;
  
  /**
   * @author yjl
   * @version $Id: DoneTimeAspect.java, v 0.1 2020-12-28 10:59 yjl Exp $$
   */
  @Aspect
  @Component
  public class DoneTimeAspect {
  
      @Around("@annotation(doneTime)")
      public Object around(ProceedingJoinPoint joinPoint, DoneTime doneTime) throws Throwable {
          System.out.println("方法开始时间是:"+new Date());
          String s = JSON.toJSONString(doneTime.param());
          String join = String.join(s, "   这是获取的参数");
          System.out.println(join);
          Object o = joinPoint.proceed();
          System.out.println("方法结束时间是:"+new Date()) ;
          return o;
      }
  }
  ```

## 创建Controller 测试

* **创建一个 AopController 进行测试，其实就是两个普通的 Web 请求方法，其中 aopTest 没有使用自定义注解，aopTestDoneTime使用了自定义注解**

* ```java
  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;
  
  import testhttp.demo.aspect.DoneTime;
  
  /**
   * 测试 Aop切面
   *
   * @author yjl
   * @version $Id: AopController.java, v 0.1 2020-12-28 9:29 yjl Exp $$
   */
  @RestController
  @RequestMapping("/aopTest")
  public class AopController {
  
      @GetMapping("/logAspect")
      public String aopTest() {
          System.out.println("方法执行在执行中。。。");
          return "成功";
      }
  
      @GetMapping("/doneTimeAspect")
      @DoneTime(param = "aopTestDoneTime")
      public String aopTestDoneTime(String name) {
          System.out.println("利用自定义注解使用切面");
          System.out.println("方法执行中。。。。。。");
          return "成功！！！";
      }
  
  }
  ```

* 