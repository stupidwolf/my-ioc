package xyz.stupidwolf.ioc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.stupidwolf.ioc.annotation.IocBoot;
import xyz.stupidwolf.ioc.boot.IocApplication;
import xyz.stupidwolf.ioc.example.*;
import xyz.stupidwolf.ioc.exception.BeansException;
import xyz.stupidwolf.ioc.factory.BeanFactory;

@IocBoot(scanBase = "xyz.stupidwolf.ioc")
public class IocStart {
    private BeanFactory beanFactory;
    @Before
    public void init() {
        beanFactory = IocApplication.run(IocStart.class);
    }

    /**
     * 属性方式注入
     */
    @Test
    public void testSimpleBeanInject() {
        SimpleBean simpleBean = beanFactory.getBean("simpleBean", SimpleBean.class);
        Assert.assertNotNull(simpleBean);
        simpleBean.hello();
    }

    /**
     * 依赖于其它bean
     */
    @Test
    public void  testDependBeanInject() {
        DependBeanByField dependBeanByField = beanFactory.getBean("dependBeanByField", DependBeanByField.class);
        Assert.assertNotNull(dependBeanByField);
        dependBeanByField.hello();
    }

    /**
     * 存有循环依赖-属性方式注入
     */
    @Test
    public void testDependCircular() {
        CircularDependBean circularDependBean = beanFactory.getBean("circularDependBean", CircularDependBean.class);
        Assert.assertNotNull(circularDependBean);
        circularDependBean.hello();
    }

    /**
     * 构造方法注入
     */
    @Test
    public void testDependBeanByConstructor() {
        DependBeanByConstructor dependBeanByConstructor =
                beanFactory.getBean("dependBeanByConstructor", DependBeanByConstructor.class);

        Assert.assertNotNull(dependBeanByConstructor);
        dependBeanByConstructor.hello();
    }

    /**
     * 混合方式（构造方法 + 属性）注入
     */
    @Test
    public void testMixDependBean() {
        MixDependBean mixDependBean = beanFactory.getBean("mixDependBean", MixDependBean.class);
        Assert.assertNotNull(mixDependBean);
        mixDependBean.hello();
    }

    /**
     * 构造方法方式-存有循环依赖
     * 抛出运行时异常: BeansException
     */
    @Test
    public void testCircularDependBeanByConstructor() {
        try {
            CircularDependBeanByConstructor circularDependBeanByConstructor = beanFactory.getBean("circularDependBeanByConstructor",
                    CircularDependBeanByConstructor.class);
            circularDependBeanByConstructor.hello();
            Assert.fail("can't arrive here!");
        } catch (BeansException ex) {
            Assert.assertTrue("circle depend happen.", true);
        }

    }

    /**
     * 通过 class type方式注入单例bean
     */
    @Test
    public void testInjectBeanByClassType() {
        SimpleBean simpleBean = beanFactory.getBean(SimpleBean.class);
        Assert.assertNotNull(simpleBean);
        simpleBean.hello();
    }

    /**
     * 不存在bean
     */
    @Test
    public void testInjectBeanByClassTypeNotFound() {
        try {
            beanFactory.getBean(NoBean.class);
            Assert.fail("the code should not run around here!");
        } catch (BeansException ex) {
            Assert.assertTrue(ex.getLocalizedMessage(), true);
        }
    }

    /**
     * java编程方式注册bean
     */
    @Test
    public void testGetBeanFromConfigurationAnnotation() {
        JavaTypeBean javaTypeBean1 = beanFactory.getBean("javaTypeBean1", JavaTypeBean.class);
        JavaTypeBean javaTypeBean2 = beanFactory.getBean("javaTypeBean2", JavaTypeBean.class);
        JavaTypeBean javaTypeBeanCustom = beanFactory.getBean("javaTypeBeanCustom", JavaTypeBean.class);
        Assert.assertNotNull(javaTypeBean1);
        Assert.assertNotNull(javaTypeBean2);
        Assert.assertNotNull(javaTypeBeanCustom);

        Assert.assertTrue(javaTypeBean1 != javaTypeBean2 && javaTypeBean2 != javaTypeBeanCustom);

        javaTypeBean1.hello();
        javaTypeBean2.hello();
        javaTypeBeanCustom.hello();
    }

    /**
     * java编程方式注册bean,方法参数存有其它bean的依赖
     */
    @Test
    public void testGetBeanFromConfigurationAnnotationWithDependOtherBeans() {
        JavaTypeBeanDependOtherBeans javaTypeBeanDependOtherBeans = beanFactory.getBean(JavaTypeBeanDependOtherBeans.class);
        Assert.assertNotNull(javaTypeBeanDependOtherBeans);
        javaTypeBeanDependOtherBeans.hello();
    }

    /**
     * java编程方式注册bean,存有循环依赖
     */
    @Test
    public void testGetBeanFromConfigurationAnnotationCircleEx() {
        try {
            JavaTypeBean javaTypeBean = beanFactory.getBean("javaTypeBeanCircle", JavaTypeBean.class);
            Assert.fail("can not arrive hear!");
        } catch (BeansException e) {
            Assert.assertTrue("an bean exception", true);
        }
    }
}
