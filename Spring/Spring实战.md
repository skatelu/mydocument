# Spring 实战5

## 第一章

### 1.3.3 测试控制器

* 在测试 SpringBoot Controller相关请求的时候，可以使用 @WebMvcTest(xxxController.class) 注解

* ```java
  @RunWith(SpringRunner.class)
  // @WebMvcTest 会让这个测试在 Spring MVC 应用的上下文中执行 
  // 在本例中，它会将 HomeController 注册到 Spring MVC 中，这样的话，我们就可以向它发送请求了
  @WebMvcTest(HomeController.class)   // <1> 指定要测试的controller
  public class HomeControllerTest {
  
    /** 
     * MockMvc是由spring-test包提供，实现了对Http请求的模拟，能够直接使用网络的形式，转换到Controller的调用，使得测试速度快、不依赖网络环境。同时提供了一套验证的工具，结果的验证十分方便。
     */
    @Autowired
    private MockMvc mockMvc;   // <2> 
  
    @Test
    public void testHomePage() throws Exception {
      mockMvc.perform(get("/"))    // <3>
      
        .andExpect(status().isOk())  // <4>
        
        .andExpect(view().name("home"))  // <5>
        
        .andExpect(content().string(           // <6>
            containsString("Welcome to...")));  
    }
  
  }
  ```

* 



## 第二章

### 2.3 校验表达输入

* **Spring 用注解对传入的参数举行校验，JSR-303**
* 