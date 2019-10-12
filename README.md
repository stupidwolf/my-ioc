### 功能说明
- 模仿Spring IOC实现的IOC容器，用于学习目的
- 支持JSR-330标准，同时也提供了一些自定义的注解
- 支持通过包扫描的方式注册bean，同时也支持通过Java的代码方式注册bean
- 支持通过`bean name`或者`bean class type`方式获取bean


### 使用说明
#### 初始化容器
    使用`@IocBoot`注解初始化ioc容器,容器启动后能够自动扫描`scanBase`属性所指定包下符合条件所有使用了`@Singleton`,`Configuration`等注解,然后添加到ioc容器中
```java
@IocBoot(scanBase = "xyz.stupidwolf.ioc")
public class IocStart {
    public static void main(String[] args) { 
        BeanFactory beanFactory = IocApplication.run(IocStart.class);
    }
}
```

#### 声明bean
- 方式1: 通过`@Singleton`注册bean,具体如下:
```java
import javax.inject.Singleton;

@Singleton
public class SimpleBean {
    public void hello() {
        System.out.println("hello, my-ioc!");
    }
}
```
可以显示通过`@Name`指定具体bean注册到ioc容器上的名字,若不指定,则默认使用类名(第一个字母小写)

- 方式2: java代码方式注册,通过`@Configuration`,`@Named`
```java
import xyz.stupidwolf.ioc.annotation.Bean;
import xyz.stupidwolf.ioc.annotation.Configuration;

import javax.inject.Named;

@Configuration
public class ConfigurationBean {
    // 不存在其它bean依赖
    @Bean
    public JavaTypeBean javaTypeBean1() {
        return new JavaTypeBean();
    }

    // 存在其它bean的依赖,注意方法参数需要使用`@Named`参数显示声明
    @Bean
    public JavaTypeBeanDependOtherBeans javaTypeBeanDependOtherBeans(@Named("simpleBean") SimpleBean simpleBean,
                                                                     @Named("javaTypeBean") JavaTypeBean javaTypeBean) {
        return new JavaTypeBeanDependOtherBeans(simpleBean, javaTypeBean);
    }
}
```

#### 注入注解
    当你的java对象被my-ioc容器管理时,容器会自动通过属性或者构造方法的方式帮你注入你所需要的bean,你只需要在需要注入的地方使用`@Inject`注解即可.当存在多个相同类型的`bean`实例时,需要使用`@Named`注解区分开
```java
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class MixDependBean {
    // 方式1: 属性注入方式
    @Inject
    private SimpleBean simpleBean;

    private CircularDependBean circularDependBean;
    
    // 方式2: 基于构造方法方式注入
    @Inject
    public MixDependBean(@Named("circularDependBean") CircularDependBean circularDependBean) {
        this.circularDependBean = circularDependBean;
    }

    public void hello() {
        this.simpleBean.hello();
        this.circularDependBean.hello();
    }
}
```

- 通过`bean name`获取bean:
```java
// 方式1: 通过bean name 方式获取
SimpleBean simpleBean = beanFactory.getBean("simpleBean", SimpleBean.class);
simpleBean.hello();

// 方式2: 通过class type 方式获取bean:
SimpleBean simpleBean = beanFactory.getBean(SimpleBean.class);
simpleBean.hello();
```

- 已通过的测试场景:
    - @Singleton方式定义bean
    - @Configuration方式定义bean
    - 全部使用通过属性注入
    - 全部使用构造方法注入
    - 混合使用属性和构造方法方式注入
    - 通过属性注入时,支持循环依赖注入
    - 通过构造方法注入时,若存在循环依赖,ioc容器能够识别,并抛出对应的异常信息
    - 通过java代码方式注入时,若存在循环依赖,ioc容器能够检测,并抛出对应的异常信息
   

- 更多例子参考文件`src/test/java/xyz/stupidwolf/ioc/IocStart.java`

