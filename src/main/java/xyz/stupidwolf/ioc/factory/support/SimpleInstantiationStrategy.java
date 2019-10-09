package xyz.stupidwolf.ioc.factory.support;

import xyz.stupidwolf.ioc.factory.config.BeanDefinition;
import xyz.stupidwolf.ioc.exception.BeansException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SimpleInstantiationStrategy implements InstantiationStrategy {
    @Override
    public Object instantiate(BeanDefinition beanDefinition) throws BeansException {
        Class<?> clazz = beanDefinition.getBeanClass();
        try {
            Constructor defaultConstructor = clazz.getDeclaredConstructor((Class[])null);
            defaultConstructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new BeansException("no default constructor found: ", e);
        } catch (IllegalAccessException e) {
            throw new BeansException("Is the constructor accessible?", e);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            throw new BeansException("Constructor threw exception: ", e);
        }
        return null;
    }
}
