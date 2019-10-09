package xyz.stupidwolf.ioc.factory.support;

import xyz.stupidwolf.ioc.exception.BeansException;

public interface BeanPostProcessor {
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
