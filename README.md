### 功能说明
- 模仿Spring IOC实现的IOC容器，用于学习目的
- 支持JSR-330标准，同时也提供了一些自定义的注解
- 支持通过包扫描的方式注册bean，同时也支持通过Java的代码方式注册bean
- 支持通过`bean name`或者`bean class type`方式获取bean


### 使用说明
- 使用`@IocBoot`注解初始化ioc容器,容器启动后能够自动扫描`scanBase`属性所指定包下符合条件所有使用了`@Singleton`,`Configuration`等注解,然后添加到ioc容器中
```java
@IocBoot(scanBase = "xyz.stupidwolf.ioc")
public class IocStart {
    public static void main(String[] args) { 
        BeanFactory beanFactory = IocApplication.run(IocStart.class);
    }
}
```

- 通过`@Singleton`注册bean,具体如下:
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

- 通过`@Inject`注入bean,当前支持属性注入以及构造方法注入的方式:
```java
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class MixDependBean {
    @Inject
    private SimpleBean simpleBean;

    private CircularDependBean circularDependBean;

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
其中,通过构造方法注入的参数，需要配套使用`@Named`.

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
    - 全部使用通过属性注入
    - 全部使用构造方法注入
    - 混合使用属性和构造方法方式注入
    - 通过属性注入时,支持循环依赖注入
    - 通过构造方法注入时,若存在循环依赖,ioc容器能够识别,并抛出对应的异常信息
   

- 更多例子参考文件`src/test/java/xyz/stupidwolf/ioc/IocStart.java`


- TODO 支持自定义注解`@Configuration`,`@Bean`等自定义注解
