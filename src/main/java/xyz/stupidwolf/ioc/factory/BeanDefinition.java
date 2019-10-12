package xyz.stupidwolf.ioc.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class BeanDefinition {
    private String beanName;

    private Class<?> beanClass;

    private boolean isSingleton = true;

    private boolean isPrototype = false;

    private List<String> dependBeanNames;

    private Constructor<?> constructor;

    private String[] constructorArgsBeanName;

    private Object[] constructorArgs;

    private Method method;

    private String[] methodArgsBeanName;

    private Object[] methodArgs;

    /** 仅当bean是通过@Configuration方式定义时才设置该属性的值 **/
    private String configurationBeanName;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }

    public boolean isPrototype() {
        return isPrototype;
    }

    public void setPrototype(boolean prototype) {
        isPrototype = prototype;
    }

    public List<String> getDependBeanNames() {
        return dependBeanNames;
    }

    public void setDependBeanNames(List<String> dependBeanNames) {
        this.dependBeanNames = dependBeanNames;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public String[] getConstructorArgsBeanName() {
        return constructorArgsBeanName;
    }

    public void setConstructorArgsBeanName(String[] constructorArgsBeanName) {
        this.constructorArgsBeanName = constructorArgsBeanName;
    }

    public Object[] getConstructorArgs() {
        return constructorArgs;
    }

    public void setConstructorArgs(Object[] constructorArgs) {
        this.constructorArgs = constructorArgs;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String[] getMethodArgsBeanName() {
        return methodArgsBeanName;
    }

    public void setMethodArgsBeanName(String[] methodArgsBeanName) {
        this.methodArgsBeanName = methodArgsBeanName;
    }

    public Object[] getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(Object[] methodArgs) {
        this.methodArgs = methodArgs;
    }

    public String getConfigurationBeanName() {
        return configurationBeanName;
    }

    public void setConfigurationBeanName(String configurationBeanName) {
        this.configurationBeanName = configurationBeanName;
    }
}
