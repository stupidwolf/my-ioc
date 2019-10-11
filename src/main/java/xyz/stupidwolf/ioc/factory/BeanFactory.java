package xyz.stupidwolf.ioc.factory;

import xyz.stupidwolf.ioc.exception.BeansException;

public interface BeanFactory {
    <T> T getBean(String beanName, Class<T> requireType) throws BeansException;
}
