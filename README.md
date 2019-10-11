### 功能说明
- 模仿Spring IOC实现的IOC容器，用于学习目的
- 支持JSR-330标准，同时也提供了一些自定义的注解
- 支持通过包扫描的方式注册bean，同时也支持通过Java的代码方式注册bean


### 使用说明
- 使用`@IocBoot`注解初始化ioc容器,容器启动后能够自动扫描`scanBase`属性所指定包下符合条件所有使用了`@Singleton`,`Configuration`等注解,然后添加到ioc容器中
```java
@IocBoot(scanBase = "xyz.stupidwolf.ioc")
public class IocStart {
    public static void main(String[] args) { 
        BeanFactory beanFactory = beanFactory = IocApplication.run(IocStart.class);
    }
}
```

- 通过`@Singleton`注解注册bean,具体如下:
```java
import javax.inject.Singleton;

@Singleton
public class SimpleBean {
    public void hello() {
        System.out.println("hello, my-ioc!");
    }
}
```
可以显示通过`@Name`注解指定具体bean注册到ioc容器上的名字,若不指定,则默认使用类名(第一个字母小写)

- 通过`@Inject`注解注入bean,当前支持属性注入以及构造方法注入的方式:
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
其中,通过构造方法注入的参数，需要配套使用`@Named`注解.

- 已通过的测试场景:
    - 全部使用通过属性注入
    - 全部使用构造方法注入
    - 混合使用属性和构造方法方式注入
    - 通过属性注入时,支持循环依赖注入
    - 通过构造方法注入时,若存在循环依赖,ioc容器能够识别,并抛出对应的异常信息

- 更多例子参考文件`src/test/java/xyz/stupidwolf/ioc/IocStart.java`


- TODO 支持自定义注解`@Configuration`,`@Bean`等自定义注解
- TODO 支持auto by class type方式注入bean
