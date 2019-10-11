package xyz.stupidwolf.ioc.factory;

import java.lang.reflect.Constructor;
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
}
