package xyz.stupidwolf.ioc.factory.support;

import xyz.stupidwolf.ioc.exception.BeansException;

public interface BeanFactory {
    <T> T getBean(String beanName, Class<T> requireType) throws BeansException;
}
