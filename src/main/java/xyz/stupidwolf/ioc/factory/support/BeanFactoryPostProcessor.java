package xyz.stupidwolf.ioc.factory.support;

import xyz.stupidwolf.ioc.exception.BeansException;

public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(BeanFactory beanFactory) throws BeansException;
}
