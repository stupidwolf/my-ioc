package xyz.stupidwolf.ioc.factory;

import xyz.stupidwolf.ioc.factory.BeanDefinition;
import xyz.stupidwolf.ioc.exception.BeanDefinitionStoreException;
import xyz.stupidwolf.ioc.exception.NoSuchBeanDefinitionException;

public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException;

    void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
}
